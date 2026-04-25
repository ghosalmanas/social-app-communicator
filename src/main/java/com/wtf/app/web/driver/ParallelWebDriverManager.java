package com.wtf.app.web.driver;

import com.wtf.app.model.enums.TaskType;
import com.wtf.app.util.SessionKeepAlive;
import com.wtf.app.util.ThreadLocalAutomationContext;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.inject.Provider;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.remote.*;

import java.util.*;
import java.io.File;
import java.time.Duration;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Manages multiple WebDriver instances for parallel execution with isolated profiles.
 */
@Configuration
@Lazy
public class ParallelWebDriverManager {
    // Minimum free disk space required in bytes (1GB)
    private static final long MIN_DISK_SPACE_BYTES = 1024 * 1024 * 1024;
    private static final long MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024; // 100MB max cache per profile
    private static final long MAX_PROFILES_TO_KEEP = 3; // Keep only the 3 most recent profiles
    private static final long MAX_PROFILE_AGE_MINUTES = 30; // Delete profiles older than 30 minutes
    // Maximum number of WebDriver instances to prevent resource exhaustion
    private static final int MAX_CONCURRENT_DRIVERS = 5;

    // Time in milliseconds between disk space checks
    private static final long DISK_SPACE_CHECK_INTERVAL_MS = 300000; // 5 minutes
    private static final Logger logger = LoggerFactory.getLogger(ParallelWebDriverManager.class);
    
    @Value("${browser.profile.directory:C:/temp/chrome-profiles}")
    private String profileBaseDir;
    
    @Value("${webdriver.profile.persist:false}")
    private boolean persistProfiles;

    @Value("${webdriver.fb.profile:Default}")
    private String fbProfileName;

    @Value("${webdriver.max.inactive.minutes:30}")
    private int maxInactiveMinutes;

    @Value("${webdriver.cleanup.interval.minutes:15}")
    private int cleanupIntervalMinutes;

    @Value("${webdriver.headless:false}")
    private boolean headless;
    
    @Value("${webdriver.timeout.pageLoad:86400}") // 24 hours in seconds
    private int pageLoadTimeout;
    
    @Value("${webdriver.timeout.implicit:2}") // 1 minute
    private int implicitWait;
    
    @Value("${webdriver.timeout.script:86400}") // 24 hours in seconds
    private int scriptTimeout;

    @Value("${webdriver.timeout.connection:86400}") // 24 hours in seconds
    private int connectionTimeout;

    @Value("${webdriver.timeout.keepAlive:300}") // 5 minutes in seconds
    private int keepAliveTimeout;

    @Autowired
    private SessionKeepAlive sessionKeepAlive;
    
    @Autowired
    private WebDriverHealthService webDriverHealthService;
    
    @Value("${webdriver.chrome.version:latest}")
    private String chromeVersion;
    
    @Value("${webdriver.chrome.binary:}")
    private String chromeBinaryPath;
    
    @Value("${webdriver.chrome.driver:}")
    private String chromeDriverPath;
    
    @Value("${webdriver.chrome.args:}")
    private String[] chromeArgs;
    
    @Value("${selenium.chrome.extension.path:}")
    private String chromeExtensionPath;
    
    @Value("${selenium.chrome.extension.required:false}")
    private boolean chromeExtensionRequired;
    
    @Value("${webdriver.chrome.experimental.options:{}}")
    private String experimentalOptionsJson = "{}";
    
    private ChromeOptions experimentalOptions = new ChromeOptions();
    
    @Value("${webdriver.retry.attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${webdriver.retry.delay:1000}")
    private long retryDelayMs;
    
    @Value("${webdriver.chrome.log.level:SEVERE}")
    private String chromeLogLevel;
    
    private final ConcurrentMap<TaskType, WebDriver> profileDriverMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> driverLastUsed= new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> driverCreationTime= new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> driverInUse= new ConcurrentHashMap<>();
    private volatile long lastDiskSpaceCheck = 0;
    private volatile long lastFreeSpace = 0;
    private final ThreadLocal<String> currentProfile = new ThreadLocal<>();
    private final ThreadLocal<WebDriver> currentDriverInThreadLocal = new ThreadLocal<>();
    private final Map<String, String> profileToPathMap = new ConcurrentHashMap<>();
    private String errorMsg = "";
    private Throwable lastError = null;
    private static final long INACTIVITY_SHUTDOWN_DELAY_MS = 45 * 60 * 1000; // 45 minutes
    private static final long BROWSER_HEALTH_CHECK_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
    private static final long CHROME_CHECK_INTERVAL_MS = 10 * 60 * 1000; // Check every 10 minutes
    private static final long CHROME_MISSING_SHUTDOWN_DELAY_MS = 45 * 60 * 1000; // 45 minutes
    private volatile long lastChromeSeenTime = System.currentTimeMillis();

    @PostConstruct
    public void init() {
        parseExperimentalOptions();
        ensureProfileDirectory();
        scheduleProfileCleanup();
        startInactivityMonitor();
        
        try {
            // Initialize WebDriverManager properties
            System.setProperty("wdm.targetPath", "target/webdrivers");
            System.setProperty("wdm.forceCache", "true");
            System.setProperty("wdm.forceDownload", "false");

            // Configure ChromeDriver logging
            Level logLevel = Level.parse(chromeLogLevel);
            System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, 
                logLevel == Level.OFF ? "true" : "false");
            
            // Set Chrome binary path if specified
            if (chromeBinaryPath != null && !chromeBinaryPath.isEmpty()) {
                System.setProperty("webdriver.chrome.binary", chromeBinaryPath);
            }
            
            // Set ChromeDriver path if specified
            if (chromeDriverPath != null && !chromeDriverPath.isEmpty()) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            }
            
            // Configure WebDriverManager
            configureWebDriverManager();
            
            logger.info("ParallelWebDriverManager initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing ParallelWebDriverManager", e);
            throw new RuntimeException("Failed to initialize ParallelWebDriverManager", e);
        }
    }


    //@PostConstruct
    public void initNotUsed() {
        // Ensure the base profile directory exists
        try {
            Path basePath = Paths.get(profileBaseDir).toAbsolutePath();
            Files.createDirectories(basePath);
            logger.info("Initialized WebDriver profiles directory at: {}", basePath);
        } catch (IOException e) {
            logger.error("Failed to create WebDriver profiles directory", e);
            throw new RuntimeException("Failed to initialize WebDriver profiles directory", e);
        }

        // Start the cleanup thread
        startCleanupThread();

        // Register shutdown hook for proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void shutdown() {
        // Clean up all drivers
        for (WebDriver driver : profileDriverMap.values()) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.error("Failed to quit driver", e);
            }
        }

        // Clean up profile directories
        try {
            Files.walk(Paths.get(profileBaseDir))
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.error("Failed to delete profile directory", e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Failed to clean up profile directories", e);
        }
    }

    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(cleanupIntervalMinutes * 60 * 1000);
                    cleanupOldProfiles();
                } catch (InterruptedException e) {
                    logger.error("Error in cleanup thread: " + errorMsg, lastError);
                    this.errorMsg = "Cleanup thread error: " + (lastError != null ? lastError.getMessage() : "Unknown error");
                    this.lastError = lastError;
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private String createUniqueProfileDirectory(String profileName) {
        if (persistProfiles) {
            // For persistent profiles, use a consistent path
            String profilePath = profileBaseDir + "/" + profileName;
            new File(profilePath).mkdirs();
            return profilePath;
        } else {
            // For non-persistent profiles, use timestamp
            String timestamp = String.valueOf(System.currentTimeMillis());
            String profilePath = profileBaseDir + "/" + profileName + "_" + timestamp;
            new File(profilePath).mkdirs();
            return profilePath;
        }
    }


    /**
     * Ensures the base profile directory exists and has enough space.
     * @throws IllegalStateException if disk space is insufficient
     */

    private void ensureProfileDirectory() {
        try {
            File profileDir = new File(profileBaseDir);
            if (!profileDir.exists()) {
                if (!profileDir.mkdirs()) {
                    throw new IOException("Failed to create profile directory: " + profileBaseDir);
                }
                logger.info("Created profile directory: {}", profileBaseDir);
            }
            
            // Ensure WhatsApp profile directory exists
            File whatsappProfileDir = new File(profileBaseDir, "whatsapp-profile");
            if (!whatsappProfileDir.exists() && !whatsappProfileDir.mkdirs()) {
                logger.warn("Failed to create WhatsApp profile directory: {}", whatsappProfileDir.getAbsolutePath());
            }
            
            cleanupOldProfiles();
            
            // Check disk space
            long freeSpace = getAvailableSpace(profileBaseDir);
            if (freeSpace < MIN_DISK_SPACE_BYTES) {
                logger.warn("Low disk space: {}MB available, {}MB required. Attempting to free up space...",
                        freeSpace / (1024 * 1024), MIN_DISK_SPACE_BYTES / (1024 * 1024));

                if (!tryFreeUpSpace()) {
                    // One final cleanup attempt
                    cleanupOldProfiles(true); // Force cleanup
                    freeSpace = getAvailableSpace(profileBaseDir);

                    if (freeSpace < MIN_DISK_SPACE_BYTES) {
                        throw new IllegalStateException(String.format(
                                "Insufficient disk space. Required: %dMB, Available: %dMB. " +
                                        "Please free up disk space and try again.",
                                MIN_DISK_SPACE_BYTES / (1024 * 1024),
                                freeSpace / (1024 * 1024)
                        ));
                    }
                }
            }
            logger.info("Disk space check passed. Available: {}MB", freeSpace / (1024 * 1024));
        } catch (Exception e) {
            logger.error("Failed to check disk space", e);
        }
    }
    
    private void scheduleProfileCleanup() {
        // Schedule regular profile cleanup
    }
    
    private long getAvailableSpace(String path) {
        return new File(path).getUsableSpace();
    }
    
    private boolean tryFreeUpSpace() {
        // Implementation for freeing up space
        return false;
    }
    
    private void cleanupOldProfiles() {
        cleanupOldProfiles(false);
    }
    
    private void cleanupOldProfiles(boolean force) {
        // Implementation for cleaning up old profiles
    }
    
    /**
     * Checks if any Chrome browser processes are running
     * @return true if Chrome is running, false otherwise
     */
    /**
     * Initiates a system shutdown with force option and no delay
     */
    private void initiateShutdown() {
        try {
            logger.warn("Initiating system shutdown...");
            Runtime.getRuntime().exec("shutdown /s /f /t 0");
            logger.info("Shutdown command executed successfully");
        } catch (IOException e) {
            logger.error("Failed to execute shutdown command: {}", e.getMessage(), e);
        }
    }

    /**
     * Checks if any Chrome browser processes are running
     * @return true if Chrome is running, false otherwise
     */
    private boolean isChromeRunning() {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq chrome.exe\"");
            try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream())) {
                // The output will contain "chrome.exe" if Chrome is running
                return scanner.findWithinHorizon("chrome.exe", 0) != null;
            }
        } catch (Exception e) {
            logger.error("Error checking Chrome processes: {}", e.getMessage());
            return false;
        }
    }
                    
    private void startInactivityMonitor() {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    boolean isChromeRunning = isChromeRunning();
                    long currentTime = System.currentTimeMillis();
                    
                    if (isChromeRunning) {
                        // Update the last seen time whenever Chrome is running
                        lastChromeSeenTime = currentTime;
                        logger.debug("Chrome process is running");
                    } else {
                        long timeSinceLastSeen = currentTime - lastChromeSeenTime;
                        long minutesSinceLastSeen = timeSinceLastSeen / (60 * 1000);
                        long minutesUntilShutdown = (CHROME_MISSING_SHUTDOWN_DELAY_MS - timeSinceLastSeen) / (60 * 1000);
                        
                        logger.debug("Chrome not running for {} minutes. Will shut down in {} minutes if Chrome doesn't return.", 
                                minutesSinceLastSeen, minutesUntilShutdown > 0 ? minutesUntilShutdown : 0);
                        
                        // Only shut down if Chrome hasn't been seen for at least 30 minutes
                        if (timeSinceLastSeen >= CHROME_MISSING_SHUTDOWN_DELAY_MS) {
                            logger.warn("No Chrome processes found for {} minutes. Initiating system shutdown...", 
                                    minutesSinceLastSeen);
                            initiateShutdown();
                            break;
                        }
                    }
                    
                    // Check every 10 minutes
                    Thread.sleep(CHROME_CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Inactivity monitor thread was interrupted");
                    break;
                }
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.setName("WebDriver-Inactivity-Monitor");
        monitorThread.start();
        logger.info("Started WebDriver inactivity monitor");
    }

    private void parseExperimentalOptions() {
        try {
            /*if (experimentalOptionsJson != null && !experimentalOptionsJson.trim().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                experimentalOptions = mapper.readValue(experimentalOptionsJson,
                    new TypeReference<Map<String, Object>>() {});
                logger.info("Loaded experimental options: {}", experimentalOptions);
            } else */{
                logger.info("No experimental options provided, using defaults");
                experimentalOptions = getHumanLikeOptions();
            }
        } catch (Exception e) {
            logger.warn("Failed to parse experimental options: {}", e.getMessage());
            experimentalOptions = getHumanLikeOptions();
        }
    }

    @Qualifier("chromeDriver")
    public Provider<WebDriver> chromeDriverProvider() {
        return this::getCurrentDriver;
    }


    /**
     * Get the WebDriver for the current thread's active profile.
     */
    public WebDriver getCurrentDriver() {
        TaskType taskType = ThreadLocalAutomationContext.getContext().getTaskType();
        logger.info("Getting WebDriver for current thread {} {}", Thread.currentThread().getName(), taskType);

        if(currentDriverInThreadLocal.get()!=null){
            logger.info("Getting WebDriver from current thread {} {}", currentDriverInThreadLocal.get().getCurrentUrl(), taskType.name());
            profileDriverMap.put(taskType, ThreadLocalAutomationContext.getContext().getDriver());
            return currentDriverInThreadLocal.get();
        }
        if(ThreadLocalAutomationContext.getContext().getDriver()!=null){
            logger.info("Getting WebDriver from current context{} {}", ThreadLocalAutomationContext.getContext().getDriver().getCurrentUrl(), taskType.name());
            profileDriverMap.put(taskType, ThreadLocalAutomationContext.getContext().getDriver());
            return ThreadLocalAutomationContext.getContext().getDriver();
        }
        logger.info("Getting WebDriver for profile: {}", taskType);


        WebDriver webDriver = profileDriverMap.get(taskType);
        if (taskType == null || webDriver ==null) {
            logger.info("CREATING NEW DRIVER as No active profile and no active driver from context.");
            WebDriver driver = getDriver(taskType);
            logger.info("Verifying and returning WebDriver for current thread {}",  ThreadLocalAutomationContext.getContext().getDriver().getCurrentUrl());
            if(driver != null) {
                return driver;
            }
            throw new IllegalStateException("No active profile set for the current thread");
        }

        logger.info("Setting WebDriver for current thread {} {}", webDriver.getCurrentUrl(), ThreadLocalAutomationContext.getContext().getDriver().getCurrentUrl());
        currentDriverInThreadLocal.set(webDriver);
        ThreadLocalAutomationContext.getContext().setDriver(webDriver);
        logger.info("Verifying and returning WebDriver for current thread {} {}", webDriver.getCurrentUrl(), ThreadLocalAutomationContext.getContext().getDriver().getCurrentUrl());
        return webDriver;
    }

    private void configureWebDriverManager() {
        try {
            logger.info("Configuring WebDriverManager for Chrome");
            
            // Setup WebDriverManager for Chrome
            WebDriverManager chromeDriverManager = WebDriverManager.chromedriver();
            
            // Set ChromeDriver version based on configuration or auto-detect
            if (chromeVersion != null && !chromeVersion.isEmpty() && !"latest".equalsIgnoreCase(chromeVersion)) {
                logger.info("Using specified ChromeDriver version: {}", chromeVersion);
                chromeDriverManager.driverVersion(chromeVersion);
            } else {
                logger.info("Using latest ChromeDriver version");
            }
            
            // Configure WebDriverManager settings
            chromeDriverManager.setup();
            
            // Log the resolved ChromeDriver version
            String resolvedVersion = chromeDriverManager.getDownloadedDriverVersion();
            logger.info("Resolved ChromeDriver version: {}", resolvedVersion);
            
            // Set system properties for ChromeDriver
            System.setProperty("webdriver.chrome.silentOutput", "true");
            
            // Additional ChromeDriver options for better stability
            System.setProperty("webdriver.chrome.verboseLogging", "false");
            System.setProperty("webdriver.chrome.silentOutput", "true");
            
            // Disable ChromeDriver logging to console
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
            
            logger.info("WebDriverManager configuration completed successfully");
        } catch (Exception e) {
            logger.error("Failed to configure WebDriverManager", e);
            throw new RuntimeException("Failed to configure WebDriverManager", e);
        }
    }


    /**
     * Get a WebDriver instance for the specified profile.
     * Creates a new instance if one doesn't exist for the profile.
     */
    public WebDriver getDriver(TaskType profileName) {
        try {

            WebDriver driver = profileDriverMap.get(profileName);
            if (driver == null || !isSessionActive(driver)) {//REVERT if Required
                int maxRetries = 3;
                int retryCount = 0;
                long retryDelayMs = 2000; // Start with 2 second delay

                while (retryCount < maxRetries) {
                    try {
                        logger.info("Creating WebDriver for profile: {}", profileName);
                        logger.info("Driver Map: {}", profileDriverMap.entrySet().stream().map(e->e.getKey()+"-"+e.getValue().getCurrentUrl()+"-"+e.getValue().getTitle()).collect(Collectors.joining(",")));

                        driver = createWebDriver(profileName.name());
                        if (isSessionActive(driver)) {// REVERT if required
                            profileDriverMap.put(profileName, driver);
                            logger.info("WebDriver created for profile: {} {}", profileName, driver.getCurrentUrl());
                            currentDriverInThreadLocal.set(driver);
                            ThreadLocalAutomationContext.getContext().setDriver(driver);
                            return driver;
                        }
                    } catch (Exception e) {
                        logger.warn("WebDriver creation attempt {} failed: {}", retryCount + 1, e.getMessage());
                    }

                    retryCount++;
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(retryDelayMs);
                            retryDelayMs *= 2; // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("Thread interrupted while waiting to retry WebDriver creation", ie);
                        }
                    }
                }

                throw new IllegalStateException("Failed to create a valid WebDriver after " + maxRetries + " attempts");
            }
            return driver;
        } catch (Exception e) {
            logger.error("Error getting WebDriver for profile: " + profileName, e);
            throw e;
        }
    }

    
    private boolean isConnectionError(WebDriverException e) {
        String message = e.getMessage();
        return message != null && (message.contains("connection refused")
                || message.contains("connection reset")
                || message.contains("failed to connect to")
                || message.contains("timed out")
                || message.contains("session not created"));
    }
    
    private void cleanupChromeProcesses() {
        try {
            logger.info("Cleaning up Chrome and ChromeDriver processes...");
            
            // Kill Chrome processes
            ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", "chrome.exe");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Successfully terminated Chrome processes");
            } else {
                logger.warn("Failed to terminate Chrome processes, exit code: {}", exitCode);
            }
            
            // Kill ChromeDriver processes
            processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", "chromedriver.exe");
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            
            // Wait for the process to complete
            exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Successfully terminated ChromeDriver processes");
            } else {
                logger.warn("Failed to terminate ChromeDriver processes, exit code: {}", exitCode);
            }
            
            // Add a small delay to allow processes to fully terminate
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            logger.error("Error during Chrome process cleanup: {}", e.getMessage(), e);
        }
    }
    
    private WebDriver createWebDriver(String profileName) {
        logger.info("Creating new WebDriver instance for profile: {}", profileName);

        try {
            // Configure Chrome options
            ChromeOptions options = getHumanLikeOptions();

            // Determine profile directory based on profile persistence setting
            if (persistProfiles && profileBaseDir.contains("User Data")) {
                // Using existing Chrome user data directory - use it directly
                logger.info("Using existing Chrome user data directory: {}", profileBaseDir);
                options.addArguments("--user-data-dir=" + profileBaseDir);
                // Use the configured Facebook profile name for Facebook tasks, Default for others
                String profileDirName = profileName.startsWith("FACEBOOK_") ? fbProfileName : "Default";
                logger.info("Using profile directory: {}", profileDirName);
                options.addArguments("--profile-directory=" + profileDirName);
            } else {
                // Creating new profile directory under base directory
                File profileDir = new File(profileBaseDir, profileName);
                if (!profileDir.exists() && !profileDir.mkdirs()) {
                    throw new RuntimeException("Failed to create profile directory: " + profileDir.getAbsolutePath());
                }
                logger.info("Creating new profile directory: {}", profileDir.getAbsolutePath());
                options.addArguments("--user-data-dir=" + profileDir.getAbsolutePath());
                options.addArguments("--profile-directory=Default");
            }

            // Additional Chrome options
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-blink-features=AutomationControlled");

            // Set up ChromeDriver service
            ChromeDriverService service = new ChromeDriverService.Builder()
                .withLogLevel(ChromiumDriverLogLevel.INFO)
                .build();

            // Create and return the WebDriver instance
            WebDriver driver = new ChromeDriver(service, options);

            // Configure timeouts
            driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout))
                .implicitlyWait(Duration.ofSeconds(implicitWait));

            logger.info("Successfully created WebDriver for profile: {}", profileName);
            return driver;

        } catch (Exception e) {
            logger.error("Failed to create WebDriver for profile: {}", profileName, e);

            // Log additional diagnostic information
            logger.error("Chrome version: {}", chromeVersion);
            logger.error("Profile base directory: {}", profileBaseDir);
            logger.error("Headless mode: {}", headless);
            logger.error("Page load timeout: {}s, Implicit wait: {}s", pageLoadTimeout, implicitWait);

            // Clean up any partially created resources
            cleanupChromeProcesses();

            throw new RuntimeException("Failed to create WebDriver: " + e.getMessage(), e);
        }
    }


    /**
     * Safely quit a WebDriver instance, ensuring all resources are cleaned up.
     * Handles various edge cases and logs detailed information.
     * @param driver The WebDriver instance to quit
     */
    private void safeQuitDriver(WebDriver driver) {
        if (driver == null) {
            return;
        }

        String sessionId = "unknown";
        try {
            if (driver instanceof RemoteWebDriver) {
                SessionId id = ((RemoteWebDriver) driver).getSessionId();
                if (id != null) {
                    sessionId = id.toString();
                }
            }

            logger.info("Attempting to quit WebDriver session: {}", sessionId);

            // Try to close all windows first
            try {
                for (String handle : driver.getWindowHandles()) {
                    try {
                        driver.switchTo().window(handle);
                        driver.close();
                        logger.debug("Closed window: {}", handle);
                        Thread.sleep(100); // Small delay between window closes
                    } catch (Exception e) {
                        logger.debug("Error closing window {}: {}", handle, e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.debug("Error while closing windows: {}", e.getMessage());
            }

            // Then quit the driver
            try {
                driver.quit();
                logger.info("Successfully quit WebDriver session: {}", sessionId);
            } catch (Exception e) {
                logger.warn("Error during driver.quit() for session {}: {}", sessionId, e.getMessage());
                // Try to kill the process if possible
                if (e.getMessage() != null && e.getMessage().contains("failed to kill")) {
                    logger.warn("Attempting to forcefully terminate the browser process...");
                    try {
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
                        } else {
                            Runtime.getRuntime().exec("pkill -f chrome");
                        }
                        logger.info("Browser process terminated");
                    } catch (Exception ex) {
                        logger.error("Failed to terminate browser process: {}", ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Unexpected error while quitting WebDriver (session: {}): {}",
                    sessionId, e.getMessage(), e);
        } finally {
            // Ensure driver reference is cleared
            try {
                driver = null;
            } catch (Exception e) {
                // Ignore any errors during cleanup
            }
        }
    }

    public boolean isSessionActive(WebDriver driver) {
        if (driver == null) {
            logger.debug("WebDriver is null, session is not active");
            return false;
        }
        
        try {
            WebDriver.Timeouts timeouts = driver.manage().timeouts();
            Duration originalImplicitWait = timeouts.getImplicitWaitTimeout();
            
            try {
                // Set a short timeout for the checks
                timeouts.implicitlyWait(Duration.ofSeconds(2));
                
                // Check if we can get the current URL
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl == null || currentUrl.trim().isEmpty() || currentUrl.startsWith("data:")) {
                    logger.debug("Invalid or empty URL detected: {}", currentUrl);
                    return false;
                }
                
                // Check if we can get a window handle
                try {
                    driver.getWindowHandle();
                } catch (NoSuchWindowException e) {
                    logger.debug("No window handle found, session is not active");
                    return false;
                }
                
                // For RemoteWebDriver, check session ID
                if (driver instanceof RemoteWebDriver) {
                    try {
                        ((RemoteWebDriver) driver).getSessionId();
                    } catch (Exception e) {
                        logger.debug("Invalid session ID: {}", e.getMessage());
                        return false;
                    }
                }
                
                // Try to execute JavaScript to check if the page is responsive
                try {
                    boolean isDocumentReady = (boolean) ((JavascriptExecutor) driver).executeScript(
                        "return document.readyState === 'complete'");
                        
                    Object userAgent = ((JavascriptExecutor) driver).executeScript("return navigator.userAgent");
                    if (userAgent == null) {
                        logger.debug("JavaScript execution returned null userAgent");
                        return false;
                    }
                    
                    // Check if there are any windows/tabs open
                    int windowCount = driver.getWindowHandles().size();
                    if (windowCount == 0) {
                        logger.debug("No windows/tabs found in the browser");
                        return false;
                    }
                    
                    return isDocumentReady;
                } catch (JavascriptException e) {
                    logger.debug("JavaScript execution failed: {}", e.getMessage());
                    // If we can execute JS but get an error, the session is still active
                    return true;
                } catch (UnreachableBrowserException e) {
                    logger.error("Browser is unreachable: {}", e.getMessage());
                    return false;
                } catch (WebDriverException e) {
                    logger.error("WebDriver error during JavaScript execution: {}", e.getMessage());
                    return false;
                }
                
            } finally {
                // Restore original timeout
                try {
                    timeouts.implicitlyWait(originalImplicitWait);
                } catch (Exception e) {
                    logger.warn("Failed to restore original implicit wait: {}", e.getMessage());
                }
            }
            
        } catch (WebDriverException e) {
            if (isConnectionError(e)) {
                logger.debug("WebSocket connection error in session validation: {}", e.getMessage());
            } else {
                logger.debug("WebDriver exception during session validation: {}", e.getMessage());
            }
            return false;
        } catch (Exception e) {
            logger.debug("Unexpected exception during session validation: {}", e.getMessage());
            return false;
        }
    }

    public WebDriver createDriver(TaskType taskType) {
        logger.info("Creating WebDriver for profile from calling method: {}: {}", Thread.currentThread().getName(), taskType.name());
        return getDriver(taskType);
    }

    public JavascriptExecutor getJsExecutor(TaskType taskType) {
        logger.info("Getting JavascriptExecutor for profile from calling method: {}: {}", Thread.currentThread().getName(), taskType.name());
        WebDriver driver = getDriver(taskType);
        if (driver instanceof JavascriptExecutor) {
            return (JavascriptExecutor) driver;
        }
        throw new IllegalStateException("WebDriver instance is not a JavascriptExecutor");
    }

    public ConcurrentMap<TaskType, WebDriver> getProfileDriverMap() {
        return profileDriverMap;
    }

    public ConcurrentMap<TaskType, WebDriver> updateProfileDriverEntry(TaskType taskType, WebDriver driver) {
        profileDriverMap.put(taskType, driver);
        return profileDriverMap;
    }

    /**
     * Configures Chrome extension loading from CRX file.
     * @param options ChromeOptions to add extension to
     */
    private void configureChromeExtension(ChromeOptions options) {
        if (chromeExtensionPath == null || chromeExtensionPath.trim().isEmpty()) {
            return;
        }
        
        // Only load extension if it's required
        if (!chromeExtensionRequired) {
            logger.info("Chrome extension loading is disabled (chromeExtensionRequired=false)");
            return;
        }
        
        try {
            // Try to load from classpath first
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(chromeExtensionPath);
            if (is != null) {
                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("chrome-ext-");
                java.nio.file.Path tempFile = tempDir.resolve("extension.crx");
                java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                is.close();
                options.addExtensions(tempFile.toFile());
                logger.info("Loaded Chrome extension from classpath: {}", chromeExtensionPath);
            } else {
                // Try to load from file system
                File extensionFile = new File(chromeExtensionPath);
                if (extensionFile.exists() && extensionFile.canRead()) {
                    options.addExtensions(extensionFile);
                    logger.info("Loaded Chrome extension from file: {}", chromeExtensionPath);
                } else {
                    throw new IllegalStateException("Required Chrome extension not found: " + chromeExtensionPath);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load Chrome extension: {}", e.getMessage());
            if (chromeExtensionRequired) {
                throw new RuntimeException("Failed to load required Chrome extension", e);
            }
        }
    }

    /**
     * Creates ChromeOptions with settings that make the browser appear more like a human user.
     * Includes various optimizations for stability and automation detection avoidance.
     * @return Configured ChromeOptions instance
     */
    public ChromeOptions getHumanLikeOptions() {
        logger.info("Configuring ChromeOptions with human-like settings");
        ChromeOptions options = new ChromeOptions();
        
        try {
            // Set headless mode if configured
            options.setHeadless(headless);
            
            // Disable automation flags and extensions
            options.setExperimentalOption("excludeSwitches", Arrays.asList(
                "enable-automation",
                "load-extension",
                "enable-logging"
            ));
            options.setExperimentalOption("useAutomationExtension", false);
            
            // Set common preferences
            Map<String, Object> prefs = new HashMap<>();
            // Disable password saving popup
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            // Enable notifications
            prefs.put("profile.default_content_setting_values.notifications", 1);
            // Disable translation prompt
            prefs.put("translate_whitelists", new HashMap<>());
            prefs.put("translate", new HashMap<>());
            // Disable geolocation
            prefs.put("profile.managed_default_content_settings.geolocation", 2);
            // Disable popup blocking
            prefs.put("profile.default_content_setting_values.popups", 0);
            // Disable multiple downloads prompt
            prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
            prefs.put("download.prompt_for_download", false);
            
            options.setExperimentalOption("prefs", prefs);
            
            // Set window size and position
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--window-position=0,0");
            
            // Add common arguments for stability and automation
            List<String> args = new ArrayList<>(Arrays.asList(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-infobars",
                "--disable-notifications",
                "--disable-blink-features=AutomationControlled",
                "--remote-allow-origins=*",
                "--disable-browser-side-navigation",
                "--disable-web-security",
                "--disable-features=IsolateOrigins,site-per-process",
                "--disable-site-isolation-trials",
                "--disable-popup-blocking",
                "--disable-translate",
                "--ignore-certificate-errors",
                "--safebrowsing-disable-download-protection",
                "--safebrowsing-disable-extension-blacklist",
                "--start-maximized",
                "--disable-renderer-backgrounding",
                "--disable-backgrounding-occluded-windows",
                "--disable-background-networking",
                "--no-first-run",
                "--no-default-browser-check",
                "--disable-ipc-flooding-protection"
            ));
            
            // Only add --disable-extensions if no extension is configured
            if (chromeExtensionPath == null || chromeExtensionPath.trim().isEmpty()) {
                args.add("--disable-extensions");
            }
            
            // Add user agent to mimic a real browser
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            args.add("--user-agent=" + userAgent);
            
            // Add all arguments to Chrome options
            options.addArguments(args);
            
            // Add Chrome extension if configured
            configureChromeExtension(options);
            
            // Set Chrome binary path if specified
            if (chromeBinaryPath != null && !chromeBinaryPath.trim().isEmpty()) {
                logger.info("Using Chrome binary at: {}", chromeBinaryPath);
                options.setBinary(chromeBinaryPath);
            }
            
            // Set ChromeDriver path if specified
            if (chromeDriverPath != null && !chromeDriverPath.trim().isEmpty()) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                logger.info("Using ChromeDriver at: {}", chromeDriverPath);
            }
            
            // Set Chrome version if specified
            if (chromeVersion != null && !chromeVersion.trim().isEmpty() && !"latest".equalsIgnoreCase(chromeVersion)) {
                try {
                    WebDriverManager.chromedriver().browserVersion(chromeVersion).proxy("myproxy:8080").timeout(50).setup();
                    logger.info("Using Chrome version: {}", chromeVersion);
                } catch (Exception e) {
                    logger.warn("Failed to set Chrome version to {}: {}", chromeVersion, e.getMessage());
                }
            }
            
            logger.debug("ChromeOptions configured successfully");
            
        } catch (Exception e) {
            logger.error("Error configuring ChromeOptions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure Chrome options", e);
        }
        
        return options;
    }
    
    /**
     * Safely quits and removes a WebDriver instance for the specified task type.
     * @param taskType The task type for which to quit the WebDriver
     */
    public void quitDriver(TaskType taskType) {
        if (taskType == null) {
            logger.warn("Cannot quit driver: TaskType is null");
            return;
        }
        
        WebDriver driver = profileDriverMap.get(taskType);
        if (driver != null) {
            try {
                logger.info("Quitting WebDriver for task type: {}", taskType);
                safeQuitDriver(driver);
                
                // Clean up profile directory if it exists
                String profileDir = profileToPathMap.get(taskType.name());
                if (profileDir != null) {
                    try {
                        FileUtils.deleteDirectory(new File(profileDir));
                        logger.debug("Deleted profile directory: {}", profileDir);
                    } catch (IOException e) {
                        logger.warn("Failed to delete profile directory: {}", profileDir, e);
                    } finally {
                        profileToPathMap.remove(taskType.name());
                    }
                }
                
                // Remove from maps
                profileDriverMap.remove(taskType);
                
                // Clear thread-local if it's the current driver
                WebDriver current = currentDriverInThreadLocal.get();
                if (current == driver) {
                    currentDriverInThreadLocal.remove();
                }
                
                logger.info("Successfully quit WebDriver for task type: {}", taskType);
            } catch (Exception e) {
                logger.error("Error quitting WebDriver for task type: " + taskType, e);
            }
        } else {
            logger.debug("No WebDriver found for task type: {}", taskType);
        }
    }
}
