package com.wtf.app;

import com.wtf.app.util.ConcurrencyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles application shutdown and cleanup of resources.
 */
@Component
public class ApplicationShutdownHook implements ApplicationListener<ContextStoppedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationShutdownHook.class);

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        logger.info("Application is shutting down. Performing cleanup...");
        
        // Shutdown ConcurrencyUtils
        ConcurrencyUtils.shutdown();

        try {
            Runtime.getRuntime().exec(
                    //kill java processes
                    "pkill -f java");
            //kill chrome processes
            Runtime.getRuntime().exec(
                    "pkill -f chrome");
            //kill chromedriver processes
            Runtime.getRuntime().exec(
                    "pkill -f chromedriver");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        
        // Add any additional cleanup tasks here
        
        logger.info("Cleanup completed. Application is ready to shut down.");
    }
}
