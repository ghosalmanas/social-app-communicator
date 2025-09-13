package com.wtf.app.web.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;

/**
 * Manages WebDriver instances with proper lifecycle management.
 */
@Component("legacyWebDriverManager")
@Deprecated
public class WebDriverManager {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverManager.class);
    
    @Value("${selenium.webdriver.path:#{null}}")
    private String webDriverPath;
    
    @Value("${selenium.headless:false}")
    private boolean headless;
    
    @Value("${selenium.page.load.timeout.seconds:60}")
    private int pageLoadTimeout;
    
    @Value("${selenium.implicit.wait.seconds:10}")
    private int implicitWait;
    
    private ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();
    
    /**
     * Gets a WebDriver instance for the current thread.
     * Creates a new instance if one doesn't exist.
     */
    public WebDriver getWebDriver() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver == null) {
            driver = createWebDriver();
            webDriverThreadLocal.set(driver);
        }
        return driver;
    }
    
    /**
     * Creates a new WebDriver instance with configured options.
     */
    private WebDriver createWebDriver() {
        if (webDriverPath != null) {
            System.setProperty("webdriver.chrome.driver", webDriverPath);
            logger.info("Using ChromeDriver from: {}", webDriverPath);
        } else {
            logger.warn("No ChromeDriver path specified. Ensure 'selenium.webdriver.path' is set in application.properties");
        }
        
        ChromeOptions options = new ChromeOptions();
        
        // Configure headless mode
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            logger.info("Running Chrome in headless mode");
        }
        
        // Common Chrome options
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--window-size=1920,1080",
            "--disable-extensions",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--disable-blink-features=AutomationControlled"
        );
        
        // Mobile emulation for WhatsApp Web
      /*  Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "iPhone X");
        options.setExperimentalOption("mobileEmulation", mobileEmulation);*/
        
        // Initialize WebDriver
        WebDriver driver = new ChromeDriver(options);
        
        // Configure timeouts
        driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
        
        logger.info("Created new WebDriver instance with pageLoadTimeout={}s, implicitWait={}s", 
                   pageLoadTimeout, implicitWait);
        
        return driver;
    }
    
    /**
     * Quits the WebDriver instance for the current thread.
     */
    public void quitWebDriver() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("Successfully quit WebDriver instance");
            } catch (Exception e) {
                logger.error("Error quitting WebDriver: {}", e.getMessage(), e);
            } finally {
                webDriverThreadLocal.remove();
            }
        }
    }
    
    /**
     * Cleans up all WebDriver instances when the application shuts down.
     */
    @PreDestroy
    public void cleanup() {
        quitWebDriver();
    }
}
