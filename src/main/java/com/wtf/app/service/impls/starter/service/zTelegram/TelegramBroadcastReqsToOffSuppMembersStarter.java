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
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.openqa.selenium.Keys;

/**
 * Service for broadcasting requirements to offline support members via Telegram.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Component("telegramBroadcastReqsToOffSuppMembersStarter")
public class TelegramBroadcastReqsToOffSuppMembersStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(TelegramBroadcastReqsToOffSuppMembersStarter.class);

    private static final String MESSAGE_TO_SUPPORT_CANDIDATE_LIST_JD_REQUIREMENT = "We have Support Freelancing Requirement for following Tech Stack"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "1. "
            + "Java Development for 2-3 years of consultant in India,"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "2. "
            + "Reactor Kafka with Java for 7 years Experience consultant in US"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "Please let me know if you are comfortable for daily Max 2 hrs(Mon-Fri) Freelancing"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "Please Save The Contact for Continuous Requirements in Pipeline";

    /**
     * Constructs a new TelegramBroadcastReqsToOffSuppMembersStarter with required dependencies.
     *
     * @param initialSetup The initial setup for the service
     * @param parallelWebDriverManager The parallel web driver manager
     * @param socialMediaParentRunner The runner for executing social media tasks
     * @param whatsappDTO DTO for WhatsApp configuration
     * @param telegramDTO DTO for Telegram configuration
     * @param facebookDTO DTO for Facebook configuration
     * @throws IllegalArgumentException if any of the required dependencies are null
     */
    @Autowired
    public TelegramBroadcastReqsToOffSuppMembersStarter(
            
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
        logger.debug("TelegramBroadcastReqsToOffSuppMembersStarter initialized with all required dependencies");
    }

    /**
     * Static method to start the broadcast process.
     * This method is called from the RunnerOneStopSolution class.
     * 
     * @throws InterruptedException if the thread is interrupted
     * @throws IOException if an I/O error occurs
     * @throws AWTException if there is an issue with the AWT environment
     */
    /**
     * @deprecated Use Spring dependency injection instead of static callStart()
     */
    @Deprecated
    public static CompletableFuture<Void> callStart() {
        throw new UnsupportedOperationException("Use Spring dependency injection instead of static callStart()");
    }

    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        try {
            logger.info("Starting TelegramBroadcastReqsToOffSuppMembersStarter...");

            // Validate all required dependencies
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(telegramDTO, "telegramDTO must not be null");

            // Execute the service using the parent runner
            socialMediaParentRunner.execute(context, this, telegramDTO, TaskType.TELEGRAM_BROADCAST_AD);

            logger.info("Successfully completed TelegramBroadcastReqsToOffSuppMembersStarter");
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            String errorMsg = "Error in TelegramBroadcastReqsToOffSuppMembersStarter: " + e.getMessage();
            logger.error(errorMsg, e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) throws InterruptedException, AWTException, IOException {
        logger.warn("traverseGroupsTemplate is not applicable for TelegramBroadcastReqsToOffSuppMembersStarter, as it broadcasts to individuals.");
        // This starter broadcasts to individual members, not groups, so this method is intentionally left blank.
    }
}
