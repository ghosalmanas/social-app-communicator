package com.wtf.app.web.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages WebDriver instances for async operations with connection pooling.
 * Handles thread-safe creation, reuse, and cleanup of WebDriver instances.
 */
@Component
public class AsyncWebDriverManager implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(AsyncWebDriverManager.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("webdriver.access");
    
    // Thread-safe storage for WebDriver instances and temp directories
    private final Map<String, WebDriver> driverMap = new ConcurrentHashMap<>();
    private final Map<String, Path> tempDirs = new ConcurrentHashMap<>();
    private final ChromeOptions baseChromeOptions;
    
    // Connection pool for WebDriver instances
    private final Queue<WebDriver> driverPool = new ConcurrentLinkedQueue<>();
    private final int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private final AtomicInteger activeDrivers = new AtomicInteger(0);
    private final Object poolLock = new Object();
    
    // Statistics and monitoring
    private long totalDriverCreations = 0;
    private long totalDriverReuses = 0;
    
    @Autowired
    public AsyncWebDriverManager(ChromeOptions chromeOptions) {
        this.baseChromeOptions = chromeOptions != null ? chromeOptions : createDefaultChromeOptions();
        logger.info("AsyncWebDriverManager initialized with max pool size: {}", maxPoolSize);
        
        // Register shutdown hook for proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    private ChromeOptions createDefaultChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        
        // Common options that don't need to be unique per instance
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--window-size=1920,1080",
            "--disable-extensions",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--disable-infobars",
            "--disable-browser-side-navigation"
        );
        
        // Performance optimizations
        options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);
        options.setUnhandledPromptBehaviour(org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
        
        return options;
    }
    
    private ChromeOptions createInstanceSpecificOptions() {
        // Create a new options object as a copy of the base options
        ChromeOptions options = new ChromeOptions();
        options.merge(baseChromeOptions);
        
        // Create a unique user data directory for this instance
        try {
            Path tempUserDataDir = Files.createTempDirectory("chrome_user_data_");
            tempUserDataDir.toFile().deleteOnExit();
            options.addArguments("--user-data-dir=" + tempUserDataDir.toAbsolutePath());
            
            // Store the temp directory path for cleanup
            tempDirs.put(Thread.currentThread().getName(), tempUserDataDir);
            
            logger.debug("Created temporary Chrome user data directory: {}", tempUserDataDir);
        } catch (IOException e) {
            logger.warn("Failed to create temporary Chrome user data directory. Using default profile.", e);
        }
        
        return options;
    }
    
    /**
     * Creates a new WebDriver instance asynchronously with connection pooling.
     * @return CompletableFuture containing the WebDriver instance
     */
    @Async("automationTaskExecutor")
    public CompletableFuture<WebDriver> createDriver() {
        String threadName = Thread.currentThread().getName();
        accessLogger.debug("Requesting WebDriver for thread: {}", threadName);
        
        // Try to get a driver from the pool first
        WebDriver driver = driverPool.poll();
        if (driver != null) {
            totalDriverReuses++;
            accessLogger.debug("Reusing WebDriver from pool for thread: {}", threadName);
            driverMap.put(threadName, driver);
            return CompletableFuture.completedFuture(driver);
        }
        
        // Check if we can create a new driver
        if (activeDrivers.get() >= maxPoolSize) {
            accessLogger.warn("Max pool size ({}) reached, waiting for available WebDriver", maxPoolSize);
            return CompletableFuture.failedFuture(
                new IllegalStateException("Maximum number of WebDriver instances reached")
            );
        }
        
        // Create a new driver
        try {
            accessLogger.info("Creating new WebDriver instance for thread: {}", threadName);
            
            // Create instance-specific options
            ChromeOptions instanceOptions = createInstanceSpecificOptions();
            
            // Create the driver with proper error handling
            driver = new ChromeDriver(instanceOptions);
            activeDrivers.incrementAndGet();
            totalDriverCreations++;
            
            // Configure timeouts and other settings
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(50));
            
            // Store the driver in the thread map
            driverMap.put(threadName, driver);
            
            accessLogger.info("Created new WebDriver for thread: {} (Active: {})", 
                threadName, activeDrivers.get());
                
            return CompletableFuture.completedFuture(driver);
            
        } catch (Exception e) {
            // Clean up on failure
            cleanupTempDir(threadName);
            String errorMsg = String.format("Failed to create WebDriver for thread %s: %s", 
                threadName, e.getMessage());
            accessLogger.error(errorMsg, e);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        }
    }
    
    /**
     * Safely quits and removes a WebDriver instance.
     * @param driver The WebDriver instance to quit
     */
    public void quitDriver(WebDriver driver) {
        if (driver == null) {
            return;
        }
        
        String threadName = Thread.currentThread().getName();
        accessLogger.debug("Quitting WebDriver for thread: {}", threadName);
        
        // Remove from the thread map
        driverMap.remove(threadName);
        
        try {
            // Only quit if the driver is not in the pool
            if (!driverPool.contains(driver)) {
                try {
                    driver.quit();
                    activeDrivers.decrementAndGet();
                    accessLogger.info("Closed WebDriver for thread: {} (Active: {})", 
                        threadName, activeDrivers.get());
                } catch (Exception e) {
                    accessLogger.error("Error closing WebDriver for thread: {}", threadName, e);
                }
            } else {
                accessLogger.debug("WebDriver already in pool, skipping quit for thread: {}", threadName);
            }
        } finally {
            // Always clean up the temp directory
            cleanupTempDir(threadName);
        }
    }
    
    private void cleanupTempDir(String threadName) {
        Path tempDir = tempDirs.remove(threadName);
        if (tempDir != null) {
            try {
                // Skip if the directory doesn't exist
                if (!Files.exists(tempDir)) {
                    return;
                }
                
                // Delete the temp directory and its contents
                Files.walk(tempDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .filter(java.io.File::exists)
                    .forEach(file -> {
                        try {
                            if (!file.delete()) {
                                file.deleteOnExit();
                            }
                        } catch (SecurityException e) {
                            logger.warn("Security exception deleting file: {}", file, e);
                            file.deleteOnExit();
                        }
                    });
                    
                accessLogger.debug("Cleaned up temporary directory: {}", tempDir);
                
            } catch (IOException e) {
                logger.warn("Failed to clean up temporary directory: {}", tempDir, e);
            } catch (Exception e) {
                logger.error("Unexpected error cleaning up temp directory: {}", tempDir, e);
            }
        }
    }
    
    /**
     * Gets the WebDriver for the current thread if it exists and is active.
     * @return The WebDriver instance or null if not found or inactive
     */
    public WebDriver getDriverForCurrentThread() {
        String threadName = Thread.currentThread().getName();
        WebDriver driver = driverMap.get(threadName);
        
        if (driver != null) {
            if (isSessionActive(driver)) {
                accessLogger.trace("Returning active WebDriver for thread: {}", threadName);
                return driver;
            } else {
                accessLogger.debug("Found inactive WebDriver for thread: {}, cleaning up", threadName);
                quitDriver(driver);
            }
        }
        
        return null;
    }
    
    /**
     * Shuts down the WebDriver manager and cleans up all resources.
     */
    public void shutdown() {
        logger.info("Shutting down WebDriver manager (Active drivers: {})", activeDrivers.get());
        
        // Close all drivers in the pool
        WebDriver driver;
        while ((driver = driverPool.poll()) != null) {
            try {
                driver.quit();
                activeDrivers.decrementAndGet();
            } catch (Exception e) {
                logger.error("Error closing pooled WebDriver", e);
            }
        }
        
        // Close all active drivers
        driverMap.forEach((threadName, drv) -> {
            try {
                if (drv != null) {
                    drv.quit();
                    activeDrivers.decrementAndGet();
                    logger.debug("Closed WebDriver for thread: {}", threadName);
                }
            } catch (Exception e) {
                logger.error("Error closing WebDriver for thread: " + threadName, e);
            }
        });
        
        // Clean up all temp directories
        tempDirs.keySet().forEach(this::cleanupTempDir);
        
        // Clear all collections
        driverMap.clear();
        tempDirs.clear();
        driverPool.clear();
        
        logger.info("WebDriver manager shutdown complete");
    }
    
    @Override
    public void destroy() {
        shutdown();
    }
    
    /**
     * Returns statistics about WebDriver usage.
     * @return A map containing statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "totalCreations", totalDriverCreations,
            "totalReuses", totalDriverReuses,
            "activeDrivers", activeDrivers.get(),
            "poolSize", driverPool.size(),
            "threadMappings", driverMap.size(),
            "maxPoolSize", maxPoolSize
        );
    }
    
    /**
     * Checks if a WebDriver session is active and responsive.
     * @param driver The WebDriver to check
     * @return true if the session is active and responsive, false otherwise
     */
    public boolean isSessionActive(WebDriver driver) {
        if (driver == null) {
            return false;
        }
        
        try {
            // Check if the driver is in the pool (already closed)
            if (driverPool.contains(driver)) {
                return false;
            }
            
            // Simple check to see if the session is still valid
            driver.getWindowHandle();
            return true;
            
        } catch (Exception e) {
            accessLogger.debug("WebDriver session is not active: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Returns a driver to the pool for reuse.
     * @param driver The WebDriver to return to the pool
     */
    public void returnToPool(WebDriver driver) {
        if (driver == null) {
            return;
        }
        
        String threadName = Thread.currentThread().getName();
        
        // Only add to pool if it's not already there and we're under the max pool size
        if (!driverPool.contains(driver) && driverPool.size() < maxPoolSize / 2) {
            try {
                // Clear cookies and local storage between uses
                driver.manage().deleteAllCookies();
                
                // Add to pool
                driverPool.offer(driver);
                accessLogger.debug("Returned WebDriver to pool from thread: {}", threadName);
                
            } catch (Exception e) {
                accessLogger.warn("Error preparing WebDriver for pool from thread: {}", threadName, e);
                quitDriver(driver);
            }
        } else {
            // If pool is full or driver already in pool, just quit it
            accessLogger.debug("Pool full or driver already in pool, closing WebDriver from thread: {}", threadName);
            quitDriver(driver);
        }
    }
}
