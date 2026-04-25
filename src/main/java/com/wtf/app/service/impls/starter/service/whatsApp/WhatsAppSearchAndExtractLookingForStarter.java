package com.wtf.app.service.impls.starter.service.whatsApp;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.commons.InitialSetup;
import com.wtf.app.service.impls.WA_SearchAndExtractLookingFor;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service for searching and extracting "I am looking for" messages from WhatsApp.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Component("whatsappSearchAndExtractLookingForStarter")
public class WhatsAppSearchAndExtractLookingForStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(WhatsAppSearchAndExtractLookingForStarter.class);

    /**
     * Constructs a new WhatsAppSearchAndExtractLookingForStarter with required dependencies.
     *
     * @param initialSetup The initial setup configuration
     * @param parallelWebDriverManager The parallel WebDriver manager
     * @param socialMediaParentRunner The runner for executing social media tasks
     * @param whatsappDTO DTO for WhatsApp configuration
     * @param telegramDTO DTO for Telegram configuration
     * @param facebookDTO DTO for Facebook configuration
     */
    @Autowired
    public WhatsAppSearchAndExtractLookingForStarter(
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
        logger.debug("WhatsAppSearchAndExtractLookingForStarter initialized");
    }

    /**
     * Starts the WhatsApp search and extract process for "I am looking for" messages.
     *
     * @param context The automation context
     * @return a CompletableFuture that completes when the operation is done
     */
    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        logger.info("Starting WhatsApp search and extract 'I am looking for' messages process");
        try {
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(whatsappDTO, "whatsappDTO must not be null");

            socialMediaParentRunner.execute(context, this, whatsappDTO, TaskType.WHATSAPP_SEARCH_LOOKING_FOR);
            logger.info("Successfully completed WhatsApp search and extract 'I am looking for' messages process");
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Error in WhatsApp search and extract 'I am looking for' messages process: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Error in WhatsApp search and extract 'I am looking for' messages process", e));
        }
    }

    /**
     * Traverses groups and performs the search and extract operation.
     * This method is called by the parent class to perform the actual traversal.
     *
     * @param socialParentCommonUtils the SocialParentCommonUtils instance to use for traversal
     */
    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils socialParentCommonUtils) throws InterruptedException, AWTException, IOException {
        logger.info("Starting to search and extract 'I am looking for' messages from WhatsApp");

        try {
            // Create instance of WA_SearchAndExtractLookingFor
            WA_SearchAndExtractLookingFor instance = new WA_SearchAndExtractLookingFor(initialSetup, parallelWebDriverManager);
            
            // Execute the search and extract logic using the start method which handles initialization
            AutomationContext tempContext = new AutomationContext(TaskType.WHATSAPP_SEARCH_LOOKING_FOR);
            tempContext.setSocialModel(whatsappDTO);
            instance.start(tempContext, TaskType.WHATSAPP_SEARCH_LOOKING_FOR).get();
            
            logger.info("Successfully searched and extracted 'I am looking for' messages from WhatsApp");
        } catch (Exception e) {
            logger.error("Error searching and extracting WhatsApp messages: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching and extracting WhatsApp messages", e);
        }
    }
}
