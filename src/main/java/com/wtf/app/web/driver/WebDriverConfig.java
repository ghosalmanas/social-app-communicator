package com.wtf.app.web.driver;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Provider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unified WebDriver configuration and management.
 * Handles both regular and headless WebDriver instances with connection pooling.
 * Manages async execution and proper resource cleanup.
 */
//@Configuration
//@EnableAsync
//@EnableScheduling
//@EnableConfigurationProperties
public class WebDriverConfig implements DisposableBean, ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);
    
    // Configuration properties with defaults
    @Value("${selenium.chrome.extension.path:}")
    private String chromeExtensionPath;

    @Value("${selenium.chrome.extension.required:false}")
    private boolean chromeExtensionRequired;

    @Value("classpath:${selenium.chrome.extension.path:}")
    private Resource chromeExtensionResource;
    
    @Value("${webdriver.max.concurrent.instances:5}")
    private int maxConcurrentDrivers;
    
    @Value("${webdriver.async.core.pool.size:10}")
    private int corePoolSize;
    
    @Value("${webdriver.async.max.pool.size:10}")
    private int maxPoolSize;
    
    @Value("${webdriver.async.queue.capacity:25}")
    private int queueCapacity;

    @Value("${selenium.webdriver.path:#{null}}")
    private String webDriverPath;

    @Value("${selenium.headless:false}")
    private boolean headless;

    @Value("${selenium.page.load.timeout.seconds:30}")
    private int pageLoadTimeout;

    @Value("${selenium.implicit.wait.seconds:10}")
    private int implicitWait;


    // WebDriver pool management
    private BlockingQueue<WebDriver> driverPool;
    private final AtomicInteger activeBrowsers = new AtomicInteger(0);
    private final Set<WebDriver> allDrivers = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean shuttingDown = false;
    private static final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        this.driverPool = new LinkedBlockingQueue<>(maxConcurrentDrivers);
        logger.info("Initializing WebDriver pool with max {} instances", maxConcurrentDrivers);
        
        // Initialize core pool
        for (int i = 0; i < corePoolSize; i++) {
            createAndAddDriverToPool();
        }
        
        logger.info("WebDriver pool initialized with {} instances", driverPool.size());
    }


    /**
     * Primary WebDriver bean for general use.
     * Uses prototype scope with TARGET_CLASS proxy mode to support async operations.
     */
    @Bean
    @Primary
    @Scope(scopeName = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public WebDriver webDriver() {
        return getWebDriver(false);
    }
    

    /**
     * Headless WebDriver for background tasks.
     */
    /*@Bean(name = "headlessDriver")
    @Scope(scopeName = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public WebDriver headlessDriver() {
        return getWebDriver(true);
    }
    */

    /**
     * Chrome options configuration with common settings.
     */
    @Bean
    public ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        // Disable automation flags that might trigger bot detection
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);


        // Common Chrome options
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--window-size=1920,1080",
            "--disable-blink-features=AutomationControlled"
        );
        
        // Add Chrome extension if configured
        configureChromeExtension(options);
        
        return options;
    }
    
    /**
     * Task executor for async operations.
     */
    //@Bean(name = "webDriverTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("WebDriver-Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * WebDriverWait configuration.
     */
    @Bean
    @Scope("prototype")
    public WebDriverWait webDriverWait1(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(30));
    }
    
    /**
     * JavaScript executor for the current WebDriver.
     */
    //@Bean
    @Scope(scopeName = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JavascriptExecutor javascriptExecutor1(WebDriver driver) {
        return (JavascriptExecutor) driver;
    }

    private void configureChromeExtension(ChromeOptions options) {
        if (!StringUtils.hasText(chromeExtensionPath)) {
            return;
        }
        
        try {
            if (chromeExtensionResource.exists() && chromeExtensionResource.isReadable()) {
                // For classpath resources
                try (InputStream is = chromeExtensionResource.getInputStream()) {
                    Path tempDir = Files.createTempDirectory("chrome-ext-");
                    Path tempFile = tempDir.resolve("extension.crx");
                    Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    options.addExtensions(tempFile.toFile());
                    logger.info("Loaded Chrome extension from classpath: {}", chromeExtensionPath);
                }
            } else {
                // For file system paths
                File extensionFile = new File(chromeExtensionPath);
                if (extensionFile.exists() && extensionFile.canRead()) {
                    options.addExtensions(extensionFile);
                    logger.info("Loaded Chrome extension from file: {}", chromeExtensionPath);
                } else if (chromeExtensionRequired) {
                    throw new IllegalStateException("Required Chrome extension not found: " + chromeExtensionPath);
                } else {
                    logger.warn("Chrome extension not found: {}", chromeExtensionPath);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load Chrome extension: {}", e.getMessage());
        }
    }

    private void createAndAddDriverToPool() {
        if (activeBrowsers.get() >= maxConcurrentDrivers) {
            logger.warn("Maximum number of WebDriver instances ({}) reached", maxConcurrentDrivers);
            return;
        }
        
        try {
            ChromeOptions options = getHumanLikeOptions();
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            
            allDrivers.add(driver);
            activeBrowsers.incrementAndGet();
            driverPool.offer(driver);
            
            logger.debug("Added new WebDriver to pool. Active instances: {}/{}", 
                activeBrowsers.get(), maxConcurrentDrivers);
        } catch (Exception e) {
            logger.error("Failed to create WebDriver instance", e);
            throw new RuntimeException("Failed to create WebDriver instance", e);
        }
    }



    public static ChromeOptions getHumanLikeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Common arguments for a more "human" appearance
        options.addArguments("--start-maximized");
        options.addArguments("--window-size=1920,1080"); // Or other common resolutions
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        //options.addArguments("--incognito");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"); // Example user agent
        options.addArguments("--lang=en-US");

        // Potentially helpful, but consider the implications
        // options.addArguments("--disable-gpu");
        // options.addArguments("--no-sandbox"); // Use with caution

        // Experimental options
        options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // To use a custom user profile (replace with your profile path)
        // options.addArguments("user-data-dir=/path/to/your/custom/profile");

        /*Own Features --uncomment if required*/
        /*Path path = Paths.get("src/resources/extensions/webextensions-selenium-example.crx");//
        File extensionFilePath = new File(path.toUri());
        options.addExtensions(extensionFilePath);*/

		/*
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);

		options.merge(capabilities);
		*/

        return options;
    }



    private WebDriver getWebDriver(boolean headless) {
        if (shuttingDown) {
            throw new IllegalStateException("WebDriver manager is shutting down");
        }

        // Check ThreadLocal first for request-scoped WebDriver
        WebDriver driver = threadLocalDriver.get();
        if (driver != null) {
            try {
                // Basic health check
                driver.getCurrentUrl();
                return driver;
            } catch (Exception e) {
                logger.warn("Existing WebDriver session is invalid, creating new one", e);
                closeDriver((ChromeDriver) driver);
                threadLocalDriver.remove();
            }
        }

        // Get from pool or create new
        try {
            driver = driverPool.poll(30, TimeUnit.SECONDS);
            
            if (driver == null) {
                if (activeBrowsers.get() < maxConcurrentDrivers) {
                    createAndAddDriverToPool();
                    driver = driverPool.poll(30, TimeUnit.SECONDS);
                } else {
                    throw new IllegalStateException("Timeout waiting for available WebDriver instance");
                }
            }
            
            // Store in ThreadLocal for request scope
            threadLocalDriver.set(driver);
            
            logger.debug("Acquired WebDriver from pool. Active instances: {}/{}", 
                activeBrowsers.get(), maxConcurrentDrivers);
                
            return driver;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for WebDriver instance", e);
        } catch (Exception e) {
            logger.error("Failed to get WebDriver instance", e);
            throw new RuntimeException("Failed to get WebDriver instance", e);
        }
    }

    /**
     * Returns a WebDriver instance to the pool.
     */
    public void returnDriver(WebDriver driver) {
        if (driver == null || shuttingDown) {
            return;
        }
        
        try {
            // Clear cookies and local storage between uses
            driver.manage().deleteAllCookies();
            
            // Reset the session by navigating to about:blank
            driver.get("about:blank");
            
            // Clear any alerts that might be present
            try {
                driver.switchTo().alert().dismiss();
            } catch (Exception e) {
                // No alert present, ignore
            }
            
            // Return to pool if not at capacity
            if (driverPool.size() < maxConcurrentDrivers) {
                if (!driverPool.offer(driver)) {
                    logger.warn("Failed to return WebDriver to pool, closing it");
                    closeDriver((ChromeDriver) driver);
                }
            } else {
                logger.debug("Pool at capacity, closing WebDriver instance");
                closeDriver((ChromeDriver) driver);
            }
            
            // Clear thread local
            threadLocalDriver.remove();
            
        } catch (Exception e) {
            logger.error("Error returning WebDriver to pool", e);
            closeDriver((ChromeDriver) driver);
        }
    }
    
    private void closeDriver(ChromeDriver driver) {
        if (driver != null) {
            try {
                allDrivers.remove(driver);
                driver.quit();
                activeBrowsers.decrementAndGet();
                logger.debug("Closed WebDriver instance. Active instances: {}/{}", 
                    activeBrowsers.get(), maxConcurrentDrivers);
            } catch (Exception e) {
                logger.warn("Error closing WebDriver instance", e);
            }
        }
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void destroy() {
        shuttingDown = true;
        logger.info("Shutting down WebDriver manager...");
        
        // Clean up thread local
        threadLocalDriver.remove();
        
        // Close all drivers
        allDrivers.forEach(driver -> {
            if (driver instanceof ChromeDriver) {
                closeDriver((ChromeDriver) driver);
            } else if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("Error closing WebDriver instance", e);
                }
            }
        });
        
        allDrivers.clear();
        if (driverPool != null) {
            driverPool.clear();
        }
        activeBrowsers.set(0);
        logger.info("WebDriver manager shutdown complete. Active instances: {}", activeBrowsers.get());
    }

    /*@Bean
    @Qualifier("chromeDriver")
    public Provider<WebDriver> chromeDriverProvider() {
        return () -> getWebDriver(false);
    }
*/

    /**
     * Get the current count of active browsers.
     */
    public int getActiveBrowsersCount() {
        return activeBrowsers.get();
    }
    
    /**
     * Get the maximum number of concurrent WebDriver instances.
     */
    public int getMaxConcurrentDrivers() {
        return maxConcurrentDrivers;
    }
}
