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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for searching and extracting "I am looking for" messages from WhatsApp.
 * This service extends WATGParent to leverage the existing traversal infrastructure.
 */
@Component
public class WA_SearchAndExtractLookingFor extends WATGParent {

    private static final Logger logger = LogManager.getLogger(WA_SearchAndExtractLookingFor.class);
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private final int instanceId = instanceCounter.incrementAndGet();

    private static final FlowType enumFlowType = FlowType.SEARCH_AND_EXTRACT_LOOKING_FOR;

    // Constants for hardcoded values
    private static final String DEFAULT_CONTACT_NUMBER = "+918274848227";
    private static final String EXTRACTED_MESSAGES_FILE = "dbfiles/whatsapp_extracted_messages.txt";
    private static final String SEARCH_PHRASE = "\"I am looking for\"";
    private static final String NO_NEW_POSTS_MESSAGE = "WhatsApp: No New Post with 'Looking for Support' Found";

    // Sleep time constants (all under 2 seconds)
    private static final int SEARCH_WAIT_MS = 2000;
    private static final int CONTACT_SELECT_WAIT_MS = 2000;
    private static final int MESSAGE_SEND_WAIT_MS = 1000;
    private static final int SCROLL_WAIT_MS = 1500;
    private static final int NAVIGATION_WAIT_MS = 2000;

    // Scroll configuration from application.properties
    @Value("${whatsapp.search.looking.for.max.scrolls:50}")
    private int maxScrolls;

    @Value("${whatsapp.search.looking.for.max.no.new.results:20}")
    private int maxNoNewResults;

    // Search-related XPaths (these should be moved to WhatsApp_Xpaths interface if needed)
    private static final String SEARCH_INPUT_XPATH = "//*[@aria-label='Search or start a new chat']";
    private static final String SEARCH_FILTER_MESSAGES = "//*[contains(text(), 'Messages')]";
    private static final String SEARCH_MESSAGE_ENTRY = "//*[contains(@class, 'search-section')]/descendant::div[contains(@class, 'chat-item')]";
    private static final String SEARCH_MESSAGE_TEXT = "//*[contains(text(), 'looking for') or contains(text(), 'Looking for')]";

    private final ThreadLocal<Set<String>> extractedMessagesThreadLocal = ThreadLocal.withInitial(LinkedHashSet::new);

    @Autowired
    public WA_SearchAndExtractLookingFor(InitialSetup initialSetup,
                                         ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup, parallelWebDriverManager);
        logger.info("WA_SearchAndExtractLookingFor instance with instanceId :{} initialized", instanceId);
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
                logger.error("Error during WA_SearchAndExtractLookingFor execution", e);
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
        });
    }

    /**
     * Clears search input and searches for a contact.
     * Ensures the search input is completely cleared before typing.
     * @param driver WebDriver instance
     * @param contactNumber Contact number to search for
     * @return WebElement search input
     */
    private WebElement clearAndSearchInput(WebDriver driver, String contactNumber) throws InterruptedException {

        
        List<WebElement> deleteInputs = driver.findElements(By.xpath("//button[@aria-label='End icon button']"));
        if(!deleteInputs.isEmpty()) {
            logger.info("Clearing search input by end button");
            WebElement clearInput = deleteInputs.getFirst();
            try {
                clearInput.click();
                Thread.sleep(200);
                logger.info("Successfully cleared search input by end button");
            }catch (Exception e) {
                logger.error("Error while clicking end button, trying to clear by other means", e);
                List<WebElement> textBox = driver.findElements(By.xpath( "//div[@data-testid='chat-list-search-container']/descendant::input[@role='textbox' and @dir='ltr']"));
               if(!textBox.isEmpty()) {
                   clearInput = textBox.getFirst();
                   clearInput.clear();
                   Thread.sleep(200);
                   clearInput.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
                   Thread.sleep(100);
                   clearInput.sendKeys(Keys.DELETE);
                   Thread.sleep(200);
                   clearInput.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
                   Thread.sleep(100);
                   clearInput.sendKeys(Keys.BACK_SPACE);
               } else {
                   logger.warn("Could not find alternative search input to clear");
               }

            }
        } else {
            logger.info("No search input found, clicking magnifying glass");
            driver.findElement(By.xpath(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH())).click();
            Thread.sleep(SEARCH_WAIT_MS);
            Thread.sleep(300);
        }
        WebElement clearInput = driver.findElement(By.xpath(getxPathInterface().getXPATH_SEARCH()));

        clearInput.sendKeys(contactNumber);
        Thread.sleep(SEARCH_WAIT_MS);
        
        return clearInput;
    }

    /**
     * Selects a contact from search results using keyboard navigation.
     * @param driver WebDriver instance
     * @param searchInput Search input element
     * @param contactNumber Contact number to select
     * @return true if selection successful, false otherwise
     */
    private boolean selectContact(WebDriver driver, WebElement searchInput, String contactNumber) throws InterruptedException {
        try {
            searchInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN, org.openqa.selenium.Keys.ENTER);
            Thread.sleep(CONTACT_SELECT_WAIT_MS);
            logger.info("Selected contact: {}", contactNumber);
            return true;
        } catch (Exception e) {
            logger.error("Failed to select contact {}: {}", contactNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Clicks the send button to send a message.
     * @param driver WebDriver instance
     * @return true if send successful, false otherwise
     */
    private boolean clickSendButton(WebDriver driver) throws InterruptedException {
        try {
            driver.findElement(By.xpath(getxPathInterface().getSPAN_DATA_TESTID_SEND())).click();
            Thread.sleep(MESSAGE_SEND_WAIT_MS);
            logger.info("Send button clicked successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to click send button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a message matches the "looking for" criteria.
     * @param text Message text to check
     * @return true if message matches criteria, false otherwise
     */
    private boolean isLookingForMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        return (lowerText.contains("i am looking for") || lowerText.contains("we are looking for"))
                && (lowerText.contains("support") || lowerText.contains("interview") || lowerText.contains("proxy"));
    }

    /**
     * Helper method to find the scrollable panel with fallback XPath for WhatsApp.
     * @param driver WebDriver instance
     * @return WebElement representing the scrollable panel, or null if not found
     */
    private WebElement findScrollablePanel(WebDriver driver) {
        try {
            return driver.findElement(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN()));
        } catch (Exception e) {
            try {
                return driver.findElement(By.xpath("//*[contains(@class, 'custom-scroll')]"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * Attempts to scroll using multiple fallback methods (from TG implementation).
     * @param driver WebDriver instance
     * @param searchInput Search input WebElement (for fallback)
     * @return true if any scroll method succeeded
     */
    private boolean scrollWithFallback(WebDriver driver, WebElement searchInput) throws InterruptedException {
        boolean scrollSuccess = false;
        
        // Method 1: Aggressive JavaScript on custom-scroll panel
        try {
            WebElement scrollablePanel = findScrollablePanel(driver);
            if (scrollablePanel == null) throw new Exception("Scrollable panel not found");
            for (int i = 0; i < 5; i++) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollablePanel);
                Thread.sleep(50);
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollBy(0, 2000);", scrollablePanel);
                Thread.sleep(50);
            }
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("var event = new WheelEvent('wheel', {deltaY: 1000, bubbles: true}); arguments[0].dispatchEvent(event);", scrollablePanel);
            Thread.sleep(100);
            scrollSuccess = true;
            logger.debug("Scrolled using aggressive JavaScript on custom-scroll panel");
        } catch (Exception e) {
            logger.debug("Error scrolling custom-scroll panel with JavaScript: {}", e.getMessage());
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
                logger.debug("Scrolled using JavaScript on parent of custom-scroll");
            } catch (Exception e) {
                logger.debug("Error scrolling parent of custom-scroll: {}", e.getMessage());
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
                logger.debug("Scrolled using Actions PAGE_DOWN on custom-scroll");
            } catch (Exception e) {
                logger.debug("Error scrolling custom-scroll with Actions: {}", e.getMessage());
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
                WebElement scrollablePanel = driver.findElement(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS()));
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
     * @param driver WebDriver instance
     */
    private void sendStartupNotification(WebDriver driver) {
        String contactNumber = DEFAULT_CONTACT_NUMBER;
        String startupMessage = String.format(
            "WhatsApp Search & Extract Service - Initialization Started for Phrase: 'I am looking for' on 'support', 'interview', or 'proxy' with Timestamp: %s",
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        logger.info("Sending startup notification to contact: {}", contactNumber);
        
        try {
            WebElement searchInput = clearAndSearchInput(driver, contactNumber);
            
            if (!selectContact(driver, searchInput, contactNumber)) {
                logger.warn("Could not select contact for startup notification: {}", contactNumber);
                return;
            }
            
            WebElement messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
            messageInput.clear();
            messageInput.sendKeys(startupMessage);
            Thread.sleep(MESSAGE_SEND_WAIT_MS);
            
            boolean sendSuccess = clickSendButton(driver);
            
            if (sendSuccess) {
                logger.info("Successfully sent startup notification to {}", contactNumber);
            } else {
                logger.warn("Failed to send startup notification to {}", contactNumber);
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
        String phoneNumber = DEFAULT_CONTACT_NUMBER;
        logger.info("Sending {} extracted messages to phone number: {}", extractedMessages.size(), phoneNumber);
        
        try {
            WebElement searchInput = clearAndSearchInput(driver, phoneNumber);
            
            if (!selectContact(driver, searchInput, phoneNumber)) {
                logger.error("Could not select contact: {}", phoneNumber);
                return;
            }
            
            WebElement messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
            
            // Build message with all extracted messages
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Extracted 'I am looking for' messages:\n\n");
            int count = 1;
            for (String msg : extractedMessages) {
                messageBuilder.append(count++).append(". ").append(msg).append("\n\n");
            }
            messageBuilder.append("Total messages: ").append(extractedMessages.size());
            
            messageInput.sendKeys(messageBuilder.toString());
            Thread.sleep(MESSAGE_SEND_WAIT_MS);
            
            boolean sendSuccess = clickSendButton(driver);
            
            if (sendSuccess) {
                logger.info("Successfully sent message to {}", phoneNumber);
            } else {
                logger.error("Failed to send message to {}", phoneNumber);
            }
            
        } catch (Exception e) {
            logger.error("Error sending message to phone number {}: {}", phoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Sends a single message to a specific contact.
     * @param driver WebDriver instance
     * @param contactNumber Contact number to send to
     * @param message Message to send
     */
    private void sendSingleMessage(WebDriver driver, String contactNumber, String message) {
        logger.info("Sending single message to contact: {}", contactNumber);
        
        try {
            WebElement searchInput = clearAndSearchInput(driver, contactNumber);
            
            if (!selectContact(driver, searchInput, contactNumber)) {
                logger.error("Could not select contact: {}", contactNumber);
                return;
            }
            
            WebElement messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
            messageInput.clear();
            messageInput.sendKeys(message);
            Thread.sleep(MESSAGE_SEND_WAIT_MS);
            
            boolean sendSuccess = clickSendButton(driver);
            
            if (sendSuccess) {
                logger.info("Successfully sent single message to {}", contactNumber);
            } else {
                logger.error("Failed to send single message to {}", contactNumber);
            }
            
        } catch (Exception e) {
            logger.error("Error sending single message to {}: {}", contactNumber, e.getMessage(), e);
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
     * Exports extracted messages to file.
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
            
            // Navigate to the base URL with retry logic
            int maxNavRetries = 3;
            int navRetryCount = 0;
            boolean navigationSuccess = false;
            
            while (navRetryCount < maxNavRetries && !navigationSuccess) {
                try {
                    logger.info("Navigation attempt {} to {}", navRetryCount + 1, getBaseUrl());
                    driver.get(getBaseUrl());
                    Thread.sleep(NAVIGATION_WAIT_MS);
                    navigationSuccess = true;
                    logger.info("Successfully navigated to WhatsApp base URL");
                } catch (org.openqa.selenium.WebDriverException e) {
                    navRetryCount++;
                    logger.warn("Navigation attempt {} failed: {}", navRetryCount, e.getMessage());
                    if (navRetryCount < maxNavRetries) {
                        logger.info("Retrying navigation in 2 seconds...");
                        Thread.sleep(2000);
                    } else {
                        logger.error("Failed to navigate after {} attempts", maxNavRetries);
                        throw e;
                    }
                }
            }
            
            logger.info("Current page title: {}", driver.getTitle());
            logger.info("Current URL: {}", driver.getCurrentUrl());
            
            // Wait for WhatsApp to load (check for scanner or main interface)
            if (!febxs(getxPathInterface().getSOCIAL_MEDIA_SCANNER_IDENTIFIER_AFTER_URL()).isEmpty()) {
                logger.info("WhatsApp scanner detected, waiting for QR code scan...");
                Thread.sleep(30000); // Wait for user to scan QR code
            }
            
            Thread.sleep(2000); // Wait for page to fully load after scanner
            
            // Send startup notification to indicate service is beginning
            sendStartupNotification(driver);
            
            // Click on search input using XPath from WhatsApp_Xpaths
            String searchXPath = getxPathInterface().getXPATH_SEARCH();
            logger.info("Using search XPath: {}", searchXPath);
            
            WebElement searchInput = driver.findElement(By.xpath(searchXPath));
            searchInput.click();
            Thread.sleep(1000);
            
            // Type search query with exact phrase
            searchInput.sendKeys(SEARCH_PHRASE);
            Thread.sleep(SEARCH_WAIT_MS);
            
            // Extract messages with pagination - scroll and extract until no more results
            Set<String> extractedMessages = extractedMessagesThreadLocal.get();
            Set<String> previousResults = new HashSet<>();
            int noNewResultsCount = 0;
            int scrollCount = 0;
            
            while (noNewResultsCount < maxNoNewResults && scrollCount < maxScrolls) {
                scrollCount++;
                
                boolean scrollSuccess = scrollWithFallback(driver, searchInput);
                
                if (!scrollSuccess) {
                    logger.warn("All scroll methods failed, forcing exit");
                    noNewResultsCount = maxNoNewResults;
                }
                
                Thread.sleep(500);
                
                // Extract messages from current page
                java.util.List<WebElement> searchResults = driver.findElements(By.xpath(getxPathInterface().getXPATH_SEARCH_EXTRACT_MESSAGE_ELEMENTS_LOOKING_FOR()));
                logger.info("Scroll {}: Found {} search result entries to process", scrollCount, searchResults.size());
                
                int newResultsThisPage = 0;
                for (WebElement result : searchResults) {
                    try {
                        String resultText = result.getText();
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
                        }
                    } catch (Exception e) {
                        logger.debug("Error processing search result: {}", e.getMessage());
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
                sendSingleMessage(driver, DEFAULT_CONTACT_NUMBER, NO_NEW_POSTS_MESSAGE);
            }

            // Export extracted messages to file (always export to ensure file is created/updated)
            exportExtractedMessagesToFile(extractedMessages);
            
            // Clear search
            try {
                WebElement clearSearch = driver.findElement(By.xpath(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION()));
                if (clearSearch.isDisplayed()) {
                    clearSearch.click();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                logger.debug("Clear search not available or not clickable");
            }
            
        } catch (Exception e) {
            logger.error("Error during search and extract process", e);
            throw e;
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
