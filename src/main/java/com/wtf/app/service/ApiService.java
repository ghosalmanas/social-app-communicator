package com.wtf.app.service;

import com.wtf.app.web.driver.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class to handle API operations with proper WebDriver management.
 */
@Service
public class ApiService {
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    
    private final WebDriverManager webDriverManager;
    private final int explicitWaitSeconds;
    
    @Autowired
    public ApiService(WebDriverManager webDriverManager, 
                     @Value("${selenium.explicit.wait.seconds:10}") int explicitWaitSeconds) {
        this.webDriverManager = webDriverManager;
        this.explicitWaitSeconds = explicitWaitSeconds;
    }
    
    /**
     * Performs a health check of the WebDriver.
     */
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Executes a WebDriver operation with proper error handling.
     */
    public <T> T executeWithWebDriver(WebDriverOperation<T> operation, String operationName) {
        WebDriver driver = null;
        try {
            driver = webDriverManager.getWebDriver();
            return operation.execute(driver, new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds)));
        } catch (Exception e) {
            logger.error("Error during {}: {}", operationName, e.getMessage(), e);
            
            // Take screenshot on error if enabled
            try {
                // You can implement screenshot logic here if needed
                // String screenshotPath = takeScreenshot(driver, operationName);
                // logger.info("Screenshot saved to: {}", screenshotPath);
            } catch (Exception screenshotEx) {
                logger.error("Failed to take screenshot: {}", screenshotEx.getMessage());
            }
            
            throw new RuntimeException("Failed to execute " + operationName + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Functional interface for WebDriver operations.
     */
    @FunctionalInterface
    public interface WebDriverOperation<T> {
        T execute(WebDriver driver, WebDriverWait wait) throws Exception;
    }
    
    /**
     * Helper method to wait for an element to be visible.
     */
    public WebElement waitForElement(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Helper method to wait for an element to be clickable.
     */
    public WebElement waitForClickable(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
            .until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Creates a standardized success response.
     */
    public static ResponseEntity<Map<String, Object>> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        if (data != null) {
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Creates a standardized error response.
     */
    public static ResponseEntity<Map<String, Object>> createErrorResponse(String message, Throwable throwable, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("error", throwable != null ? throwable.getMessage() : "Unknown error");
        response.put("timestamp", System.currentTimeMillis());
        
        if (throwable != null && throwable.getCause() != null) {
            response.put("cause", throwable.getCause().getMessage());
        }
        
        return ResponseEntity.status(status).body(response);
    }
}
