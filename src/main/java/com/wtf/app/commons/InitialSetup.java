package com.wtf.app.commons;


import com.wtf.app.model.dto.FacebookDTO;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.dto.TelegramDTO;
import com.wtf.app.model.dto.WhatsappDTO;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import jakarta.inject.Provider;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component("initialSetup")
public class InitialSetup {

    private static final Logger logger = LogManager.getLogger(InitialSetup.class);

    private final Provider<WebDriver> webDriverProvider;
    private final Provider<JavascriptExecutor> javascriptExecutorProvider;
    private final WhatsappDTO whatsappDTO;
    private final TelegramDTO telegramDTO;
    private final FacebookDTO facebookDTO;
    @Autowired
    private ParallelWebDriverManager parallelWebDriverManager;

    private final Map<SocialType, SocialModel> socialTypeToSocialModel = new ConcurrentHashMap<>();


    @Autowired
    public InitialSetup(
                      @Qualifier("webDriver") Provider<WebDriver> webDriverProvider,
                        Provider<JavascriptExecutor> javascriptExecutorProvider,
                      WhatsappDTO whatsappDTO, 
                      TelegramDTO telegramDTO, 
                      FacebookDTO facebookDTO) {
        this.webDriverProvider = webDriverProvider;
        this.javascriptExecutorProvider = javascriptExecutorProvider;
        this.whatsappDTO = whatsappDTO;
        this.telegramDTO = telegramDTO;
        this.facebookDTO = facebookDTO;

        initialize();
    }

    private void initialize() {
        socialTypeToSocialModel.put(SocialType.WHATSAPP, whatsappDTO);
        socialTypeToSocialModel.put(SocialType.FACEBOOK, facebookDTO);
        socialTypeToSocialModel.put(SocialType.TELEGRAM, telegramDTO);
    }

    public WebDriver setupAndExecuteSocialBySocialType(SocialType socialType) {
        if(ThreadLocalAutomationContext.getContext()!=null && ThreadLocalAutomationContext.getContext().getTaskType()!=null) {
            socialType = ThreadLocalAutomationContext.getContext().getTaskType().getSocialType();
        }
        String baseUrl = socialTypeToSocialModel.get(ThreadLocalAutomationContext.getContext().getTaskType().getSocialType()).getBaseURL();

        logger.info("Initializing new browser session for social type: {}", socialType);

        try {
            // Get a new WebDriver instance for this operation
            //WebDriver driver = webDriverProvider.get();
            WebDriver driver = parallelWebDriverManager.getCurrentDriver();
            // Initialize the driver
            driver.get(baseUrl);
            logger.info("Initial Setup:ThreadLocalAutomationContext.getSocialType() {} driverUrl {}", ThreadLocalAutomationContext.getContext().getTaskType().getSocialType(), driver.getCurrentUrl());
            handleSocialTypeWait();
            return driver;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error initializing browser: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }

    }

    /**
     * Safely sets up and returns a WebDriver instance for the specified social type.
     * Includes proper resource management and error handling.
     */
    private WebDriver setupAndGetDriver() throws InterruptedException {
        WebDriver driver = null;
        try {
            // Get WebDriver instance from the parallel manager
            driver = parallelWebDriverManager.getCurrentDriver();
            SocialType socialType = ThreadLocalAutomationContext.getContext().getTaskType().getSocialType();
            logger.info("Setting up new browser session for {}", socialType);

            // Navigate to the appropriate URL
            String baseUrl = socialTypeToSocialModel.get(socialType).getBaseURL();
            logger.debug("Navigating to URL: {}", baseUrl);
            
            driver.get(baseUrl);
            handleSocialTypeWait();
            
            // Log successful setup
            logger.info("Successfully initialized WebDriver for {} with session ID: {}", 
                socialType, ((RemoteWebDriver) driver).getSessionId());
                
            return driver;
        } catch (Exception e) {
            logger.error("Error during WebDriver setup: {}", e.getMessage(), e);
            
            // Ensure WebDriver is properly cleaned up on failure
            if (driver != null) {
                try {
                    logger.info("Cleaning up WebDriver after setup failure");
                    driver.quit();
                } catch (Exception ex) {
                    logger.error("Error during WebDriver cleanup after setup failure", ex);
                }
            }
            
            // Rethrow the original exception
            throw new RuntimeException("Failed to initialize WebDriver: " + e.getMessage(), e);
        }
    }

    private /*synchronized*/ void handleSocialTypeWait() throws InterruptedException {
        //WebDriver driver = webDriverProvider.get();
        WebDriver driver  = parallelWebDriverManager.getCurrentDriver();
        if (driver == null) return;
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20000));  // max 5 minutes max for each wait

        SocialType socialType = ThreadLocalAutomationContext.getContext().getTaskType().getSocialType();
        try {
            switch (socialType) {
                case FACEBOOK:
                    logger.info("Waiting for Facebook to be ready...");
                    Thread.sleep(10000);  // 30 seconds for Facebook
                    wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState")
                            .equals("complete"));
                    break;

                case TELEGRAM:
                    logger.info("Waiting for Telegram to be ready...");
                    Thread.sleep(2000);  // 20 seconds for Telegram
                    break;

                case WHATSAPP:
                    logger.info("Waiting for WhatsApp to be ready...");
                    Thread.sleep(5000);  // 3 minutes for WhatsApp QR code scan
                    // Wait for WhatsApp to be ready
                    /*int maxRetries = 3;
                    int retryCount = 0;
                    boolean elementFound = false;
                    
                    while (retryCount < maxRetries && !elementFound) {
                        try {
                            logger.info("Attempt {} of {} - Waiting for chat list to be ready...", 
                                retryCount + 1, maxRetries);
                            wait.until(ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//div[@data-testid='chat-list']")));
                            logger.info("Chat list found successfully after attempt {}", retryCount + 1);
                            elementFound = true;
                        } catch (TimeoutException e) {
                            retryCount++;
                            if (retryCount < maxRetries) {
                                logger.warn("Chat list not found on attempt {}. Retrying in 30 seconds...", retryCount);
                                Thread.sleep(30000); // 30 second wait between retries
                            } else {
                                logger.warn("Chat list not found after {} attempts. Continuing anyway...", maxRetries);
                            }
                        }
                    }*/
                    break;//Breakout for whatsapp

                default:
                    logger.warn("Unknown social type: " + socialType);
                    Thread.sleep(30000);  // Default wait
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            logger.warn("Wait interrupted for " + socialType + ": " + e.getMessage());
            throw e;
        }
    }

    // Method to safely close browser
    public void closeBrowser() {
        //WebDriver driver = webDriverProvider.get();
        WebDriver driver  = parallelWebDriverManager.getCurrentDriver();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("Browser session closed successfully");
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error closing browser: " + e.getMessage(), e);
            } finally {
            }
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public WebDriver getDriver() {
       // WebDriver driver  = webDriverProvider.get();
        WebDriver driver  = parallelWebDriverManager.getCurrentDriver();
        if (driver == null) {
            logger.warn("No active WebDriver session in request context. Creating a new one...");
            try {
                driver = setupAndGetDriver();
                if (driver != null) {
                    logger.info("WebDriver session created successfully");
                }else {
                    logger.error("Failed to create WebDriver instance, creating manual");
                    driver = new ChromeDriver(parallelWebDriverManager.getHumanLikeOptions());

                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to create WebDriver instance", e);
                throw new IllegalStateException("Failed to create WebDriver instance", e);
            }
        }
        return driver;
    }

    public String getBaseUrl() {
        return socialTypeToSocialModel.get(ThreadLocalAutomationContext.getContext().getTaskType().getSocialType()).getBaseURL();
    }

    public SocialType getSocialType() {
        return socialTypeToSocialModel.get(ThreadLocalAutomationContext.getContext().getTaskType().getSocialType()).getSocialType();
    }

    public Map<SocialType, SocialModel> getSocialTypeToSocialModel() {
        return socialTypeToSocialModel;
    }

    public String getConsultantsPhoneNumbersFile() {
        return whatsappDTO.getConsultantsPhoneNumbersFile();
    }
}