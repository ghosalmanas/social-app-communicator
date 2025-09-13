package com.wtf.app.util;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maintains active WebDriver sessions to prevent timeouts.
 * Periodically interacts with the page to keep the session alive.
 */
@Component
@EnableScheduling
public class SessionKeepAlive {
    private static final Logger logger = LoggerFactory.getLogger(SessionKeepAlive.class);
    private final Map<String, WebDriver> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> activeFlags = new ConcurrentHashMap<>();
    private static final long KEEP_ALIVE_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes in milliseconds

    /**
     * Register a WebDriver session to be kept alive
     * @param sessionId Unique identifier for the session
     * @param driver WebDriver instance to keep alive
     */
    public void registerSession(String sessionId, WebDriver driver) {
        if (sessionId == null || driver == null) {
            throw new IllegalArgumentException("Session ID and WebDriver cannot be null");
        }
        activeSessions.put(sessionId, driver);
        activeFlags.put(sessionId, new AtomicBoolean(true));
        logger.debug("Registered session for keep-alive: {}", sessionId);
    }

    /**
     * Unregister a WebDriver session
     * @param sessionId The session ID to unregister
     */
    public void unregisterSession(String sessionId) {
        if (sessionId != null) {
            AtomicBoolean flag = activeFlags.get(sessionId);
            if (flag != null) {
                flag.set(false);
            }
            activeSessions.remove(sessionId);
            activeFlags.remove(sessionId);
            logger.debug("Unregistered session from keep-alive: {}", sessionId);
        }
    }

    /**
     * Periodically pings active sessions to keep them alive
     */
    @Scheduled(fixedRate = KEEP_ALIVE_INTERVAL_MS)
    @Async
    public void keepSessionsAlive() {
        activeSessions.forEach((sessionId, driver) -> {
            if (activeFlags.getOrDefault(sessionId, new AtomicBoolean(false)).get()) {
                try {
                    if (driver != null) {
                        // Use JavaScript to interact with the page
                        if (driver instanceof JavascriptExecutor) {
                            // Simple interaction - scroll slightly and back
                            ((JavascriptExecutor) driver).executeScript(
                                "window.scrollBy(0, 10);" +
                                "setTimeout(() => window.scrollBy(0, -10), 100);"
                            );
                            logger.trace("Kept session alive: {}", sessionId);
                        }
                        
                        // For WhatsApp Web, we can also check if we're still logged in
                        checkWhatsAppSession(driver);
                    }
                } catch (Exception e) {
                    logger.warn("Error keeping session alive ({}): {}", sessionId, e.getMessage());
                    // Don't unregister here - let the main application handle reconnection
                }
            }
        });
    }

    /**
     * Check if the WhatsApp Web session is still valid
     */
    private void checkWhatsAppSession(WebDriver driver) {
        try {
            // Check for common WhatsApp Web elements that indicate a valid session
            boolean isLoggedIn = driver.findElements(
                By.xpath("//div[contains(@class, '_1WZqU') or contains(@class, '_3Bc7H')]")).isEmpty();
            
            if (!isLoggedIn) {
                logger.warn("WhatsApp Web session may have been lost");
                // Here you could trigger a reconnection or notification
            }
        } catch (Exception e) {
            logger.debug("Error checking WhatsApp session status: {}", e.getMessage());
        }
    }

    /**
     * Clean up all registered sessions
     */
    public void cleanup() {
        activeSessions.keySet().forEach(this::unregisterSession);
        logger.info("Cleaned up all keep-alive sessions");
    }
}
