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
 * Service for searching and extracting "I am looking for" messages from WhatsApp.
 * This service extends WATGParent to leverage the existing traversal infrastructure.
 */
public class WA_SearchAndExtractLookingFor extends WATGParent {

    private static final Logger logger = LogManager.getLogger(WA_SearchAndExtractLookingFor.class);
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private final int instanceId = instanceCounter.incrementAndGet();

    private static final FlowType enumFlowType = FlowType.SEARCH_AND_EXTRACT_LOOKING_FOR;

    // Search-related XPaths (these should be moved to WhatsApp_Xpaths interface if needed)
    private static final String SEARCH_INPUT_XPATH = "//*[@aria-label='Search input textbox']";
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
            }
        });
    }

    /**
     * Sends extracted messages to a specific phone number.
     * @param driver WebDriver instance
     * @param extractedMessages Set of extracted messages
     */
    private void sendExtractedMessagesToPhoneNumber(WebDriver driver, Set<String> extractedMessages) {
        String phoneNumber = "+918274848227";
        logger.info("Sending {} extracted messages to phone number: {}", extractedMessages.size(), phoneNumber);
        
        try {
            // Click on search button
            driver.findElement(By.xpath(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH())).click();
            Thread.sleep(1000);
            
            // Find search input and type phone number
            WebElement searchInput = driver.findElement(By.xpath(getxPathInterface().getXPATH_SEARCH()));
            searchInput.sendKeys(phoneNumber + "\n");
            Thread.sleep(2000);
            
            // Select the contact
            searchInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN, org.openqa.selenium.Keys.ENTER);
            Thread.sleep(2000);
            
            // Find message input
            WebElement messageInput = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
            
            // Build message with all extracted messages
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Extracted 'I am looking for' messages:\n\n");
            int count = 1;
            for (String msg : extractedMessages) {
                messageBuilder.append(count++).append(". ").append(msg).append("\n\n");
            }
            messageBuilder.append("Total messages: ").append(extractedMessages.size());
            
            // Type and send message
            messageInput.sendKeys(messageBuilder.toString());
            Thread.sleep(1000);
            
            // Click send button
            driver.findElement(By.xpath(getxPathInterface().getSPAN_DATA_TESTID_SEND())).click();
            Thread.sleep(2000);
            
            logger.info("Successfully sent message to {}", phoneNumber);
            
        } catch (Exception e) {
            logger.error("Error sending message to phone number {}: {}", phoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Loads existing messages from file to avoid duplicates.
     * @return Set of existing messages
     */
    private Set<String> loadExistingMessagesFromFile() {
        Set<String> existingMessages = new HashSet<>();
        String outputFileName = "dbfiles/whatsapp_extracted_messages.txt";
        
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(outputFileName);
            if (java.nio.file.Files.exists(filePath)) {
                existingMessages = java.nio.file.Files.lines(filePath)
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .collect(Collectors.toSet());
                logger.info("Loaded {} existing messages from file: {}", existingMessages.size(), outputFileName);
            } else {
                logger.info("No existing file found at: {}", outputFileName);
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
            
            // Navigate to the base URL with retry logic
            int maxNavRetries = 3;
            int navRetryCount = 0;
            boolean navigationSuccess = false;
            
            while (navRetryCount < maxNavRetries && !navigationSuccess) {
                try {
                    logger.info("Navigation attempt {} to {}", navRetryCount + 1, getBaseUrl());
                    driver.get(getBaseUrl());
                    Thread.sleep(5000);
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
            
            // Click on search input using XPath from WhatsApp_Xpaths
            String searchXPath = getxPathInterface().getXPATH_SEARCH();
            logger.info("Using search XPath: {}", searchXPath);
            
            WebElement searchInput = driver.findElement(By.xpath(searchXPath));
            searchInput.click();
            Thread.sleep(1000);
            
            // Type search query with exact phrase
            searchInput.sendKeys("\"I am looking for\"");
            Thread.sleep(2000);
            
            // Extract messages with pagination - scroll and extract until no more results
            Set<String> extractedMessages = extractedMessagesThreadLocal.get();
            Set<String> previousResults = new HashSet<>();
            int noNewResultsCount = 0;
            int maxNoNewResults = 3; // Stop after 3 consecutive scrolls with no new results
            int scrollCount = 0;
            int maxScrolls = 50; // Safety limit to prevent infinite scrolling
            
            while (noNewResultsCount < maxNoNewResults && scrollCount < maxScrolls) {
                scrollCount++;
                
                // Extract messages from current page
                java.util.List<WebElement> searchResults = driver.findElements(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS()));
                logger.info("Scroll {}: Found {} search result entries to process", scrollCount, searchResults.size());
                
                int newResultsThisPage = 0;
                for (WebElement result : searchResults) {
                    try {
                        String resultText = result.getText();
                        if (resultText != null && !resultText.trim().isEmpty()) {
                            logger.info("Processing search result: {}", resultText);
                            // Match exact phrase "I am looking for" (case-insensitive)
                            if (resultText.toLowerCase().contains("i am looking for")) {
                                // Check if message already exists in file or current extraction
                                if (!extractedMessages.contains(resultText) && !existingMessages.contains(resultText)) {
                                    extractedMessages.add(resultText);
                                    newResultsThisPage++;
                                    logger.info("Extracted search result: {}", resultText);
                                } else {
                                    logger.debug("Skipping duplicate message: {}", resultText);
                                }
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
                
                // Scroll down to load more results
                try {
                    WebElement scrollablePanel = driver.findElement(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN()));
                    scrollablePanel.sendKeys(org.openqa.selenium.Keys.PAGE_DOWN);
                    Thread.sleep(1500); // Wait for content to load
                } catch (Exception e) {
                    logger.debug("Error scrolling panel: {}", e.getMessage());
                    // Try alternative scroll method
                    try {
                        searchInput.sendKeys(org.openqa.selenium.Keys.PAGE_DOWN);
                        Thread.sleep(1500);
                    } catch (Exception e2) {
                        logger.debug("Alternative scroll also failed: {}", e2.getMessage());
                        noNewResultsCount = maxNoNewResults; // Force exit
                    }
                }
            }
            
            logger.info("Pagination complete. Total scrolls: {}, Total extracted messages: {}", scrollCount, extractedMessages.size());
            
            // Send extracted messages to phone number
            if (!extractedMessages.isEmpty()) {
                sendExtractedMessagesToPhoneNumber(driver, extractedMessages);
            }
            
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
            String outputFileName = "dbfiles/whatsapp_extracted_messages.txt";
            java.nio.file.Path outputPath = java.nio.file.Paths.get(outputFileName);
            
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
            
            logger.info("Successfully exported {} total messages ({} new) to: {}", allMessages.size(), extractedMessages.size(), outputFileName);
            
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
