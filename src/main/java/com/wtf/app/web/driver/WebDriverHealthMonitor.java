package com.wtf.app.web.driver;

import com.wtf.app.model.enums.TaskType;
import jakarta.annotation.PreDestroy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.wtf.app.web.driver.event.WebDriverHealthEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors and maintains the health of WebDriver instances.
 * Provides automatic recovery for failed sessions and periodic health checks.
 */
@Service
@EnableScheduling
public class WebDriverHealthMonitor implements WebDriverHealthService {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverHealthMonitor.class);
    
    private final Map<TaskType, AtomicBoolean> activeSessions = new ConcurrentHashMap<>();
    private final Map<TaskType, Long> lastActivityTime = new ConcurrentHashMap<>();
    private final ParallelWebDriverManager driverManager;
    private final ApplicationEventPublisher eventPublisher;
    
    // Configuration
    private static final long HEALTH_CHECK_INTERVAL_MS = 60000; // 1 minute
    private static final long INACTIVITY_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(30); // 30 minutes
    private static final long SESSION_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15); // 15 minutes
    
    @Autowired
    public WebDriverHealthMonitor(ParallelWebDriverManager driverManager, ApplicationEventPublisher eventPublisher) {
        this.driverManager = driverManager;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Registers a WebDriver session for health monitoring.
     * @param taskType The profile name associated with the WebDriver
     */
    public void registerSession(TaskType taskType) {
        activeSessions.put(taskType, new AtomicBoolean(true));
        updateActivityTime(taskType);
        logger.info("Registered session for health monitoring: {}", taskType.name());
    }
    
    /**
     * Unregisters a WebDriver session from health monitoring.
     * @param taskType The profile name to unregister
     */
    @Override
    public void unregisterSession(TaskType taskType) {
        activeSessions.remove(taskType);
        lastActivityTime.remove(taskType);
        logger.debug("Unregistered session from health monitoring: {}", taskType.name());
    }
    
    /**
     * Updates the last activity time for a session.
     * @param taskType The profile name to update
     */
    @Override
    public void updateActivityTime(TaskType taskType) {
        lastActivityTime.put(taskType, System.currentTimeMillis());
    }
    
    /**
     * Periodically checks the health of active WebDriver sessions.
     */
    @Scheduled(fixedRate = HEALTH_CHECK_INTERVAL_MS)
    @Async
    public void performHealthChecks() {
        activeSessions.forEach((profileName, isActive) -> {
            if (isActive.get()) {
                try {
                    checkSessionHealth(profileName);
                } catch (Exception e) {
                    logger.error("Error during health check for profile: " + profileName, e);
                }
            }
        });
    }
    
    /*
     * Performs health check on a single WebDriver session.
     */
    /**
     * Validates if a WebDriver instance is still active and responsive.
     * @param driver The WebDriver instance to validate
     * @return true if the driver is valid and responsive, false otherwise
     */
    private boolean isDriverValid(WebDriver driver) {
        if (driver == null) {
            return false;
        }

        try {
            // Check if we can get the current URL (basic session check)
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl == null) {
                logger.debug("Current URL is null");
                return false;
            }
            
            // Check if the WebDriver can execute JavaScript
            try {
                Object result = ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
                if (result == null) {
                    logger.debug("JavaScript execution returned null");
                    return false;
                }
            } catch (Exception e) {
                logger.debug("JavaScript execution failed: {}", e.getMessage());
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.debug("Driver validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean checkSessionHealth(TaskType taskType) {
        String profileName = taskType.name();
        // First check if we have a registered session
        if (!activeSessions.containsKey(taskType)) {
            logger.debug("No active session found for profile: {}", profileName);
            return false;
        }
        
        // Get the driver directly from the profile map to avoid circular dependency
        Map<TaskType, WebDriver> profileDriverMap = driverManager.getProfileDriverMap();
        WebDriver driver = profileDriverMap.get(taskType);
        
        if (driver == null) {
            logger.warn("Driver not found in profile map for: {}", profileName);
            return false;
        }
        
        // Perform the health check using our direct validation
        boolean isValid = isDriverValid(driver);
        logger.debug("Health check for profile {}: {}", profileName, isValid ? "HEALTHY" : "UNHEALTHY");
        
        if (!isValid) {
            logger.warn("WebDriver session for profile {} is no longer valid", profileName);
            // Trigger cleanup for the invalid session
            cleanupInvalidSession(taskType, driver);
        } else {
            // Update last activity time for valid sessions
            updateActivityTime(taskType);
        }
        
        return isValid;
    }
    
    /**
     * Cleans up an invalid WebDriver session.
     * @param taskType The profile name associated with the session
     * @param driver The WebDriver instance to clean up
     */
    private void cleanupInvalidSession(TaskType taskType, WebDriver driver) {
        String profileName = taskType.name();
        logger.info("Cleaning up invalid session for profile: {}", taskType.name());
        try {
            // Remove from active sessions
            activeSessions.remove(taskType);
            lastActivityTime.remove(taskType);
            
            // Try to properly quit the driver
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("Error while quitting WebDriver for profile {}: {}", profileName, e.getMessage());
                }
            }
            
            // Notify listeners about the invalid session
            eventPublisher.publishEvent(new WebDriverHealthEvent(this, profileName, false, "Session became invalid"));
            
        } catch (Exception e) {
            logger.error("Error during cleanup of invalid session for profile {}: {}", profileName, e.getMessage(), e);
        }
    }
    
    private void performHealthCheck(TaskType taskType) {
        String profileName = taskType.name();

        try {
            WebDriver driver = driverManager.getDriver(taskType);
            if (driver == null) {
                logger.warn("Driver not found for profile: {}", profileName);
                return;
            }
            
            // Check if session is still valid
            if (!isSessionValid(driver)) {
                logger.warn("Session invalid for profile: {}. Attempting recovery...", profileName);
                recoverSession(taskType);
                return;
            }
            
            // Check if session needs refresh
            long lastActive = lastActivityTime.getOrDefault(taskType, 0L);
            if (System.currentTimeMillis() - lastActive > SESSION_REFRESH_INTERVAL_MS) {
                logger.info("Refreshing session for profile: {}", profileName);
                refreshSession(taskType);
            }
            
        } catch (Exception e) {
            logger.error("Error checking session health for profile: " + profileName, e);
            recoverSession(taskType);
        }
    }
    
    /**
     * Verifies if a WebDriver session is still valid.
     */
    private boolean isSessionValid(WebDriver driver) {
        try {
            // Try to execute a simple JavaScript to verify session
            ((JavascriptExecutor) driver).executeScript("return document.readyState;");
            
            // Check for common WhatsApp Web elements
            return driver.findElements(
                org.openqa.selenium.By.xpath("//div[contains(@class, '_1WZqU') or contains(@class, '_3Bc7H')]")).isEmpty();
        } catch (Exception e) {
            logger.debug("Session validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Attempts to recover a failed WebDriver session.
     */
    @Override
    public boolean recoverSession(TaskType taskType) {
        String profileName = taskType.name();
        try {
            logger.info("Attempting to recover session for profile: {}", profileName);
            
            // Get the current driver and quit it
            WebDriver oldDriver = driverManager.getDriver(taskType);
            if (oldDriver != null) {
                try {
                    oldDriver.quit();
                } catch (Exception e) {
                    logger.warn("Error quitting old driver: {}", e.getMessage());
                }
            }
            
            // Remove the old driver from the manager
            driverManager.quitDriver(taskType);
            
            // Create a new driver instance
            WebDriver newDriver = driverManager.getDriver(taskType);
            if (newDriver != null) {
                logger.info("Successfully recovered session for profile: {}", profileName);
                updateActivityTime(taskType);
                return true;
            } else {
                logger.error("Failed to recover session for profile: {}", profileName);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error recovering session for profile: " + profileName, e);
            return false;
        }
    }
    
    private void recoverSessionInternal(TaskType taskType) {
        String profileName = taskType.name();
        try {
            logger.info("Attempting to recover session for profile: {}", profileName);
            
            // Get the current driver and quit it
            WebDriver oldDriver = driverManager.getDriver(taskType);
            if (oldDriver != null) {
                try {
                    oldDriver.quit();
                } catch (Exception e) {
                    logger.warn("Error quitting old driver: {}", e.getMessage());
                }
            }
            
            // Remove the old driver from the manager
            driverManager.quitDriver(taskType);
            
            // Create a new driver instance
            WebDriver newDriver = driverManager.getDriver(taskType);
            if (newDriver != null) {
                logger.info("Successfully recovered session for profile: {}", profileName);
                updateActivityTime(taskType);
            } else {
                logger.error("Failed to recover session for profile: {}", profileName);
            }
        } catch (Exception e) {
            logger.error("Error recovering session for profile: " + profileName, e);
        }
    }
    
    /**
     * Refreshes a WebDriver session to prevent timeouts.
     */
    @Override
    public void refreshSession(TaskType taskType) {
        String profileName = taskType.name();
        WebDriver driver = driverManager.getDriver(taskType);
        if (driver == null) {
            logger.warn("Cannot refresh session - driver not found for profile: {}", profileName);
            return;
        }
        try {
            logger.debug("Refreshing session for profile: {}", profileName);
            
            // Navigate to about:blank first to clear any modals/dialogs
            driver.navigate().to("about:blank");
            
            // Navigate back to WhatsApp Web
            driver.navigate().to("https://web.whatsapp.com/");
            
            // Wait for page to load
            Thread.sleep(2000);
            
            updateActivityTime(taskType);
            logger.info("Successfully refreshed session for profile: {}", profileName);
        } catch (Exception e) {
            logger.error("Error refreshing session for profile: " + profileName, e);
            recoverSession(taskType);
        }
    }
    
    /**
     * Cleans up inactive sessions.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    @Async
    public void cleanupInactiveSessions() {
        long currentTime = System.currentTimeMillis();
        lastActivityTime.forEach((taskType, lastActive) -> {
            if (currentTime - lastActive > INACTIVITY_THRESHOLD_MS) {
                logger.info("Cleaning up inactive session: {}", taskType.name());
                unregisterSession(taskType);
                driverManager.quitDriver(taskType);
            }
        });
    }
    
    /**
     * Cleans up all registered sessions.
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Shutting down WebDriverHealthMonitor");
        activeSessions.clear();
        lastActivityTime.clear();
    }
}
