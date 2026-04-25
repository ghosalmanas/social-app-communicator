package com.wtf.app.service.impls;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.*;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.parent.WATGParent;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for searching and extracting "I am looking for" messages from Telegram.
 * This service extends WATGParent to leverage the existing traversal infrastructure.
 */
public class TG_SearchAndExtractLookingFor extends WATGParent {

    private static final Logger logger = LogManager.getLogger(TG_SearchAndExtractLookingFor.class);
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private final int instanceId = instanceCounter.incrementAndGet();

    private static final FlowType enumFlowType = FlowType.SEARCH_AND_EXTRACT_LOOKING_FOR;

    // Constants
    private static final String DEFAULT_CONTACT_NAME = "@akashvijaykumarfs";
    private static final String EXTRACTED_MESSAGES_FILE = "dbfiles/telegram_extracted_messages.txt";
    private static final String SEARCH_PHRASE = "\"I am looking for\"";
    private static final String NO_NEW_POSTS_MESSAGE = "Telegram: No New Post with 'Looking for Support' Found";
    private static final int SEARCH_WAIT_MS = 1000;
    private static final int CONTACT_SELECT_WAIT_MS = 2000;
    private static final int MESSAGE_SEND_WAIT_MS = 2000;
    private static final int SCROLL_WAIT_MS = 200;
    private static final int MAX_NO_NEW_RESULTS = 10;
    private static final int MAX_SCROLLS = 50;

    private final ThreadLocal<Set<String>> extractedMessagesThreadLocal = ThreadLocal.withInitial(LinkedHashSet::new);

    @Autowired
    public TG_SearchAndExtractLookingFor(InitialSetup initialSetup,
                                         ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup, parallelWebDriverManager);
        logger.info("TG_SearchAndExtractLookingFor instance with instanceId :{} initialized", instanceId);
    }

    /**
     * Starts the search and extract process for "I am looking for" messages.
     */
    public CompletableFuture<Void> start(AutomationContext context, TaskType taskType) {
        return CompletableFuture.runAsync(() -> {
            try {
                SocialType socialType = taskType.getSocialType();
                SocialModel socialModel = initialSetup.getSocialTypeToSocialModel().get(socialType);
                
                // Set context for the thread
                context.setInstance(this);
                context.setSocialModel(socialModel);
                ThreadLocalAutomationContext.setContext(context);

                this.setSocialType(taskType.getSocialType());
                this.setFlowTypeEnum(enumFlowType);
                this.setSocialModel(socialModel);
                this.setxPathInterface(socialModel.getxPathInterface());
                this.setxPathInterfaceWATG((XPathInterfaceWATG) socialModel.getxPathInterface());
                this.setBaseUrl(socialModel.getBaseURL());
                this.setTaskTypeMapping(taskType, this);

                // Execute the search and extract logic
                searchAndExtractLookingForMessages(this);
            } catch (Exception e) {
                logger.error("Error during TG_SearchAndExtractLookingFor execution", e);
                throw new RuntimeException("Search and extract failed", e);
            } finally {
                // Cleanup thread-local context
                try {
                    ThreadLocalAutomationContext.clear();
                    logger.info("Thread-local context cleared");
                } catch (Exception e) {
                    logger.error("Error clearing thread-local context: {}", e.getMessage());
                }
            }
        }).whenComplete((result, ex) -> {
            // Callback when thread completes (success or failure)
            if (ex == null) {
                logger.info("Telegram search and extract thread completed successfully");
            } else {
                logger.error("Telegram search and extract thread completed with error", ex);
            }
        });
    }

    /**
     * Helper method to clear search input and type search text.
     * Ensures the search input is completely cleared before typing.
     * @param driver WebDriver instance
     * @param searchText Text to search for
     * @return WebElement for the search input
     */
    private WebElement clearAndSearchInput(WebDriver driver, String searchText) throws InterruptedException {
        driver.findElement(By.xpath(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH())).click();
        Thread.sleep(SEARCH_WAIT_MS);
        
        WebElement searchInput = driver.findElement(By.xpath(getxPathInterface().getXPATH_SEARCH()));
        
        searchInput.clear();
        Thread.sleep(200);
        searchInput.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
        Thread.sleep(100);
        searchInput.sendKeys(Keys.DELETE);
        Thread.sleep(200);
        searchInput.clear();
        Thread.sleep(300);
        
        searchInput.sendKeys(searchText);
        Thread.sleep(SEARCH_WAIT_MS);
        
        return searchInput;
    }

    /**
     * Selects a contact from search results using multiple fallback methods.
     * @param driver WebDriver instance
     * @param searchInput Search input WebElement
     * @param contactName Contact name to select
     * @return true if contact was selected successfully
     */
    private boolean selectContact(WebDriver driver, WebElement searchInput, String contactName) throws InterruptedException {
        logger.info("Selecting contact: {}", contactName);
        boolean contactSelected = false;
        
        // Method 1: ARROW_DOWN then ENTER
        try {
            searchInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN, Keys.ENTER);
            Thread.sleep(500);
            Thread.sleep(CONTACT_SELECT_WAIT_MS);
            
            try {
                driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
                contactSelected = true;
                logger.info("Contact selected successfully using ARROW_DOWN method");
            } catch (Exception e) {
                logger.warn("Chat did not open with ARROW_DOWN method");
            }
        } catch (Exception e) {
            logger.warn("ARROW_DOWN method failed: {}", e.getMessage());
        }
        
        // Method 2: Click on search result directly
        if (!contactSelected) {
            try {
                String contactNameOnly = contactName.replace("@", "");
                logger.info("Trying to click on search result directly");
                WebElement searchResult = driver.findElement(By.xpath(String.format(getxPathInterface().getXPATH_SEARCH_RESULT_BY_CONTACT_NAME(), contactNameOnly)));
                searchResult.click();
                Thread.sleep(CONTACT_SELECT_WAIT_MS);
                
                try {
                    driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
                    contactSelected = true;
                    logger.info("Contact selected successfully using direct click");
                } catch (Exception e) {
                    logger.warn("Chat did not open with direct click");
                }
            } catch (Exception e) {
                logger.warn("Direct click method failed: {}", e.getMessage());
            }
        }
        
        return contactSelected;
    }

    /**
     * Attempts to click the send button using multiple XPath fallbacks.
     * @param driver WebDriver instance
     * @return true if send button was clicked successfully
     */
    private boolean clickSendButton(WebDriver driver) throws InterruptedException {
        String[] sendButtonXPaths = {
            getxPathInterface().getSPAN_DATA_TESTID_SEND(),
            "//button[@title='Send Message']",
            "//button[@aria-label='Send message']",
            "//button[contains(@class, 'send')]",
            "//div[@class='input-field-wrapper']//button",
            "//button[@data-icon='send']",
            "//span[@data-icon='send']"
        };
        
        for (String xpath : sendButtonXPaths) {
            try {
                WebElement sendButton = driver.findElement(By.xpath(xpath));
                if (sendButton.isDisplayed() && sendButton.isEnabled()) {
                    sendButton.click();
                    Thread.sleep(MESSAGE_SEND_WAIT_MS);
                    logger.info("Successfully clicked send button using XPath: {}", xpath);
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Send button not found with XPath: {}", xpath);
            }
        }
        
        logger.warn("Failed to click send button with any XPath");
        return false;
    }

    /**
     * Checks if a message text matches the "looking for" criteria.
     * @param text Message text to check
     * @return true if message matches criteria
     */
    private boolean isLookingForMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        boolean hasLookingPhrase = lowerText.contains("i am looking for") || lowerText.contains("we are looking for");
        boolean hasKeywords = lowerText.contains("support") || lowerText.contains("interview") || lowerText.contains("proxy");
        
        return hasLookingPhrase && hasKeywords;
    }

    /**
     * Attempts to scroll using multiple fallback methods.
     * @param driver WebDriver instance
     * @param searchInput Search input WebElement (for fallback)
     * @return true if any scroll method succeeded
     */
    private boolean scrollWithFallback(WebDriver driver, WebElement searchInput) throws InterruptedException {
        boolean scrollSuccess = false;
        
        // Method 1: Aggressive JavaScript on LeftSearch--content custom-scroll
        try {
            WebElement scrollablePanel = findScrollablePanel(driver);
            if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
            for (int i = 0; i < 5; i++) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                Thread.sleep(50);
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollBy(0, 3000);", scrollablePanel);
                Thread.sleep(50);
            }
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("var event = new WheelEvent('wheel', {deltaY: 1000, bubbles: true}); arguments[0].dispatchEvent(event);", scrollablePanel);
            Thread.sleep(100);
            scrollSuccess = true;
            logger.debug("Scrolled using aggressive JavaScript on LeftSearch--content custom-scroll");
        } catch (Exception e) {
            logger.debug("Error scrolling LeftSearch--content with JavaScript: {}", e.getMessage());
        }
        
        // Method 2: Parent element scroll
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                WebElement parentPanel = scrollablePanel.findElement(By.xpath(".."));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", parentPanel);
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using JavaScript on parent of LeftSearch--content");
            } catch (Exception e) {
                logger.debug("Error scrolling parent of LeftSearch--content: {}", e.getMessage());
            }
        }
        
        // Method 3: Actions PAGE_DOWN
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                actions.moveToElement(scrollablePanel).sendKeys(org.openqa.selenium.Keys.PAGE_DOWN).perform();
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using Actions PAGE_DOWN on LeftSearch--content");
            } catch (Exception e) {
                logger.debug("Error scrolling LeftSearch--content with Actions: {}", e.getMessage());
            }
        }
        
        // Method 4: Window scroll
        if (!scrollSuccess) {
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using window.scrollBy");
            } catch (Exception e) {
                logger.debug("Error scrolling window: {}", e.getMessage());
            }
        }
        
        // Method 5: Document.body scroll
        if (!scrollSuccess) {
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("document.body.scrollTop = document.body.scrollHeight;");
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using document.body.scrollTop");
            } catch (Exception e) {
                logger.debug("Error scrolling document.body: {}", e.getMessage());
            }
        }
        
        // Method 6: Mouse wheel simulation
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                actions.moveToElement(scrollablePanel).click().perform();
                Thread.sleep(100);
                for (int i = 0; i < 3; i++) {
                    actions.sendKeys(org.openqa.selenium.Keys.PAGE_DOWN).perform();
                    Thread.sleep(100);
                }
                scrollSuccess = true;
                logger.debug("Scrolled using mouse wheel simulation with PAGE_DOWN");
            } catch (Exception e) {
                logger.debug("Error scrolling with mouse wheel simulation: {}", e.getMessage());
            }
        }
        
        // Method 7: Custom-scroll container
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = driver.findElement(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN()));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using JavaScript on custom-scroll container");
            } catch (Exception e) {
                logger.debug("Error scrolling panel with JavaScript: {}", e.getMessage());
            }
        }
        
        // Method 8: Inner custom-scroll element
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = driver.findElement(By.xpath("//*[contains(@class, 'custom-scroll')]"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using JavaScript on inner custom-scroll element");
            } catch (Exception e) {
                logger.debug("Error scrolling inner panel with JavaScript: {}", e.getMessage());
            }
        }
        
        // Method 9: Chat-list element
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = driver.findElement(By.xpath("//*[contains(@class, 'chat-list')]"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using JavaScript on chat-list element");
            } catch (Exception e) {
                logger.debug("Error scrolling chat-list with JavaScript: {}", e.getMessage());
            }
        }
        
        // Method 10: PAGE_DOWN on scrollable panel
        if (!scrollSuccess) {
            try {
                WebElement scrollablePanel = driver.findElement(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN()));
                scrollablePanel.sendKeys(org.openqa.selenium.Keys.PAGE_DOWN);
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using PAGE_DOWN on custom-scroll container");
            } catch (Exception e) {
                logger.debug("Error scrolling panel with PAGE_DOWN: {}", e.getMessage());
            }
        }
        
        // Method 11: Actions class with PAGE_DOWN
        if (!scrollSuccess) {
            try {
                org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                WebElement scrollablePanel = driver.findElement(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN()));
                actions.moveToElement(scrollablePanel).sendKeys(org.openqa.selenium.Keys.PAGE_DOWN).perform();
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using Actions class with PAGE_DOWN");
            } catch (Exception e) {
                logger.debug("Error scrolling with Actions class: {}", e.getMessage());
            }
        }
        
        // Method 12: Search input scroll as last resort
        if (!scrollSuccess) {
            try {
                searchInput.sendKeys(org.openqa.selenium.Keys.PAGE_DOWN);
                Thread.sleep(SCROLL_WAIT_MS);
                scrollSuccess = true;
                logger.debug("Scrolled using search input PAGE_DOWN");
            } catch (Exception e) {
                logger.debug("Alternative scroll also failed: {}", e.getMessage());
            }
        }
        
        return scrollSuccess;
    }

    /**
     * Sends a startup notification to indicate the service is initializing.
     * This method provides feedback that the Telegram search and extract service has started.
     * @param driver WebDriver instance
     */
    private void sendStartupNotification(WebDriver driver) {
        String contactName = DEFAULT_CONTACT_NAME;
        String startupMessage = String.format(
            "Telegram Search & Extract Service - Initialization Started for Phrase: 'I am looking for' on 'support', 'interview', or 'proxy' with Timestamp: %s",
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        logger.info("Sending startup notification to contact: {}", contactName);
        
        try {
            WebElement searchInput = clearAndSearchInput(driver, contactName);
            
            if (!selectContact(driver, searchInput, contactName)) {
                logger.warn("Could not select contact for startup notification: {}", contactName);
                return;
            }
            
            WebElement messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
            messageInput.clear();
            messageInput.sendKeys(startupMessage);
            Thread.sleep(1000);
            
            boolean sendSuccess = clickSendButton(driver);
            
            if (sendSuccess) {
                logger.info("Successfully sent startup notification to {}", contactName);
            } else {
                logger.warn("Failed to send startup notification to {}", contactName);
            }
            
        } catch (Exception e) {
            logger.error("Error sending startup notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends extracted messages to a specific phone number.
     * @param driver WebDriver instance
     * @param extractedMessages Set of extracted messages
     */
    private void sendExtractedMessagesToPhoneNumber(WebDriver driver, Set<String> extractedMessages) {
        String contactName = DEFAULT_CONTACT_NAME;
        logger.info("Sending {} extracted messages to contact: {}", extractedMessages.size(), contactName);
        
        Set<String> successfullySentMessages = new HashSet<>();
        
        try {
            WebElement searchInput = clearAndSearchInput(driver, contactName);
            
            if (!selectContact(driver, searchInput, contactName)) {
                throw new Exception("Could not select contact: " + contactName);
            }
            
            WebElement messageInput = null;
            int maxAttempts = 3;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    logger.info("Attempt {} to find message input", attempt);
                    messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
                    logger.info("Message input found on attempt {}", attempt);
                    break;
                } catch (Exception e) {
                    logger.warn("Message input not found on attempt {}: {}", attempt, e.getMessage());
                    if (attempt < maxAttempts) {
                        logger.info("Trying to navigate to next contact");
                        searchInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN, org.openqa.selenium.Keys.ENTER);
                        Thread.sleep(CONTACT_SELECT_WAIT_MS);
                    }
                }
            }
            
            if (messageInput == null) {
                throw new Exception("Could not find message input after " + maxAttempts + " attempts");
            }
            
            int messageCount = 1;
            for (String msg : extractedMessages) {
                try {
                    logger.info("Sending message {}/{}: {}", messageCount, extractedMessages.size(), msg);
                    
                    messageInput.clear();
                    messageInput.sendKeys(msg);
                    Thread.sleep(1000);
                    
                    boolean sendSuccess = clickSendButton(driver);
                    
                    if (sendSuccess) {
                        successfullySentMessages.add(msg);
                        logger.info("Successfully sent message {}/{}", messageCount, extractedMessages.size());
                    } else {
                        logger.warn("Failed to send message {}/{}", messageCount, extractedMessages.size());
                    }
                    
                    messageCount++;
                    Thread.sleep(1000);
                } catch (Exception e) {
                    logger.error("Error sending message {}: {}", messageCount, e.getMessage());
                }
            }
            
            logger.info("Successfully sent {}/{} messages to {}", successfullySentMessages.size(), extractedMessages.size(), contactName);
            
        } catch (Exception e) {
            logger.error("Error sending message to contact {}: {}", contactName, e.getMessage(), e);
        }
        
        if (!successfullySentMessages.isEmpty()) {
            logger.info("Exporting {} successfully sent messages to file", successfullySentMessages.size());
            exportExtractedMessagesToFile(successfullySentMessages);
        } else {
            logger.info("Exporting all {} extracted messages to file (none were successfully sent)", extractedMessages.size());
            exportExtractedMessagesToFile(extractedMessages);
        }
    }

    /**
     * Sends a single message to a contact.
     * @param driver WebDriver instance
     * @param contactName Contact name to send message to
     * @param message Message to send
     */
    private void sendSingleMessage(WebDriver driver, String contactName, String message) {
        logger.info("Sending single message to contact: {}", contactName);
        
        try {
            WebElement searchInput = clearAndSearchInput(driver, contactName);
            
            if (!selectContact(driver, searchInput, contactName)) {
                throw new Exception("Could not select contact: " + contactName);
            }
            
            WebElement messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
            messageInput.sendKeys(message);
            Thread.sleep(500);
            
            boolean sendSuccess = clickSendButton(driver);
            
            if (sendSuccess) {
                logger.info("Successfully sent single message to {}", contactName);
            } else {
                logger.warn("Failed to send single message to {}", contactName);
            }
            
        } catch (Exception e) {
            logger.error("Error sending single message to contact {}: {}", contactName, e.getMessage(), e);
        }
    }

    /**
     * Helper method to find scrollable panel with fallback XPath.
     * @param driver WebDriver instance
     * @return WebElement or null if not found
     */
    private WebElement findScrollablePanel(WebDriver driver) {
        try {
            return driver.findElement(By.xpath("//div[@class='LeftSearch--content custom-scroll']"));
        } catch (Exception e) {
            try {
                return driver.findElement(By.xpath("//div[@class='LeftSearch--content custom-scroll Transition_slide Transition_slide-active']"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * Test method to identify which scroll method works for the custom scrollbar.
     * This method tries all scroll approaches and logs which one succeeds.
     * @param driver WebDriver instance
     */
    public void testScrollMethods(WebDriver driver) {
        logger.info("=== Testing scroll methods for custom scrollbar ===");
        
        try {
            // Wait for page to load
            Thread.sleep(2000);
            
            // Method 1: Aggressive JavaScript on LeftSearch--content custom-scroll
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                for (int i = 0; i < 5; i++) {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                    Thread.sleep(50);
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollBy(0, 1000);", scrollablePanel);
                    Thread.sleep(50);
                }
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("var event = new WheelEvent('wheel', {deltaY: 1000, bubbles: true}); arguments[0].dispatchEvent(event);", scrollablePanel);
                Thread.sleep(200);
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                logger.info("Method 1 (Aggressive JS): scrollTop changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 1 (Aggressive JS): FAILED - {}", e.getMessage());
            }
            
            // Method 2: Parent element scroll
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                WebElement parentPanel = scrollablePanel.findElement(By.xpath(".."));
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", parentPanel)).doubleValue();
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", parentPanel);
                Thread.sleep(200);
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", parentPanel)).doubleValue();
                logger.info("Method 2 (Parent scroll): scrollTop changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 2 (Parent scroll): FAILED - {}", e.getMessage());
            }
            
            // Method 3: Actions PAGE_DOWN
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                actions.moveToElement(scrollablePanel).sendKeys(org.openqa.selenium.Keys.PAGE_DOWN).perform();
                Thread.sleep(200);
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                logger.info("Method 3 (Actions PAGE_DOWN): scrollTop changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 3 (Actions PAGE_DOWN): FAILED - {}", e.getMessage());
            }
            
            // Method 4: Window scroll
            try {
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return window.pageYOffset;")).doubleValue();
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
                Thread.sleep(200);
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return window.pageYOffset;")).doubleValue();
                logger.info("Method 4 (Window scroll): pageYOffset changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 4 (Window scroll): FAILED - {}", e.getMessage());
            }
            
            // Method 5: Document.body scroll
            try {
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.body.scrollTop;")).doubleValue();
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("document.body.scrollTop = document.body.scrollHeight;");
                Thread.sleep(200);
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.body.scrollTop;")).doubleValue();
                logger.info("Method 5 (Document.body): scrollTop changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 5 (Document.body): FAILED - {}", e.getMessage());
            }
            
            // Method 6: Click + PAGE_DOWN
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                actions.moveToElement(scrollablePanel).click().perform();
                Thread.sleep(100);
                for (int i = 0; i < 3; i++) {
                    actions.sendKeys(org.openqa.selenium.Keys.PAGE_DOWN).perform();
                    Thread.sleep(100);
                }
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                logger.info("Method 6 (Click + PAGE_DOWN): scrollTop changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 6 (Click + PAGE_DOWN): FAILED - {}", e.getMessage());
            }
            
            // Method 7: Simple scrollTop on custom-scroll
            try {
                WebElement scrollablePanel = findScrollablePanel(driver);
                if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
                double beforeScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                Thread.sleep(200);
                double afterScroll = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop;", scrollablePanel)).doubleValue();
                logger.info("Method 7 (Simple scrollTop): scrollTop changed from {} to {} - {}", beforeScroll, afterScroll, (afterScroll > beforeScroll ? "SUCCESS" : "FAILED"));
            } catch (Exception e) {
                logger.info("Method 7 (Simple scrollTop): FAILED - {}", e.getMessage());
            }
            
            logger.info("=== Scroll method testing complete ===");
            
        } catch (Exception e) {
            logger.error("Error during scroll method testing: {}", e.getMessage(), e);
        }
    }

    /**
     * Exports extracted messages to file by merging with existing messages.
     * @param extractedMessages Set of extracted messages to export
     */
    private void exportExtractedMessagesToFile(Set<String> extractedMessages) {
        try {
            java.nio.file.Path outputPath = java.nio.file.Paths.get(EXTRACTED_MESSAGES_FILE);
            
            // Load existing messages if file exists
            Set<String> allMessages = new LinkedHashSet<>();
            if (java.nio.file.Files.exists(outputPath)) {
                Set<String> existingMessages = java.nio.file.Files.lines(outputPath)
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .collect(Collectors.toSet());
                allMessages.addAll(existingMessages);
                logger.info("Loaded {} existing messages from file", existingMessages.size());
            }
            
            // Add new messages (LinkedHashSet maintains order and removes duplicates)
            allMessages.addAll(extractedMessages);
            
            // Write all messages to file
            java.nio.file.Files.write(outputPath, String.join("\n", allMessages).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            logger.info("Successfully exported {} total messages ({} new) to: {}", allMessages.size(), extractedMessages.size(), EXTRACTED_MESSAGES_FILE);
        } catch (Exception e) {
            logger.error("Error exporting extracted messages to file: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads existing messages from file to avoid duplicates.
     * @return Set of existing messages
     */
    private Set<String> loadExistingMessagesFromFile() {
        Set<String> existingMessages = new HashSet<>();
        
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(EXTRACTED_MESSAGES_FILE);
            if (java.nio.file.Files.exists(filePath)) {
                existingMessages = java.nio.file.Files.lines(filePath)
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .collect(Collectors.toSet());
                logger.info("Loaded {} existing messages from file: {}", existingMessages.size(), EXTRACTED_MESSAGES_FILE);
            } else {
                logger.info("No existing file found at: {}", EXTRACTED_MESSAGES_FILE);
            }
        } catch (Exception e) {
            logger.warn("Error loading existing messages from file: {}", e.getMessage());
        }
        
        return existingMessages;
    }

    /**
     * Main method to search and extract "I am looking for" messages.
     */
    public void searchAndExtractLookingForMessages(WATGCommonUtils instance) 
            throws InterruptedException, AWTException, IOException {
        logger.info("Starting search and extract for 'I am looking for' messages");
        
        try {
            // Load existing messages from file to avoid duplicates
            Set<String> existingMessages = loadExistingMessagesFromFile();
            logger.info("Loaded {} existing messages from file to avoid duplicates", existingMessages.size());
            
            // Initialize driver
            WebDriver driver = getDriver();
            
            // Navigate to the base URL
            driver.get(getBaseUrl());
            Thread.sleep(2000);
            
            logger.info("Navigated to Telegram base URL");
            logger.info("Current page title: {}", driver.getTitle());
            logger.info("Current URL: {}", driver.getCurrentUrl());
            
            // Send startup notification to indicate service is beginning
            sendStartupNotification(driver);
            
            // Wait for Telegram to load (check for login or main interface)
            if (!febxs(getxPathInterface().getSOCIAL_MEDIA_SCANNER_IDENTIFIER_AFTER_URL()).isEmpty()) {
                logger.info("Telegram loaded successfully");
            }
            
            // Click on search input using XPath from Telegram_Xpaths
            String searchXPath = getxPathInterface().getXPATH_SEARCH();
            logger.info("Using search XPath: {}", searchXPath);
            
            // Wait for search input to be present
            org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            WebElement searchInput = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(By.xpath(searchXPath)));
            searchInput.click();
            Thread.sleep(1000);
            
            // Type search query with exact phrase
            searchInput.sendKeys(SEARCH_PHRASE);
            Thread.sleep(2000);
            
            // Test scroll methods to identify which one works
            testScrollMethods(driver);
            
            // Extract messages with pagination - scroll and extract until no more results
            Set<String> extractedMessages = extractedMessagesThreadLocal.get();
            Set<String> previousResults = new HashSet<>();
            int noNewResultsCount = 0;
            int maxNoNewResults = MAX_NO_NEW_RESULTS;
            int scrollCount = 0;
            int maxScrolls = MAX_SCROLLS;
            
            while (noNewResultsCount < maxNoNewResults && scrollCount < maxScrolls) {
                scrollCount++;
                
                boolean scrollSuccess = scrollWithFallback(driver, searchInput);
                
                if (!scrollSuccess) {
                    logger.warn("All scroll methods failed, forcing exit");
                    noNewResultsCount = maxNoNewResults;
                }
                
                Thread.sleep(500);
                // Extract messages from current page
                java.util.List<WebElement> searchResults = driver.findElements(By.xpath(getxPathInterface().getXPATH_SEARCH_EXTRACT_MESSAGE_ELEMENTS()));
                logger.info("Scroll {}: Found {} search result entries to process", scrollCount, searchResults.size());
                
                // Check if we've reached the end (no more results to scroll)
                if (searchResults.isEmpty()) {
                    logger.info("No more search results found, reached end of search");
                    break;
                }
                
                // Log HTML structure of first few elements for debugging
                for (int i = 0; i < Math.min(3, searchResults.size()); i++) {
                    WebElement elem = searchResults.get(i);
                    try {
                        String tagName = elem.getTagName();
                        String className = elem.getAttribute("class");
                        String outerHtml = elem.getAttribute("outerHTML");
                        String imLookingMsg = elem.getText();
                        logger.info("Element {} - Text: {}", i, imLookingMsg);
                        logger.info("Element {} - Tag: {}, Class: '{}', HTML length: {}", i, tagName, className, outerHtml != null ? outerHtml.length() : 0);
                        logger.info("Element {} - HTML preview: {}", i, outerHtml != null ? outerHtml.substring(0, Math.min(200, outerHtml.length())) : "null");
                    } catch (Exception e) {
                        logger.error("Error logging element structure for index {}: {}", i, e.getMessage());
                    }
                }
                
                int newResultsThisPage = 0;
                logger.info("Starting to process {} search results", searchResults.size());
                for (int i = 0; i < searchResults.size(); i++) {
                    WebElement result = searchResults.get(i);
                    try {
                        String resultText = result.getText();
                        logger.info("Result text raw: '{}'", resultText);

                        if (resultText != null && !resultText.trim().isEmpty()) {
                            logger.info("Processing search result: {}", resultText);
                            
                            if (isLookingForMessage(resultText)) {
                                if (!extractedMessages.contains(resultText) && !existingMessages.contains(resultText)) {
                                    extractedMessages.add(resultText);
                                    newResultsThisPage++;
                                    //logger.info("Extracted search result: {}", resultText);
                                } else {
                                    logger.debug("Skipping duplicate message: {}", resultText);
                                }
                            } else {
                                logger.info("Preview text does not match 'looking for' criteria, skipping");
                            }
                        } else {
                            logger.info("Result text is null or empty, skipping");
                        }
                    } catch (Exception e) {
                        logger.error("Error processing search result at index {}: {}", i, e.getMessage(), e);
                    }
                }
                
                // Check if we got new results
                if (newResultsThisPage == 0) {
                    noNewResultsCount++;
                    logger.info("No new results on scroll {} (consecutive count: {})", scrollCount, noNewResultsCount);
                } else {
                    noNewResultsCount = 0; // Reset counter if we found new results
                    logger.info("Found {} new results on scroll {}", newResultsThisPage, scrollCount);
                }
            }
            
            logger.info("Pagination complete. Total scrolls: {}, Total extracted messages: {}", scrollCount, extractedMessages.size());
            
            // Filter out messages that already exist in the file - only send new messages
            Set<String> newMessages = new HashSet<>(extractedMessages);
            newMessages.removeAll(existingMessages);
            logger.info("New messages to send: {} (out of {} total extracted)", newMessages.size(), extractedMessages.size());
            
            // Send only new extracted messages to phone number
            if (!newMessages.isEmpty()) {
                sendExtractedMessagesToPhoneNumber(driver, newMessages);
            } else {
                logger.info("No new messages to send - all extracted messages already exist in file");
                // Send notification message that no new posts were found
                sendSingleMessage(driver, DEFAULT_CONTACT_NAME, NO_NEW_POSTS_MESSAGE);
            }
            
            // Clear search
            try {
                WebElement clearSearch = driver.findElement(By.xpath(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION()));
                if (clearSearch.isDisplayed()) {
                    clearSearch.click();
                    Thread.sleep(400);
                }
            } catch (Exception e) {
                logger.debug("Clear search not available or not clickable");
            }
            
            logger.info("Search and extract process completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during search and extract process", e);
            throw e;
        } finally {

            Thread.sleep(2000); // Wait before cleanup
            // Shutdown and cleanup WebDriver
            try {
                if (driver != null) {
                    logger.info("Shutting down WebDriver...");
                    driver.quit();
                    logger.info("WebDriver shutdown completed");
                }
            } catch (Exception e) {
                logger.error("Error during WebDriver shutdown: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getFilePathToExportContactsData() {
        return getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS();
    }

    @Override
    public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
        TaskSocialData taskSocialData = getTaskSocialData();
        return clearUpdateCurrDateAndReloadFile(taskSocialData.getVisitedGroupFileName(), 
                GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG, 
                taskSocialData.getVisitedGroupNames(), 
                true);
    }

    private static TaskSocialData getTaskSocialData() {
        return TASK_SOCIAL_DATA.get(ThreadLocalAutomationContext.getContext().getTaskType());
    }

    @Override
    public int getExpectedSizeToModuloToInsertExportToFile() {
        return 10;
    }

    @Override
    public FlowType getEnumFlowType() {
        return enumFlowType;
    }

    @Override
    protected int processCompletedSuccessfullyCounter() {
        return 10;
    }

    @Override
    protected int getMaxAllowedRepeatOfSameGroup() {
        return 100;
    }

    @Override
    public boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping) {
        // For search and extract, we export the extracted messages
        logger.info("exportDataToFile for search and extract: {}", fileName);
        
        try {
            // Get extracted messages from ThreadLocal
            Set<String> extractedMessages = extractedMessagesThreadLocal.get();
            
            if (extractedMessages == null || extractedMessages.isEmpty()) {
                logger.warn("No extracted messages to export");
                return true;
            }
            
            // Create output file path
            java.nio.file.Path outputPath = java.nio.file.Paths.get(EXTRACTED_MESSAGES_FILE);
            
            // Load existing messages from file if it exists
            Set<String> allMessages = new LinkedHashSet<>();
            if (java.nio.file.Files.exists(outputPath)) {
                Set<String> existingMessages = java.nio.file.Files.lines(outputPath)
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .collect(Collectors.toSet());
                allMessages.addAll(existingMessages);
                logger.info("Loaded {} existing messages from file", existingMessages.size());
            }
            
            // Add new messages to the set (LinkedHashSet maintains order and removes duplicates)
            allMessages.addAll(extractedMessages);
            
            // Write all messages (existing + new) to file
            java.nio.file.Files.write(outputPath, String.join("\n", allMessages).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            logger.info("Successfully exported {} total messages ({} new) to: {}", allMessages.size(), extractedMessages.size(), EXTRACTED_MESSAGES_FILE);
            
            // Also add to the mapping if provided
            if (setOfDataMapping != null) {
                setOfDataMapping.put(EnumStringToExport.EXTRACTED_MESSAGES, allMessages);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error exporting extracted messages to file: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(WATGCommonUtils instance, boolean hasNewGroupJoinedButNotRestored, String groupNameExceptIndv, Set<String> allPhoneContacts, int counter, int repeatCounter) throws InterruptedException {
        // For search and extract, we don't need group traversal logic
        logger.info("takeActionIfTheGroupIsEligibleToProceed called for search and extract - no action needed");
        Map<ActionIfTheGroupIsEligible, Object> result = new HashMap<>();
        result.put(ActionIfTheGroupIsEligible.FLAG_TRUE, true);
        return result;
    }

    @Override
    public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious, String lastVisitedEligibleRestoreGroup) {
        // For search and extract, we don't need group restoration logic
        logger.info("searchAndClickOnLastVisitedGroup called for search and extract - no action needed");
        return false;
    }

    @Override
    public boolean isSuccessfulSupportGroupContainsCheckRequired() {
        // For search and extract, we don't need support group filtering
        return false;
    }
}
