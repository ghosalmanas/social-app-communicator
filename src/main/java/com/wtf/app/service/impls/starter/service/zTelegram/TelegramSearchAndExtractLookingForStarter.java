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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service for searching and extracting messages containing "I am looking for" in Telegram.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Component("telegramSearchAndExtractLookingForStarter")
public class TelegramSearchAndExtractLookingForStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(TelegramSearchAndExtractLookingForStarter.class);

    /**
     * Constructs a new TelegramSearchAndExtractLookingForStarter with required dependencies.
     *
     * @param initialSetup The initial setup configuration
     * @param parallelWebDriverManager The parallel web driver manager
     * @param socialMediaParentRunner The parent runner for executing services
     * @param whatsappDTO The WhatsApp DTO
     * @param telegramDTO The Telegram DTO
     * @param facebookDTO The Facebook DTO
     */
    @Autowired
    public TelegramSearchAndExtractLookingForStarter(
            
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
        
        logger.debug("TelegramSearchAndExtractLookingForStarter initialized with all required dependencies");
    }

    /**
     * Starts the process of searching and extracting messages containing "I am looking for".
     * This method runs asynchronously to prevent blocking the main thread.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws IOException if an I/O error occurs
     * @throws AWTException if there is an issue with the AWT environment
     * @throws IllegalStateException if required dependencies are not properly initialized
     */
    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils socialParentCommonUtils) throws InterruptedException, AWTException, IOException {
        logger.info("Searching and extracting 'I am looking for' messages from Telegram");

        try {
            // Create instance of TG_SearchAndExtractLookingFor
            com.wtf.app.service.impls.TG_SearchAndExtractLookingFor instance = new com.wtf.app.service.impls.TG_SearchAndExtractLookingFor(initialSetup, parallelWebDriverManager);
            
            // Execute the search and extract logic using the start method which handles initialization
            AutomationContext tempContext = new AutomationContext(TaskType.TELEGRAM_SEARCH_LOOKING_FOR);
            tempContext.setSocialModel(telegramDTO);
            instance.start(tempContext, TaskType.TELEGRAM_SEARCH_LOOKING_FOR).get();
            
            logger.info("Successfully searched and extracted 'I am looking for' messages from Telegram");
        } catch (Exception e) {
            logger.error("Error searching and extracting Telegram messages: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching and extracting Telegram messages", e);
        }
    }

    /**
     * Static method to start the process of searching and extracting messages.
     * This method is called from the RunnerOneStopSolution class.
     * 
     * @throws InterruptedException if the thread is interrupted
     * @throws IOException if an I/O error occurs
     * @throws AWTException if there is an issue with the AWT environment
     */
    /**
     * @deprecated Use Spring dependency injection instead of static callStart()
     */
    /**
     * @return never returns, always throws UnsupportedOperationException
     * @throws UnsupportedOperationException always thrown to indicate this method should not be called
     * @deprecated Use Spring dependency injection instead of static callStart()
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static CompletableFuture<Void> callStart() {
        throw new UnsupportedOperationException(
            "Use Spring dependency injection to get an instance of " +
            TelegramSearchAndExtractLookingForStarter.class.getSimpleName()
        );
    }

    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        try {
            logger.info("Starting Telegram search and extract 'I am looking for' messages process");
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(telegramDTO, "telegramDTO must not be null");

            socialMediaParentRunner.execute(context, this, telegramDTO, TaskType.TELEGRAM_SEARCH_LOOKING_FOR);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Error in Telegram search and extract 'I am looking for' messages process: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Error in Telegram search and extract 'I am looking for' messages process", e));
        }
    }
}
