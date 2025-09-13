package com.wtf.app.service.parallel;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.base.BaseParallelAutomation;
import com.wtf.app.service.impls.WATGFetchAllWAContactsWGroupNamesService;
import com.wtf.app.service.impls.starter.service.whatsApp.WhatsappBroadcastSelfSkillsToWAGroupsStarter;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import com.wtf.app.web.driver.WebDriverHealthService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.SessionId;

/**
 * Service for running WhatsApp automations in parallel with comprehensive error handling.
 */
@Service
public class WhatsAppAutomationService extends BaseParallelAutomation {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppAutomationService.class);
    private static final String PROFILE_NAME = "whatsapp-profile";
    private static final int MAX_RETRIES = 3;
    private static final int ELEMENT_WAIT_SECONDS = 120;

    @Value("${whatsapp.web.url:https://web.whatsapp.com}")
    private String whatsappWebUrl;

    @Value("${whatsapp.qr.wait.seconds:1200}")
    private int qrWaitSeconds;

    @Value("${whatsapp.group.broadcast.enabled:true}")
    private boolean broadcastEnabled;

    private final InitialSetup initialSetup;
    @Qualifier("automationTaskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ApplicationContext applicationContext;
    private final ParallelWebDriverManager webDriverManager;
    private final WebDriverHealthService webDriverHealthService;
    private final WATGFetchAllWAContactsWGroupNamesService watgBFetchAllWAContactsWGroupNamesService;

    @Autowired
    public WhatsAppAutomationService(InitialSetup initialSetup,
                                     @Qualifier("automationTaskExecutor") ThreadPoolTaskExecutor taskExecutor, 
                                     ApplicationContext applicationContext,
                                     ParallelWebDriverManager parallelWebDriverManager,
                                     WebDriverHealthService webDriverHealthService,
                                     @Qualifier("whatsAppFetchService") WATGFetchAllWAContactsWGroupNamesService watgBFetchAllWAContactsWGroupNamesService) {
        super(taskExecutor, applicationContext);
        this.initialSetup = initialSetup;
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
        this.webDriverManager = parallelWebDriverManager;
        this.webDriverHealthService = webDriverHealthService;
        this.watgBFetchAllWAContactsWGroupNamesService = watgBFetchAllWAContactsWGroupNamesService;
        logger.info("WhatsAppAutomationService initialized with fetchService instance: {}", this.watgBFetchAllWAContactsWGroupNamesService.hashCode());
    }

    /**
     * Main entry point for starting all WhatsApp automation tasks.
     * @param context The automation context to be used for the tasks.
     */
    public void start(AutomationContext context) {
        logger.info("Starting WhatsApp automation with context: {}", context.getTaskId());
        // Here you would orchestrate the different tasks, for example:
        // 1. Fetch contacts
        // 2. Broadcast messages
        // For now, we'll just log a message.
        logger.info("WhatsApp automation tasks would run here.");
    }

    /**
     * Creates a new WebDriver instance with proper session validation and error handling.
     * @return A properly initialized WebDriver instance
     * @throws IllegalStateException if the WebDriver cannot be created or validated
     */
    private WebDriver createNewWebDriverInstance(TaskType taskType) {
        try {
            // This will block until the WebDriver is created
            logger.info("Creating new WebDriver instance");
            return webDriverManager.createDriver(taskType);
        } catch (Exception e) {
            logger.error("Error creating WebDriver instance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create WebDriver: " + e.getMessage(), e);
        }
    }

    /**
     * Configures timeouts for the WebDriver instance.
     * @param driver The WebDriver instance to configure
     * @return true if configuration was successful, false otherwise
     */
    private boolean configureTimeouts(WebDriver driver) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ELEMENT_WAIT_SECONDS));
            logger.debug("Set implicit wait to {} seconds", ELEMENT_WAIT_SECONDS);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to set implicit wait: {}", e.getMessage());
            return isSessionActive(driver);
        }
    }

    /**
     * Configures the browser window.
     * @param driver The WebDriver instance to configure
     * @return true if configuration was successful, false otherwise
     */
    private boolean configureWindow(WebDriver driver) {
        try {
            driver.manage().window().maximize();
            logger.debug("Maximized browser window");
            return true;
        } catch (Exception e) {
            logger.warn("Failed to maximize window: {}", e.getMessage());
            return isSessionActive(driver);
        }
    }

    /**
     * Safely checks if a WebDriver session is active.
     * @param driver The WebDriver instance to check
     * @return true if the session is active, false otherwise
     */
    private boolean isSessionActive(WebDriver driver) {
        // Use the health service to check session status
        return webDriverHealthService.checkSessionHealth(TaskType.WHATSAPP_BROADCAST_AD_TO_CONSULTANTS);
    }

    /**
     * Safely quits the WebDriver instance if it's not null.
     * @param driver The WebDriver instance to quit
     */
    private void safeQuitDriver(WebDriver driver) {
        logger.debug("Quitting WebDriver instance");
        webDriverManager.quitDriver(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
    }

    /**
     * Logs detailed information about the WebDriver instance for debugging purposes.
     * @param driver The WebDriver instance to log information about
     */
    private void logDriverInfo(WebDriver driver) {
        if (driver == null) {
            logger.warn("Cannot log driver info: driver is null");
            return;
        }

        try {
            logger.debug("WebDriver Implementation: {}", driver.getClass().getName());

            if (driver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
                SessionId sessionId = remoteDriver.getSessionId();
                logger.debug("Session ID: {}", sessionId != null ? sessionId.toString() : "null");

                try {
                    String currentUrl = driver.getCurrentUrl();
                    logger.debug("Current URL: {}", currentUrl);
                } catch (Exception e) {
                    logger.debug("Could not get current URL: {}", e.getMessage());
                }

                try {
                    String title = driver.getTitle();
                    logger.debug("Page title: {}", title);
                } catch (Exception e) {
                    logger.debug("Could not get page title: {}", e.getMessage());
                }

                try {
                    Object capabilities = remoteDriver.getCapabilities();
                    logger.debug("Capabilities: {}", capabilities != null ? capabilities.toString() : "null");
                } catch (Exception e) {
                    logger.debug("Could not get capabilities: {}", e.getMessage());
                }
            } else {
                logger.debug("Driver is not a RemoteWebDriver instance");
            }
        } catch (Exception e) {
            logger.warn("Error logging WebDriver information: {}", e.getMessage());
        }
    }

    /**
     * Run the WhatsApp broadcast message to all groups with retry mechanism.
     */
    public CompletableFuture<Boolean> runBroadcastToAllGroups(AutomationContext context) {
        logger.info("Starting WhatsApp broadcast to all groups...");

        return CompletableFuture.supplyAsync(() -> {
            WebDriver driver = null;
            try {
                driver = createNewWebDriverInstance(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
                // Initialize services with the new WebDriver
                WhatsappBroadcastSelfSkillsToWAGroupsStarter broadcastService =
                    getServiceInstance(WhatsappBroadcastSelfSkillsToWAGroupsStarter.class, driver);

                if (broadcastEnabled) {
                    logger.info("Starting broadcast to all WhatsApp groups...");
                    broadcastService.start(context);
                    logger.info("WhatsApp broadcast completed successfully");
                    return true;
                } else {
                    logger.info("Broadcast is disabled in configuration");
                    return false;
                }
            } catch (Exception e) {
                logger.error("Error in WhatsApp broadcast automation", e);
                throw new CompletionException(e);
            } finally {
                if (driver != null) {
                    logDriverInfo(driver);
                    safeQuitDriver(driver);
                }
            }
        }, taskExecutor);
    }

    public CompletableFuture<Boolean> broadcastToAllGroups() {
        logger.info("Starting to broadcast to all WhatsApp contacts and groups");

        return CompletableFuture.supplyAsync(() -> {
            WebDriver driver = null;
            try {
                driver = createNewWebDriverInstance(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
                // Initialize services with the new WebDriver
                WATGFetchAllWAContactsWGroupNamesService broadcastService =
                        getServiceInstance(WATGFetchAllWAContactsWGroupNamesService.class, driver);

                // Set the social model for WhatsApp
                SocialModel whatsappModel = initialSetup.getSocialTypeToSocialModel().get(SocialType.WHATSAPP);
                if (whatsappModel == null) {
                    throw new IllegalStateException("WhatsApp social model not found in initial setup");
                }

                // Set the social model and return the result of the fetch operation
                broadcastService.setSocialModel(whatsappModel);
                logger.info("socialType : " + broadcastService.getSocialModel().getSocialType() + "...baseURL : "+ broadcastService.getSocialModel().getBaseURL());
                broadcastService.setxPathInterface(whatsappModel.getxPathInterface());
                broadcastService.setBaseUrl(whatsappModel.getBaseURL());
                broadcastService.setTaskTypeMapping(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS,broadcastService);
                broadcastService.traverseGroupsTemplate(broadcastService);

                return true;
            } catch (Exception e) {
                logger.error("Error in broadcastToAllGroups", e);
                throw new CompletionException(e);
            } finally {
                if (driver != null) {
                    safeQuitDriver(driver);
                }
            }
        }, taskExecutor);
    }

    /**
     * Fetch all WhatsApp contacts and group names.
     * This method is designed to run asynchronously with proper context propagation.
     */
    /**
     * Fetch all WhatsApp contacts and group names.
     * This method is designed to run asynchronously with proper context propagation.
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Boolean> fetchAllContactsAndGroups() {
        logger.info("Starting to fetch all WhatsApp contacts and groups");

        return CompletableFuture.supplyAsync(() -> {
            WebDriver driver = null;
            try {
                driver = createNewWebDriverInstance(TaskType.WHATSAPP_FETCH_CONTACTS);
                logger.info("Using fetchService instance: {} for WhatsApp", watgBFetchAllWAContactsWGroupNamesService.hashCode());

                // Set the social model for WhatsApp
                SocialModel whatsappModel = initialSetup.getSocialTypeToSocialModel().get(SocialType.WHATSAPP);
                if (whatsappModel == null) {
                    throw new IllegalStateException("WhatsApp social model not found in initial setup");
                }

                // Set the social model and return the result of the fetch operation
                watgBFetchAllWAContactsWGroupNamesService.setSocialModel(whatsappModel);
                logger.info("socialType : " + watgBFetchAllWAContactsWGroupNamesService.getSocialModel().getSocialType() + "...baseURL : "+ watgBFetchAllWAContactsWGroupNamesService.getSocialModel().getBaseURL());
                watgBFetchAllWAContactsWGroupNamesService.setxPathInterface(whatsappModel.getxPathInterface());
                watgBFetchAllWAContactsWGroupNamesService.setBaseUrl(whatsappModel.getBaseURL());
                watgBFetchAllWAContactsWGroupNamesService.setTaskTypeMapping(TaskType.WHATSAPP_FETCH_CONTACTS,watgBFetchAllWAContactsWGroupNamesService);
                watgBFetchAllWAContactsWGroupNamesService.traverseGroupsTemplate(watgBFetchAllWAContactsWGroupNamesService);

                return true;
            } catch (Exception e) {
                logger.error("Error in fetchAllContactsAndGroups", e);
                throw new CompletionException(e);
            } finally {
                if (driver != null) {
                    safeQuitDriver(driver);
                }
            }
        }, taskExecutor);
    }


    /**
     * Navigates to WhatsApp Web and waits for the user to scan the QR code.
     *
     * @param driver The WebDriver instance to use for navigation
     * @throws RuntimeException if navigation fails or QR code scan times out
     */
    private void navigateToWhatsAppWeb(WebDriver driver) {
        try {
            logger.info("Navigating to WhatsApp Web...");
            driver.get(whatsappWebUrl);

            // Wait for the user to scan the QR code
            logger.info("Please scan the QR code to log in to WhatsApp Web...");

            // Create a new WebDriverWait instance with the configured timeout
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(qrWaitSeconds));

            // Try multiple selectors in case WhatsApp changes their UI
            try {
                // First try the standard chat list selector from WhatsApp_Xpaths
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@aria-label='Chat list']/div")
                ));
                logger.debug("Found chat list using primary selector");
            } catch (TimeoutException e) {
                try {
                    // Fallback to the data-testid selector
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div[data-testid='chat-list']")
                    ));
                    logger.debug("Found chat list using data-testid selector");
                } catch (TimeoutException ex) {
                    // Try one more fallback selector
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[contains(@class, 'two') and contains(@class, 'chat-list')]")
                    ));
                    logger.debug("Found chat list using class-based selector");
                }
            }

            // Additional check to ensure the UI is fully loaded and interactive
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='textbox' and @contenteditable='true']")
            ));

            logger.info("Successfully logged in to WhatsApp Web");

            // Additional wait to ensure the UI is fully loaded
            Thread.sleep(2000);

        } catch (TimeoutException e) {
            takeScreenshot(driver, "whatsapp-login-error-" + System.currentTimeMillis());
            logger.error("Screenshot saved to: screenshots/whatsapp-login-error-" + System.currentTimeMillis() + ".png");
            throw new RuntimeException("QR code scan timed out after " + qrWaitSeconds + " seconds. " +
                "Please check if you've scanned the QR code and that WhatsApp Web is fully loaded.", e);
        } catch (Exception e) {
            logger.error("Failed to navigate to WhatsApp Web", e);
            throw new RuntimeException("Failed to navigate to WhatsApp Web: " + e.getMessage(), e);
        }
    }

    /**
     * Takes a screenshot of the current browser window.
     *
     * @param driver The WebDriver instance to use for taking the screenshot
     * @param fileName The base name for the screenshot file (without extension)
     */
    private void takeScreenshot(WebDriver driver, String fileName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File screenshotsDir = new File("screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }
            FileUtils.copyFile(screenshot, new File(screenshotsDir, fileName + ".png"));
        } catch (Exception e) {
            logger.error("Failed to take screenshot: " + e.getMessage(), e);
        }
    }

    /**
     * Waits for the QR code to be scanned and the chat list to appear.
     *
     * @param driver The WebDriver instance to use
     * @param qrWaitSeconds Maximum time to wait for the QR code to be scanned (in seconds)
     * @return A CompletableFuture that completes when the QR code is scanned or fails with an exception
     */
private CompletableFuture<Void> waitForQrCodeScan(WebDriver driver, int qrWaitSeconds) {
    return CompletableFuture.runAsync(() -> {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(qrWaitSeconds));
            // Wait for the chat list to appear, indicating successful login
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@aria-label='Chat list']/div")));

            // Additional wait to ensure the app is fully loaded
            Thread.sleep(2000);

        } catch (TimeoutException e) {
            // Take a screenshot for debugging
            takeScreenshot(driver, "whatsapp-login-error-" + System.currentTimeMillis());
            logger.error("Screenshot saved to: screenshots/whatsapp-login-error-" + System.currentTimeMillis() + ".png");
            throw new RuntimeException("QR code scan timed out after " + qrWaitSeconds + " seconds. " +
                "Please check if you've scanned the QR code and that WhatsApp Web is fully loaded.", e);
        } catch (Exception e) {
            throw new CompletionException(new RuntimeException("Error during QR code scan wait", e));
        }
    }, taskExecutor);
}
    /**
     * Executes a task with retry logic.
     *
     * @param <T> The type of result returned by the task
     * @param task The task to execute
     * @param taskName The name of the task for logging purposes
     * @return The result of the task
     * @throws RuntimeException if all retry attempts fail
     */
    private <T> T executeWithRetry(Supplier<T> task, String taskName) {
        int attempts = 0;
        Exception lastError = null;

        while (attempts < MAX_RETRIES) {
            try {
                logger.info("Executing {} (attempt {}/{})", taskName, attempts + 1, MAX_RETRIES);
                return task.get();
            } catch (Exception e) {
                lastError = e;
                logger.warn("Error executing {} (attempt {}/{}): {}",
                           taskName, attempts + 1, MAX_RETRIES, e.getMessage());

                if (attempts < MAX_RETRIES - 1) {
                    try {
                        // Exponential backoff
                        long delay = (long) Math.pow(2, attempts) * 1000;
                        logger.info("Retrying in {} ms...", delay);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Task execution was interrupted", ie);
                    }
                }
                attempts++;
            }
        }
        throw new RuntimeException("Failed to execute task " + taskName + " after " + MAX_RETRIES + " attempts", lastError);
    }
    @Override
    protected <T> T getServiceInstance(Class<T> serviceClass, WebDriver driver) {
        // For broadcast service, we can still use the singleton instance from context
        if (serviceClass.isAssignableFrom(WhatsappBroadcastSelfSkillsToWAGroupsStarter.class)) {
            return applicationContext.getBean(serviceClass);
        }
        // For fetch service, return the prototype instance we've already injected.
        if (serviceClass.isAssignableFrom(WATGFetchAllWAContactsWGroupNamesService.class)) {
            return (T) this.watgBFetchAllWAContactsWGroupNamesService;
        }
        // Fallback or error handling
        throw new IllegalArgumentException("Unsupported service class: " + serviceClass.getName());
    }
}
