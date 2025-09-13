package com.wtf.app.service.impls.starter.service.zTelegram;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.service.impls.WATGBroadcastMessageToAllGroupsService;
import com.wtf.app.service.impls.starter.service.whatsApp.SocialMediaParentRunner;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.awt.AWTException;
import java.io.IOException;

/**
 * Service for broadcasting self-skills messages to Telegram groups.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Service("telegramBroadcastSelfSkillsToTGGroupsStarter")
public class TelegramBroadcastSelfSkillsToTGGroupsStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(TelegramBroadcastSelfSkillsToTGGroupsStarter.class);
    
    private final WATGBroadcastMessageToAllGroupsService watgBroadcastMessageToAllGroupsService;

    @Autowired
    public TelegramBroadcastSelfSkillsToTGGroupsStarter(
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            @Qualifier("whatsappDTO") SocialModel whatsappDTO,
            @Qualifier("telegramDTO") SocialModel telegramDTO,
            @Qualifier("facebookDTO") SocialModel facebookDTO,
            WATGBroadcastMessageToAllGroupsService watgBroadcastMessageToAllGroupsService) {
        super(
            initialSetup,
            parallelWebDriverManager,
            socialMediaParentRunner,
            whatsappDTO,
            telegramDTO,
            facebookDTO
        );
        this.watgBroadcastMessageToAllGroupsService = watgBroadcastMessageToAllGroupsService;
    }

    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) throws InterruptedException, AWTException, IOException {
        logger.info("Starting to traverse Telegram groups for self-skills broadcast");
        Assert.notNull(watgBroadcastMessageToAllGroupsService, "watgBroadcastMessageToAllGroupsService must not be null");
        Assert.notNull(telegramDTO, "telegramDTO must not be null");

        // Set the social model and xpath interface if needed
        if (instance instanceof SocialMediaServiceStarter) {
            SocialMediaServiceStarter starter = (SocialMediaServiceStarter) instance;
            starter.setSocialModel(telegramDTO);
            if (telegramDTO.getxPathInterface() != null) {
                starter.setxPathInterface(telegramDTO.getxPathInterface());
            }
        }

        // The context is retrieved from ThreadLocal
        AutomationContext context = ThreadLocalAutomationContext.getContext();
        Assert.notNull(context, "AutomationContext cannot be null");

        // Call the public start method to begin the broadcast
        watgBroadcastMessageToAllGroupsService.start(context, TaskType.TELEGRAM_BROADCAST_AD);
        logger.info("Successfully completed broadcast to Telegram groups");
    }



    /**
     * Starts the process of broadcasting self-skills messages to Telegram groups.
     * This method runs asynchronously to prevent blocking the main thread.
     *
     * @return CompletableFuture that completes when the broadcast is done
     */
    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        logger.info("Starting Telegram self-skills broadcast...");
        try {
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(telegramDTO, "telegramDTO must not be null");

            socialMediaParentRunner.execute(context, this, telegramDTO, TaskType.TELEGRAM_BROADCAST_AD);

            logger.info("Successfully completed broadcast to Telegram groups");
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Error in Telegram self-skills broadcast: {}", e.getMessage(), e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
