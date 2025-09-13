package com.wtf.app.service.base;

import org.openqa.selenium.WebDriver;

/**
 * Interface for services that need access to a WebDriver instance.
 */
public interface WebDriverAware {
    
    /**
     * Set the WebDriver instance for this service.
     * 
     * @param webDriver The WebDriver instance to use
     */
    void setWebDriver(WebDriver webDriver);
}
