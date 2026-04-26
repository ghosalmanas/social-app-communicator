package com.wtf.app.listener;

import com.wtf.app.controller.CommonController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener to automatically start automation tasks after application startup
 * based on configuration flags in application.properties.
 */
@Component
public class AutomationStartupListener {
    private static final Logger logger = LogManager.getLogger(AutomationStartupListener.class);

    @Autowired
    private CommonController commonController;

    @Value("${automation.auto.start.enabled:false}")
    private boolean autoStartEnabled;

    /**
     * Automatically starts automation tasks after the application is fully ready.
     * This event is fired when the application is ready to service requests.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application is ready. Checking if auto-start is enabled...");

        if (!autoStartEnabled) {
            logger.info("Auto-start is disabled. Skipping automation startup.");
            return;
        }

        logger.info("Auto-start is enabled. Invoking automation endpoint...");

        try {
            // Call the existing controller endpoint to start all automations
            commonController.startAllAutomations();
            logger.info("Automation tasks started successfully on application startup");
        } catch (Exception e) {
            logger.error("Failed to start automation tasks on application startup", e);
        }
    }
}
