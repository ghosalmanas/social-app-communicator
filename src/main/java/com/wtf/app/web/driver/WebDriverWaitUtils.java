package com.wtf.app.web.driver;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for WebDriver wait operations with caching support.
 * Replaces Thread.sleep() with explicit waits for better performance.
 */
public class WebDriverWaitUtils {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverWaitUtils.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private static final long POLLING_INTERVAL_MS = 500;
    
    // Simple cache for elements that are frequently accessed
    private final Map<String, WebElement> elementCache = new ConcurrentHashMap<>();
    private final WebDriver driver;

    public WebDriverWaitUtils(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Wait for an element to be present in the DOM and visible.
     * @param locator The By locator for the element
     * @param timeoutSeconds Maximum time to wait in seconds
     * @return The WebElement once found
     */
    public WebElement waitForElement(By locator, long timeoutSeconds) {
        String cacheKey = locator.toString();
        return elementCache.computeIfAbsent(cacheKey, k -> {
            WebElement element = waitFor(ExpectedConditions.visibilityOfElementLocated(locator), timeoutSeconds);
            return element;
        });
    }

    /**
     * Wait for an element to be clickable.
     * @param locator The By locator for the element
     * @param timeoutSeconds Maximum time to wait in seconds
     * @return The clickable WebElement
     */
    public WebElement waitForClickable(By locator, long timeoutSeconds) {
        String cacheKey = "clickable_" + locator.toString();
        return elementCache.computeIfAbsent(cacheKey, k -> 
            waitFor(ExpectedConditions.elementToBeClickable(locator), timeoutSeconds)
        );
    }

    /**
     * Wait for a condition to be true with a default timeout.
     */
    public <T> T waitFor(Function<WebDriver, T> condition) {
        return waitFor(condition, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Wait for a condition to be true with a custom timeout.
     */
    public <T> T waitFor(Function<WebDriver, T> condition, long timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 
                Duration.ofSeconds(timeoutSeconds), 
                Duration.ofMillis(POLLING_INTERVAL_MS));
            return wait.ignoring(StaleElementReferenceException.class)
                      .ignoring(NoSuchElementException.class)
                      .until(condition);
        } catch (TimeoutException e) {
            logger.warn("Timeout waiting for condition: {}", condition);
            throw e;
        }
    }

    /**
     * Wait for a condition to be true with retry logic.
     */
    public <T> T waitWithRetry(Supplier<T> action, String operation, int maxRetries) {
        int attempt = 0;
        Exception lastError = null;
        
        while (attempt < maxRetries) {
            try {
                return action.get();
            } catch (Exception e) {
                lastError = e;
                attempt++;
                long backoff = (long) (Math.pow(2, attempt) * 100 + Math.random() * 1000);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted", ie);
                }
            }
        }
        throw new RuntimeException("Operation failed after " + maxRetries + " attempts: " + operation, lastError);
    }

    /**
     * Clear the element cache.
     */
    public void clearCache() {
        elementCache.clear();
    }

    /**
     * Clear a specific element from the cache.
     */
    public void clearFromCache(By locator) {
        elementCache.remove(locator.toString());
        elementCache.remove("clickable_" + locator.toString());
    }

    /**
     * Wait for page to load completely.
     */
    public void waitForPageLoad() {
        waitFor(driver -> {
            String state = (String) ((JavascriptExecutor) driver)
                .executeScript("return document.readyState");
            return state.equals("complete");
        }, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Wait for an element to be stale (useful for detecting DOM changes).
     */
    public void waitForStaleness(WebElement element) {
        waitFor(ExpectedConditions.stalenessOf(element));
    }
}
