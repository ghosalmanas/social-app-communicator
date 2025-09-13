package com.wtf.app.service.impls.starter.service.whatsApp;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.service.impls.WATGBroadcastMessageToAllGroupsService;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service starter for broadcasting self skills to WhatsApp groups.
 */
@Component("whatsappBroadcastSelfSkillsToWAGroupsStarter")
public class WhatsappBroadcastSelfSkillsToWAGroupsStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(WhatsappBroadcastSelfSkillsToWAGroupsStarter.class);

    private final WATGBroadcastMessageToAllGroupsService watgBroadcastMessageToAllGroupsService;

    @Autowired
    public WhatsappBroadcastSelfSkillsToWAGroupsStarter(
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
        Assert.notNull(watgBroadcastMessageToAllGroupsService, "watgBroadcastMessageToAllGroupsService must not be null");
        this.watgBroadcastMessageToAllGroupsService = watgBroadcastMessageToAllGroupsService;
    }

    /**
     * Traverses WhatsApp groups and sends broadcast messages.
     * This method is called by the parent class to perform the actual traversal.
     *
     * @param instance the service starter instance
     * @throws InterruptedException if the thread is interrupted
     * @throws AWTException if there's an AWT error
     * @throws IOException if there's an I/O error
     */
    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) {
        logger.info("Starting to traverse WhatsApp groups for self-skills broadcast");
        Assert.notNull(watgBroadcastMessageToAllGroupsService, "watgBroadcastMessageToAllGroupsService must not be null");

        try {
            AutomationContext context = ThreadLocalAutomationContext.getContext();
            Assert.notNull(context, "AutomationContext cannot be null");
            watgBroadcastMessageToAllGroupsService.start(context,  TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            logger.info("Successfully completed broadcast to WhatsApp groups");
        } catch (Exception e) {
            logger.error("Error during WhatsApp groups traversal: {}", e.getMessage(), e);
            throw new RuntimeException("Error during WhatsApp groups traversal", e);
        }
    }

    /**
     * Entry point for the service. This method is called asynchronously.
     */
    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        logger.info("Starting WhatsappBroadcastSelfSkillsToWAGroupsStarter...");
        try {
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(whatsappDTO, "whatsappDTO must not be null");
            Assert.notNull(watgBroadcastMessageToAllGroupsService, "watgBroadcastMessageToAllGroupsService must not be null");

            socialMediaParentRunner.execute(context, this, whatsappDTO, TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);

            logger.info("Successfully completed WhatsappBroadcastSelfSkillsToWAGroupsStarter");
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            String errorMsg = "Error in WhatsappBroadcastSelfSkillsToWAGroupsStarter: " + e.getMessage();
            logger.error(errorMsg, e);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        }
    }

    /**
     * @deprecated Use Spring dependency injection instead of static callStart()
     */
    @Deprecated
    public static CompletableFuture<Void> callStart() {
        throw new UnsupportedOperationException("This method is deprecated. Use Spring's dependency injection instead.");
    }
}
