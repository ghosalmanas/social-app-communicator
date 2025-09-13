package com.wtf.app.model.config;

import org.openqa.selenium.WebDriver;

/**
 * Interface for managing WebDriver instances with pooling support.
 */
public interface WebDriverManager {
    /**
     * Gets a WebDriver instance from the pool or creates a new one if under the limit.
     * @return WebDriver instance
     * @throws InterruptedException if interrupted while waiting for a WebDriver
     */
    WebDriver getWebDriver() throws InterruptedException;
    
    /**
     * Returns a WebDriver instance to the pool.
     * @param driver The WebDriver to return to the pool
     */
    void returnDriver(WebDriver driver);
    
    /**
     * Shuts down the WebDriver pool and cleans up resources.
     */
    void shutdown();
}
