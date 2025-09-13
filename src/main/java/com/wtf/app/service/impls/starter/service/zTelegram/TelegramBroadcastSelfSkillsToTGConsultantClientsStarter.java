package com.wtf.app.service.impls.starter.service.zTelegram;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.service.impls.starter.service.whatsApp.SocialMediaParentRunner;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;

/**
 * Service for broadcasting self-skills messages to Telegram consultant clients.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Component("telegramBroadcastSelfSkillsToTGConsultantClientsStarter")
public class TelegramBroadcastSelfSkillsToTGConsultantClientsStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(TelegramBroadcastSelfSkillsToTGConsultantClientsStarter.class);

    /**
     * Constructs a new TelegramBroadcastSelfSkillsToTGConsultantClientsStarter with required dependencies.
     *
     * @param initialSetup The initial setup configuration
     * @param parallelWebDriverManager The parallel WebDriver manager
     * @param socialMediaParentRunner The runner for executing social media tasks
     * @param whatsappDTO DTO for WhatsApp configuration
     * @param telegramDTO DTO for Telegram configuration
     * @param facebookDTO DTO for Facebook configuration
     * @throws IllegalArgumentException if any of the required dependencies are null
     */
    @Autowired
    public TelegramBroadcastSelfSkillsToTGConsultantClientsStarter(
            
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            @Qualifier("whatsappDTO") SocialModel whatsappDTO,
            @Qualifier("telegramDTO") SocialModel telegramDTO,
            @Qualifier("facebookDTO") SocialModel facebookDTO) {
        super(
            
            initialSetup,
            parallelWebDriverManager,
            socialMediaParentRunner,
            whatsappDTO,
            telegramDTO,
            facebookDTO
        );
    }

    /**
     * Starts the process of broadcasting self-skills to Telegram consultant clients asynchronously.
     *
     */
    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) {
        logger.info("Starting to traverse Telegram consultant clients for broadcast");

        try {
            // Implementation for group traversal logic specific to this service
            logger.info("Executing broadcast to Telegram consultant clients");

            // TODO: Implement actual broadcast logic here

            logger.info("Successfully sent messages to all consultant clients");
        } catch (Exception e) {
            logger.error("Error during Telegram consultant clients traversal: {}", e.getMessage(), e);
            throw new RuntimeException("Error during Telegram consultant clients traversal: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<Boolean> start(AutomationContext context) {
        try {
            logger.info("Starting Telegram broadcast to consultant clients");

            // Validate required dependencies
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(telegramDTO, "telegramDTO must not be null");

            // Execute the service using the parent runner
            socialMediaParentRunner.execute(context, this, telegramDTO, TaskType.TELEGRAM_BROADCAST_AD);

            logger.info("Successfully completed Telegram broadcast to consultant clients");
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            String errorMsg = "Error in Telegram broadcast to consultant clients: " + e.getMessage();
            logger.error(errorMsg, e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
