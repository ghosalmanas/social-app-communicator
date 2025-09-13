package com.wtf.app.service.impls.starter.service.zFacebook;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.impls.FacebookBroadcastToSensitiveGroupAnnonymService;
import com.wtf.app.service.impls.starter.service.whatsApp.SocialMediaParentRunner;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import com.wtf.app.parent.SocialParentCommonUtils;

@Component("facebookBroadcastSelfSkillsToFBSensitiveGroupsStarter")
public class FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter.class);

    private final FacebookBroadcastToSensitiveGroupAnnonymService fbBBroadcastToSensitiveGroupAnnonymService;

    @Autowired
    public FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter(
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            @Qualifier("whatsappDTO") SocialModel whatsappDTO,
            @Qualifier("telegramDTO") SocialModel telegramDTO,
            @Qualifier("facebookDTO") SocialModel facebookDTO,
            FacebookBroadcastToSensitiveGroupAnnonymService fbBBroadcastToSensitiveGroupAnnonymService) {
        super(
            initialSetup,
            parallelWebDriverManager,
            socialMediaParentRunner,
            whatsappDTO,
            telegramDTO,
            facebookDTO
        );
        this.fbBBroadcastToSensitiveGroupAnnonymService = fbBBroadcastToSensitiveGroupAnnonymService;
    }

    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) throws InterruptedException, IOException {
        logger.info("Starting to traverse Facebook sensitive groups for self-skills broadcast");
        Assert.notNull(fbBBroadcastToSensitiveGroupAnnonymService, "fbBBroadcastToSensitiveGroupAnnonymService must not be null");

        try {
            // Call the service method
            fbBBroadcastToSensitiveGroupAnnonymService.traverseGroups();
            logger.info("Successfully completed Facebook sensitive groups traversal");
        } catch (Exception e) {
            String errorMsg = "Error during Facebook sensitive groups traversal: " + e.getMessage();
            logger.error(errorMsg, e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        try {
            logger.info("Starting Facebook broadcast to sensitive groups");

            // Validate required dependencies
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(facebookDTO, "facebookDTO must not be null");

            // Execute the service using the parent class method
            socialMediaParentRunner.execute(context, this, facebookDTO, TaskType.FACEBOOK_BROADCAST_AD);

            logger.info("Successfully completed Facebook broadcast to sensitive groups");
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            String errorMsg = "Error in Facebook broadcast to sensitive groups: " + e.getMessage();
            logger.error(errorMsg, e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

}
