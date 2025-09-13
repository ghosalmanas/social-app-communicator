package com.wtf.app.service.parallel;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.enums.TaskType;

import com.wtf.app.service.base.BaseParallelAutomation;
import com.wtf.app.service.base.WebDriverAware;
import com.wtf.app.service.impls.starter.service.zTelegram.TelegramBroadcastSelfSkillsToTGGroupsStarter;
import com.wtf.app.service.impls.WATGFetchAllWAContactsWGroupNamesService;

import com.wtf.app.web.driver.AsyncWebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service for running Telegram automations in parallel with comprehensive error handling.
 */
@Service
public class TelegramAutomationService extends BaseParallelAutomation {
    private static final String PROFILE_NAME = "telegram-profile";
    private static final int MAX_RETRIES = 3;
    private static final int ELEMENT_WAIT_SECONDS = 120;
    
    @Value("${telegram.web.url:https://web.telegram.org}")
    private String telegramWebUrl;
    
    @Value("${telegram.login.wait.seconds:120}")
    private int loginWaitSeconds;
    
    @Value("${telegram.group.broadcast.enabled:true}")
    private boolean broadcastEnabled;
    
    private final InitialSetup initialSetup;
    @Qualifier("automationTaskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ApplicationContext applicationContext;
    private final AsyncWebDriverManager webDriverManager;
    private final TelegramBroadcastSelfSkillsToTGGroupsStarter broadcastService;
    private final WATGFetchAllWAContactsWGroupNamesService telegramFetchService;
    private static final Logger logger = LoggerFactory.getLogger(TelegramAutomationService.class);
    
    @Autowired
    public TelegramAutomationService(
            ThreadPoolTaskExecutor taskExecutor,
            ApplicationContext applicationContext,
            AsyncWebDriverManager webDriverManager,
            TelegramBroadcastSelfSkillsToTGGroupsStarter broadcastService,
            @Qualifier("telegramFetchService") WATGFetchAllWAContactsWGroupNamesService telegramFetchService) {
        super(taskExecutor, applicationContext);
        this.initialSetup = applicationContext.getBean(InitialSetup.class);
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
        this.webDriverManager = webDriverManager;
        this.broadcastService = broadcastService;
        this.telegramFetchService = telegramFetchService;
        logger.info("TelegramAutomationService initialized with fetchService instance: {}", this.telegramFetchService.hashCode());
    }
    
    /**
     * Main entry point for starting all Telegram automation tasks.
     * @param context The automation context to be used for the tasks.
     */
    public void start(AutomationContext context) {
        logger.info("Starting Telegram automation with context: {}", context.getTaskId());
        broadcastToGroups(context);
        logger.info("Telegram automation tasks would run here.");
    }

    /**
     * Creates a new WebDriver instance using the AsyncWebDriverManager.
     * This method is kept for backward compatibility.
     */
    private WebDriver createNewWebDriverInstance() {
        try {
            // This will block until the WebDriver is created
            return webDriverManager.createDriver().get();
        } catch (Exception e) {
            logger.error("Error creating WebDriver instance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create WebDriver: " + e.getMessage(), e);
        }
    }
    
    /**
     * Run the Telegram broadcast message to all groups with retry mechanism.
     */
    public CompletableFuture<Boolean> broadcastToGroups(AutomationContext context) {
        
        if (!broadcastEnabled) {
            logger.info("Broadcast is disabled in configuration");
            return CompletableFuture.completedFuture(false);
        }

        return webDriverManager.createDriver()
            .thenCompose(driver -> {
                try {

                    // Set the WebDriver in the broadcast service if it's WebDriverAware
                    if (broadcastService instanceof WebDriverAware) {
                        ((WebDriverAware) broadcastService).setWebDriver(driver);
                    }
                    
                    logger.info("Starting broadcast to all Telegram groups...");
                    return broadcastService.start(context).thenApply(success -> {
                        logger.info("Telegram broadcast completed successfully");
                        return success;
                    });
                    
                } catch (Exception e) {
                    logger.error("Error in Telegram broadcast automation", e);
                    return CompletableFuture.failedFuture(e);
                } finally {
                    // Clean up the WebDriver and context
                    // webDriverManager.quitDriver(driver); // This will be handled by the aspect or when the future completes
                }
            })
            .whenComplete((result, ex) -> {
                // Final cleanup, regardless of outcome
                // This is a good place to ensure the driver is quit if it hasn't been already.
                // For now, assuming WebDriver cleanup is handled correctly elsewhere.
            })
            .exceptionally(ex -> {
                logger.error("Failed to complete Telegram broadcast", ex);
                return false;
            });
    }
    
    /**
     * Fetch all Telegram contacts and group names.
     * This method is designed to run asynchronously with proper context propagation.
     */
    @Async("automationTaskExecutor")
    public CompletableFuture<Boolean> fetchAllContactsAndGroups() {
        return webDriverManager.createDriver()
            .thenCompose(driver -> {
                try {
                    logger.info("Using fetchService instance: {} for Telegram", telegramFetchService.hashCode());
                    telegramFetchService.setWebDriver(driver);

                    SocialModel telegramModel = initialSetup.getSocialTypeToSocialModel().get(SocialType.TELEGRAM);
                    telegramFetchService.setSocialModel(telegramModel);
                    telegramFetchService.setxPathInterface(telegramModel.getxPathInterface());
                    telegramFetchService.setBaseUrl(telegramModel.getBaseURL());
                    telegramFetchService.setTaskTypeMapping(TaskType.TELEGRAM_FETCH_CONTACTS, telegramFetchService);

                    telegramFetchService.traverseGroupsTemplate(telegramFetchService);
                    return CompletableFuture.completedFuture(true);
                } catch (Exception e) {
                    logger.error("Error in fetchAllContactsAndGroups", e);
                    return CompletableFuture.failedFuture(e);
                } finally {
                    safeQuitDriver(driver);
                }
            });
    }
    
    private void safeQuitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.error("Error quitting WebDriver", e);
            }
        }
    }

    /**
     * Navigate to Telegram Web and wait for login.
     */
    private void navigateToTelegramWeb(WebDriver driver) {
        try {
            logger.info("Navigating to Telegram Web..."+telegramWebUrl);
            driver.get(telegramWebUrl);
            
            // Wait for login to complete (check for main chat list)
            logger.info("Please log in to Telegram Web...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(loginWaitSeconds));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class, 'chatlist')]")
            ));
            
            // Additional wait to ensure chats are loaded
            Thread.sleep(5000);
            logger.info("Successfully logged in to Telegram Web");
            
        } catch (TimeoutException e) {
            throw new RuntimeException("Timed out waiting for login. Please log in within " + loginWaitSeconds + " seconds.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Telegram navigation was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to Telegram Web", e);
        }
    }
}
