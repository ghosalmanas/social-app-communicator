package com.wtf.app.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Utility class for WebDriver operations with explicit waits and error handling.
 */
public class WebDriverUtils {
    private static final Logger log = LoggerFactory.getLogger(WebDriverUtils.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private static final long POLLING_INTERVAL_MS = 500;

    private WebDriverUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Navigate to a URL with explicit wait for page load
     * @param driver The WebDriver instance
     * @param url The URL to navigate to
     * @param timeoutSeconds Maximum time to wait for page load
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateTo(WebDriver driver, String url, long timeoutSeconds) {
        if (driver == null || url == null || url.trim().isEmpty()) {
            log.warn("Invalid parameters for navigation - driver: {}, url: {}", 
                    driver != null ? "not null" : "null", url);
            return false;
        }

        try {
            log.info("Navigating to: {}", url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            
            // Navigate to the URL
            driver.get(url);
            
            // Wait for document.readyState to be complete
            wait.until(webDriver -> 
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            
            log.debug("Successfully navigated to: {}", url);
            return true;
            
        } catch (TimeoutException e) {
            log.error("Timeout waiting for page to load: {}", url, e);
        } catch (Exception e) {
            log.error("Error navigating to {}: {}", url, e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Navigate to a URL with default timeout
     */
    public static boolean navigateTo(WebDriver driver, String url) {
        return navigateTo(driver, url, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Wait for an element to be present and visible
     * @param driver The WebDriver instance
     * @param by The locator to find the element
     * @param timeoutSeconds Maximum time to wait
     * @return The WebElement if found and visible, null otherwise
     */
    public static WebElement waitForElement(WebDriver driver, By by, long timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds), 
                                                 Duration.ofMillis(POLLING_INTERVAL_MS));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (Exception e) {
            log.debug("Element not found or not visible within {}s: {}", timeoutSeconds, by);
            return null;
        }
    }

    /**
     * Wait for all elements matching the locator to be present and visible
     */
    public static List<WebElement> waitForElements(WebDriver driver, By by, long timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds),
                                                 Duration.ofMillis(POLLING_INTERVAL_MS));
            return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
        } catch (Exception e) {
            log.debug("No elements found matching {} within {}s: {}", by, timeoutSeconds, e.getMessage());
            return List.of();
        }
    }

    /**
     * Check if an element is present and visible
     */
    public static boolean isElementPresent(WebDriver driver, By by) {
        try {
            return driver.findElement(by).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely get the current URL
     */
    public static String getCurrentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            log.warn("Failed to get current URL: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Execute JavaScript and return the result
     */
    public static Object executeScript(WebDriver driver, String script, Object... args) {
        try {
            return ((JavascriptExecutor) driver).executeScript(script, args);
        } catch (Exception e) {
            log.error("Error executing JavaScript: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Wait for a condition to be true
     */
    public static <T> T waitForCondition(WebDriver driver, java.util.function.Function<WebDriver, T> condition, 
                                        long timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds),
                                                 Duration.ofMillis(POLLING_INTERVAL_MS));
            return wait.until(condition);
        } catch (Exception e) {
            log.debug("Condition not met within {}s: {}", timeoutSeconds, e.getMessage());
            return null;
        }
    }

    /**
     * Wait for an alert to be present
     */
    public static boolean waitForAlert(WebDriver driver, long timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return wait.until(ExpectedConditions.alertIsPresent()) != null;
        } catch (Exception e) {
            log.debug("No alert found within {}s: {}", timeoutSeconds, e.getMessage());
            return false;
        }
    }
}
