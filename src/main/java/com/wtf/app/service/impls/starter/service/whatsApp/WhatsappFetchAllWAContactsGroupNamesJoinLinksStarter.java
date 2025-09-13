package com.wtf.app.service.impls.starter.service.whatsApp;

import com.wtf.app.a_runner.SocialMediaServiceStarter;
import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.service.interfaces.IAllWAGroupNamesRetrievalAndJoinNewGroups;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Service for fetching all WhatsApp contacts and group names with join links.
 * This service runs asynchronously to prevent blocking the main application thread.
 */
@Component("whatsappFetchAllWAContactsGroupNamesJoinLinksStarter")
public class WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter extends SocialMediaServiceStarter {
    private static final Logger logger = LogManager.getLogger(WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter.class);
    
    private final IAllWAGroupNamesRetrievalAndJoinNewGroups watgBFetchAllWAContactsWGroupNamesService;

    /**
     * Constructs a new WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter with required dependencies.
     *
     * @param initialSetup The initial setup configuration
     * @param parallelWebDriverManager The parallel WebDriver manager
     * @param socialMediaParentRunner The runner for executing social media tasks
     * @param whatsappDTO DTO for WhatsApp configuration
     * @param telegramDTO DTO for Telegram configuration
     * @param facebookDTO DTO for Facebook configuration
     * @param watgBFetchAllWAContactsWGroupNamesService Service for fetching WhatsApp contacts and group names
     */
    @Autowired
    public WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter(
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            @Qualifier("whatsappDTO") SocialModel whatsappDTO,
            @Qualifier("telegramDTO") SocialModel telegramDTO,
            @Qualifier("facebookDTO") SocialModel facebookDTO,
            @Lazy IAllWAGroupNamesRetrievalAndJoinNewGroups watgBFetchAllWAContactsWGroupNamesService) {
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
        logger.debug("WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter initialized");
    }

    /**
     * Static method to start the process of fetching WhatsApp contacts and group names.
     * This method is called from the RunnerOneStopSolution class.
     * 
     * @return a CompletableFuture that completes when the operation is done
     */
    @Override
    public CompletableFuture<Boolean> start(AutomationContext context) {
        logger.info("Starting WhatsApp contacts and group names fetch process");
        try {
            Assert.notNull(socialMediaParentRunner, "socialMediaParentRunner must not be null");
            Assert.notNull(whatsappDTO, "whatsappDTO must not be null");
            Assert.notNull(watgBFetchAllWAContactsWGroupNamesService, "watgBFetchAllWAContactsWGroupNamesService must not be null");

            socialMediaParentRunner.execute(context, this, whatsappDTO, TaskType.WHATSAPP_FETCH_CONTACTS);
            logger.info("Successfully completed WhatsApp contacts and group names fetch process");
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Error in WhatsApp contacts and group names fetch process: {}", e.getMessage(), e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Traverses groups and fetches all WhatsApp contacts and group names with join links.
     * This method is called by the parent class to perform the actual traversal.
     *
     * @param instance the SocialParentCommonUtils instance to use for traversal
     */
    @Override
    public void traverseGroupsTemplate(com.wtf.app.parent.SocialParentCommonUtils instance) {
        logger.info("Starting to fetch all WhatsApp contacts and group names with join links");
        Assert.notNull(watgBFetchAllWAContactsWGroupNamesService, "watgBFetchAllWAContactsWGroupNamesService must not be null");

        try {
            watgBFetchAllWAContactsWGroupNamesService.traverseGroups((WATGCommonUtils) instance);
            logger.info("Successfully fetched all WhatsApp contacts and group names with join links");
        } catch (Exception e) {
            logger.error("Error fetching WhatsApp contacts and group names: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching WhatsApp contacts and group names", e);
        }
    }
    
    private static boolean fetchConfigProperties() {
        Properties prop = new Properties();

        // try-with-resources automatically closes the InputStream
        try (InputStream input = WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return true;
            }
            prop.load(input);
            prop.forEach((key, value) -> System.out.println("getConfigProperties: "+key + " : " + value));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
