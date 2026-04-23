package com.wtf.app.service.impls.starter.service.zFacebook;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.impls.FacebookBroadcastForOffSuppAnnonymService;
import com.wtf.app.service.impls.starter.service.whatsApp.SocialMediaParentRunner;
import com.wtf.app.util.ThreadLocalAutomationContext;
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
import com.wtf.app.parent.SocialParentCommonUtils;

@Component("facebookBroadcastGreetOffFreelancerInFBGroupsStarter")
public class FacebookBroadcastGreetOffFreelancerInFBGroupsStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(FacebookBroadcastGreetOffFreelancerInFBGroupsStarter.class);

    private final FacebookBroadcastForOffSuppAnnonymService fbCBroadcastForOffSuppAnnonymService;

    @Autowired
    public FacebookBroadcastGreetOffFreelancerInFBGroupsStarter(
            
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            @Qualifier("whatsappDTO") SocialModel whatsappDTO,
            @Qualifier("telegramDTO") SocialModel telegramDTO,
            @Qualifier("facebookDTO") SocialModel facebookDTO,
            FacebookBroadcastForOffSuppAnnonymService fbCBroadcastForOffSuppAnnonymService) {
        super(
            
            initialSetup,
            parallelWebDriverManager,
            socialMediaParentRunner,
            whatsappDTO,
            telegramDTO,
            facebookDTO
        );
        Assert.notNull(fbCBroadcastForOffSuppAnnonymService, "FacebookBroadcastForOffSuppAnnonymService must not be null");
        this.fbCBroadcastForOffSuppAnnonymService = fbCBroadcastForOffSuppAnnonymService;
    }


    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) throws InterruptedException, AWTException, IOException {
        logger.info("Starting to traverse Facebook groups for greeting off-freelancers");
        Assert.notNull(fbCBroadcastForOffSuppAnnonymService, "fbCBroadcastForOffSuppAnnonymService must not be null");

        try {
            // Get the current context
            AutomationContext context = ThreadLocalAutomationContext.getContext();
            if (context == null) {
                throw new IllegalStateException("AutomationContext is not set in ThreadLocal");
            }

            // Call the service method
            fbCBroadcastForOffSuppAnnonymService.traverseGroups();
            logger.info("Successfully completed greeting off-freelancers in Facebook groups");
        } catch (Exception e) {
            logger.error("Error during greeting off-freelancers in Facebook groups: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        logger.info("Starting Facebook greeting off-freelancers in groups");
        try {
            // Set the context in ThreadLocalAutomationContext
            ThreadLocalAutomationContext.setContext(context);

            // Validate required dependencies
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(facebookDTO, "facebookDTO must not be null");
            context.setSocialModel(facebookDTO);

            // Execute the service using the parent class method
            socialMediaParentRunner.execute(context, this, facebookDTO, TaskType.FACEBOOK_BROADCAST_AD);

            logger.info("Successfully completed greeting off-freelancers in Facebook groups");
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            logger.error("Error in greeting off-freelancers in Facebook groups: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Error in greeting off-freelancers in Facebook groups", e));
        } finally {
            // Clean up the context
            ThreadLocalAutomationContext.clear();
        }
    }
}
