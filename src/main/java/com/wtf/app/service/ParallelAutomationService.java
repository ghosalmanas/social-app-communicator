package com.wtf.app.service;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.service.impls.FacebookBroadcastSelfSkillSetService;
import com.wtf.app.service.impls.FacebookBroadcastToSensitiveGroupAnnonymService;
import com.wtf.app.service.impls.WATGBroadcastMessageToAllGroupsService;
import com.wtf.app.service.impls.WATGFetchAllWAContactsWGroupNamesService;
import com.wtf.app.service.impls.starter.service.whatsApp.WhatsAppSearchAndExtractLookingForStarter;
import com.wtf.app.service.impls.starter.service.zTelegram.TelegramSearchAndExtractLookingForStarter;
import com.wtf.app.service.parallel.TelegramAutomationService;
import com.wtf.app.service.parallel.WhatsAppAutomationService;
import com.wtf.app.util.ThreadLocalAutomationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to run WhatsApp and Telegram automations in parallel with comprehensive
 * error handling and execution tracking.
 */
@Service
public class ParallelAutomationService {
    private static final Logger logger = LogManager.getLogger(ParallelAutomationService.class);
    private final ThreadPoolTaskExecutor taskExecutor;
    private final WATGFetchAllWAContactsWGroupNamesService whatsAppFetchService;
    private final WATGFetchAllWAContactsWGroupNamesService telegramFetchService;
    private final WATGBroadcastMessageToAllGroupsService whatsAppBroadcastService;
    private final WATGBroadcastMessageToAllGroupsService telegramBroadcastService;
    private final WhatsAppAutomationService whatsAppService;
    private final TelegramAutomationService telegramService;
    private final FacebookBroadcastSelfSkillSetService facebookBroadcastSelfSkillSetService;
    private final TelegramSearchAndExtractLookingForStarter telegramSearchAndExtractLookingForStarter;
    private final WhatsAppSearchAndExtractLookingForStarter whatsappSearchAndExtractLookingForStarter;

    @Autowired
    private FacebookBroadcastToSensitiveGroupAnnonymService facebookBroadcastToSensitiveGroupAnnonymService;
    private final AtomicInteger taskCounter = new AtomicInteger(0);


    @Value("${onlyAllowAllBroadcastsWATGFB:false}")
    private boolean onlyAllowAllBroadcastsWATGFB;

    @Value("${onlyAllowAllFetchAndJoinsWATG:false}")
    private boolean onlyAllowAllFetchAndJoinsWATG;

    @Value("${whatsapp.group.broadcast.enabled:false}")
    private boolean whatsappBroadcastEnabled;

    @Value("${telegram.group.broadcast.enabled:false}")
    private boolean telegramBroadcastEnabled;

    @Value("${whatsapp.fetch.contacts.enabled:false}")
    private boolean whatsappFetchContactsEnabled;

    @Value("${whatsapp.search.looking.for.enabled:false}")
    private boolean whatsappSearchLookingForEnabled;

    @Value("${telegram.fetch.contacts.enabled:false}")
    private boolean telegramFetchContactsEnabled;

    @Value("${telegram.search.looking.for.enabled:false}")
    private boolean telegramSearchLookingForEnabled;

    @Value("${facebook.group.broadcast.enabled:false}")
    private boolean facebookBroadcastEnabled;

    @Value("${facebook.group.broadcast.sensitive.enabled:false}")
    private boolean facebookSensitiveBroadcastEnabled;

    @Value("${automation.max.retries:3}")
    private int maxRetries;

    @Value("${automation.retry.delay.ms:2000}")
    private long retryDelayMs;


    @Autowired
    public ParallelAutomationService(
            @Qualifier("automationTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
            @Qualifier("whatsAppFetchService") WATGFetchAllWAContactsWGroupNamesService whatsAppFetchService,
            @Qualifier("telegramFetchService") WATGFetchAllWAContactsWGroupNamesService telegramFetchService,
            @Qualifier("whatsAppBroadcastService") WATGBroadcastMessageToAllGroupsService whatsAppBroadcastService,
            @Qualifier("telegramBroadcastService") WATGBroadcastMessageToAllGroupsService telegramBroadcastService,
            WhatsAppAutomationService whatsAppService,
            TelegramAutomationService telegramService,
            FacebookBroadcastSelfSkillSetService facebookBroadcastSelfSkillSetService,
            @Lazy TelegramSearchAndExtractLookingForStarter telegramSearchAndExtractLookingForStarter,
            @Lazy WhatsAppSearchAndExtractLookingForStarter whatsappSearchAndExtractLookingForStarter) {
        this.taskExecutor = taskExecutor;
        this.whatsAppFetchService = whatsAppFetchService;
        this.telegramFetchService = telegramFetchService;
        this.whatsAppBroadcastService = whatsAppBroadcastService;
        this.telegramBroadcastService = telegramBroadcastService;
        this.whatsAppService = whatsAppService;
        this.telegramService = telegramService;
        this.facebookBroadcastSelfSkillSetService = facebookBroadcastSelfSkillSetService;
        this.telegramSearchAndExtractLookingForStarter = telegramSearchAndExtractLookingForStarter;
        this.whatsappSearchAndExtractLookingForStarter = whatsappSearchAndExtractLookingForStarter;
        logger.info("Initialized ParallelAutomationService with thread pool size: {}-{}",
                taskExecutor.getCorePoolSize(), taskExecutor.getMaxPoolSize());
    }

    /**
     * Starts all configured automation tasks (WhatsApp, Telegram) in parallel.
     *
     * @param context The automation context to be shared across0 all tasks.
     */
    public void startAllAutomations(AutomationContext context, List<TaskType> taskTypes) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        logger.info("Starting all automations with context ID: {}", context.getTaskId());

        if ((onlyAllowAllFetchAndJoinsWATG || whatsappFetchContactsEnabled) && taskTypes.contains(TaskType.WHATSAPP_FETCH_CONTACTS)) {
            futures.add(CompletableFuture.runAsync(() -> {
                logger.info("Starting WhatsApp fetch contacts task with context ID: {}", context.getTaskId());
                        TaskType whatsappFetchContacts = TaskType.WHATSAPP_FETCH_CONTACTS;
                        if(context.getTaskType()== whatsappFetchContacts) {
                    whatsAppFetchService.start(context, whatsappFetchContacts);
                }
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("WhatsApp fetch contacts failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if (whatsappSearchLookingForEnabled && taskTypes.contains(TaskType.WHATSAPP_SEARCH_LOOKING_FOR)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType whatsappSearchLookingFor = TaskType.WHATSAPP_SEARCH_LOOKING_FOR;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(whatsappSearchLookingFor);
                whatsappSearchAndExtractLookingForStarter.start(automationContext);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("WhatsApp search and extract 'I am looking for' failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if ((onlyAllowAllFetchAndJoinsWATG || telegramFetchContactsEnabled) && taskTypes.contains(TaskType.TELEGRAM_FETCH_CONTACTS)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType telegramFetchContacts = TaskType.TELEGRAM_FETCH_CONTACTS;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(telegramFetchContacts);
                        
                telegramFetchService.start(automationContext, telegramFetchContacts);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("Telegram fetch contacts failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if ((onlyAllowAllBroadcastsWATGFB ||whatsappBroadcastEnabled) && taskTypes.contains(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType whatsappBroadcastAd = TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(whatsappBroadcastAd);
                        automationContext.setProfileName(whatsappBroadcastAd.name());
                whatsAppBroadcastService.start(automationContext, whatsappBroadcastAd);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("WhatsApp broadcast failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if ((onlyAllowAllBroadcastsWATGFB || telegramBroadcastEnabled) && taskTypes.contains(TaskType.TELEGRAM_BROADCAST_AD)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType telegramBroadcastAd = TaskType.TELEGRAM_BROADCAST_AD;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(telegramBroadcastAd);
                telegramBroadcastService.start(automationContext, telegramBroadcastAd);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("Telegram broadcast failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if (telegramSearchLookingForEnabled && taskTypes.contains(TaskType.TELEGRAM_SEARCH_LOOKING_FOR)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType telegramSearchLookingFor = TaskType.TELEGRAM_SEARCH_LOOKING_FOR;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(telegramSearchLookingFor);
                telegramSearchAndExtractLookingForStarter.start(automationContext);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("Telegram search and extract 'I am looking for' failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if ((onlyAllowAllBroadcastsWATGFB || facebookBroadcastEnabled) && taskTypes.contains(TaskType.FACEBOOK_BROADCAST_AD)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType facebookBroadcastAd = TaskType.FACEBOOK_BROADCAST_AD;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(facebookBroadcastAd);
                        facebookBroadcastSelfSkillSetService.start(automationContext, facebookBroadcastAd);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("Facebook broadcast failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        //Already handled in FacebookCommonUtils by appending to generic broadcast to avoid duplicate login and approve prompt
        if ((!onlyAllowAllBroadcastsWATGFB && facebookSensitiveBroadcastEnabled && !facebookBroadcastEnabled) && taskTypes.contains(TaskType.FACEBOOK_BROADCAST_SENSITIVE_AD)) {
            futures.add(CompletableFuture.runAsync(() -> {
                        TaskType facebookBroadcastSensitiveAd = TaskType.FACEBOOK_BROADCAST_SENSITIVE_AD;
                        AutomationContext automationContext = ThreadLocalAutomationContext.createContext(facebookBroadcastSensitiveAd);
                        facebookBroadcastToSensitiveGroupAnnonymService.start(automationContext, facebookBroadcastSensitiveAd);
                    }, taskExecutor)
                    .exceptionally(ex -> {
                        logger.error("Facebook sensitive broadcast failed", ex);
                        return null; // Mark as complete even on failure
                    }));
        }

        if (futures.isEmpty()) {
            logger.warn("No automation tasks are enabled. Nothing to run.");
            return;
        }

        // Wait for all scheduled tasks to complete
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            logger.info("All automations completed successfully.");
        } catch (CompletionException e) {
            logger.error("One or more automation tasks failed during execution.", e.getCause());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while waiting for automation tasks to complete.", e);
        }
    }

    /**
     * Fetch contacts and groups from both services in parallel.
     * @return CompletableFuture that completes when both fetch operations finish
     */
    @Async("automationTaskExecutor")
    public CompletableFuture<Void> fetchAllContactsAndGroups() {
        logger.info("Starting to fetch contacts and groups from all services...");

        CompletableFuture<Void> whatsAppFuture = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> telegramFuture = CompletableFuture.completedFuture(null);

        if (whatsappFetchContactsEnabled) {
            whatsAppFuture = CompletableFuture.runAsync(() -> {
                try {
                    logger.info("Fetching WhatsApp contacts and groups...");
                    whatsAppService.fetchAllContactsAndGroups();
                    logger.info("Successfully fetched WhatsApp contacts and groups");
                } catch (Exception e) {
                    logger.error("Failed to fetch WhatsApp contacts and groups", e);
                    throw new RuntimeException("Failed to fetch WhatsApp data", e);
                }
            }, taskExecutor);
        }
        
        if (telegramFetchContactsEnabled) {
            telegramFuture = CompletableFuture.runAsync(() -> {
                try {
                    logger.info("Fetching Telegram contacts and groups...");
                    telegramService.fetchAllContactsAndGroups();
                    logger.info("Successfully fetched Telegram contacts and groups");
                } catch (Exception e) {
                    logger.error("Failed to fetch Telegram contacts and groups", e);
                    throw new RuntimeException("Failed to fetch Telegram data", e);
                }
            }, taskExecutor);
        }
        
        return CompletableFuture.allOf(whatsAppFuture, telegramFuture)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Error fetching contacts and groups", ex);
                } else {
                    logger.info("Successfully fetched all contacts and groups");
                }
            });
    }

    /**
     * Executes a task with retry logic.
     * @param task The task to execute
     * @return CompletableFuture representing the task execution with retries
     */
    private CompletableFuture<Void> runWithRetry1(WATGCommonUtils task) {
        return CompletableFuture.runAsync(() -> {
            int attempt = 0;
            Exception lastException = null;
            
            while (attempt < maxRetries) {
                try {
                    // Since WATGCommonUtils doesn't have a run() method, we need to call the appropriate method
                    // For example, if the task is a Runnable, we can cast it
                    if (task instanceof Runnable) {
                        ((Runnable) task).run();
                    } else {
                        // If it's not a Runnable, try to call a common method like execute()
                        // This assumes there's an execute() method in WATGCommonUtils
                        task.getClass().getMethod("execute").invoke(task);
                    }
                    logger.info("Task {} completed successfully on attempt {}", 
                        task.getClass().getSimpleName(), attempt + 1);
                    return; // Success - exit the retry loop
                } catch (Exception e) {
                    lastException = e;
                    attempt++;
                    logger.warn("Attempt {}/{} failed for task {}: {}", 
                        attempt, maxRetries, task.getClass().getSimpleName(), e.getMessage());
                    
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", ie);
                        }
                    }
                }
            }
            
            // If we get here, all retries failed
            String errorMsg = String.format("Task %s failed after %d attempts: %s", 
                task.getClass().getSimpleName(), maxRetries, lastException != null ? lastException.getMessage() : "");
            logger.error(errorMsg, lastException);
            throw new RuntimeException(errorMsg, lastException);
        }, taskExecutor);
    }



}
