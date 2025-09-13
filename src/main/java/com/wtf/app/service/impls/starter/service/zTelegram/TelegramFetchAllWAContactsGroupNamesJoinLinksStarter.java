package com.wtf.app.service.impls.starter.service.zTelegram;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.service.impls.WATGFetchAllWAContactsWGroupNamesService;
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
 * Service for fetching WhatsApp contacts and group names from Telegram.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Component("telegramFetchAllWAContactsGroupNamesJoinLinksStarter")
public class TelegramFetchAllWAContactsGroupNamesJoinLinksStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(TelegramFetchAllWAContactsGroupNamesJoinLinksStarter.class);
    
    private final WATGFetchAllWAContactsWGroupNamesService watgBFetchAllWAContactsWGroupNamesService;


    /**
     * Constructs a new TelegramFetchAllWAContactsGroupNamesJoinLinksStarter with required dependencies.
     *
     * @param initialSetup The initial setup configuration
     * @param parallelWebDriverManager The parallel web driver manager
     * @param socialMediaParentRunner The parent runner for executing services
     * @param whatsappDTO The WhatsApp DTO
     * @param telegramDTO The Telegram DTO
     * @param facebookDTO The Facebook DTO
     * @param watgBFetchAllWAContactsWGroupNamesService The service for fetching WhatsApp contacts and group names
     */
    @Autowired
    public TelegramFetchAllWAContactsGroupNamesJoinLinksStarter(
            
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            @Qualifier("whatsappDTO") SocialModel whatsappDTO,
            @Qualifier("telegramDTO") SocialModel telegramDTO,
            @Qualifier("facebookDTO") SocialModel facebookDTO,
            @Lazy WATGFetchAllWAContactsWGroupNamesService watgBFetchAllWAContactsWGroupNamesService) {
        
        super(
            
            initialSetup,
            parallelWebDriverManager,
            socialMediaParentRunner,
            whatsappDTO,
            telegramDTO,
            facebookDTO
        );
        Assert.notNull(watgBFetchAllWAContactsWGroupNamesService, "WATGFetchAllWAContactsWGroupNamesService must not be null");
        this.watgBFetchAllWAContactsWGroupNamesService = watgBFetchAllWAContactsWGroupNamesService;
        
        logger.debug("TelegramFetchAllWAContactsGroupNamesJoinLinksStarter initialized with all required dependencies");
    }

    /**
     * Starts the process of fetching WhatsApp contacts and group names.
     * This method runs asynchronously to prevent blocking the main thread.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws IOException if an I/O error occurs
     * @throws AWTException if there is an issue with the AWT environment
     * @throws IllegalStateException if required dependencies are not properly initialized
     */
    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils socialParentCommonUtils) throws InterruptedException, AWTException, IOException {
        logger.info("Traversing Telegram to fetch WA contacts and groups");
        Assert.notNull(watgBFetchAllWAContactsWGroupNamesService, "watgBFetchAllWAContactsWGroupNamesService must not be null");
        WATGCommonUtils instance = (WATGCommonUtils) socialParentCommonUtils;
        // Call the service method with the instance parameter
        watgBFetchAllWAContactsWGroupNamesService.traverseGroups(instance);
    }

    /**
     * Static method to start the process of fetching WhatsApp contacts and group names.
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
            TelegramFetchAllWAContactsGroupNamesJoinLinksStarter.class.getSimpleName()
        );
    }

    @Async
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        try {
            logger.info("Starting Telegram contacts and group names fetch process");
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(telegramDTO, "telegramDTO must not be null");
            Assert.notNull(watgBFetchAllWAContactsWGroupNamesService, "watgBFetchAllWAContactsWGroupNamesService must not be null");

            socialMediaParentRunner.execute(context, this, telegramDTO, TaskType.TELEGRAM_FETCH_CONTACTS);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Error in Telegram contacts and group names fetch process: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Error in Telegram contacts and group names fetch process", e));
        }
    }
}
