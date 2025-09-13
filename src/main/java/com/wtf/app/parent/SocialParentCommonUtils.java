package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterface;
import com.wtf.app.util.RetryUtils;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component(value = "socialParentCommonUtils")
public abstract class SocialParentCommonUtils extends Constants {
    private static final Logger logger = LogManager.getLogger(SocialParentCommonUtils.class);
    private static final int MAX_RETRIES = 3;
    private static final int PAGE_LOAD_TIMEOUT_SEC = 120;
    private static final int IMPLICIT_WAIT_SEC = 1;
    private static final int EXPLICIT_WAIT_SEC = 2;
    public final InitialSetup initialSetup;

    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private final int instanceId = instanceCounter.incrementAndGet();

    @Value("${browser.profile.directory:C:/temp/chrome-profiles}")
    private String profileBaseDir;

    protected WebDriverWait driverWait;
    protected final ParallelWebDriverManager parallelWebDriverManager;
    protected XPathInterface xPathInterface;
    protected String baseUrl;
    protected WebDriver driver;
    protected boolean isDriverInitialized = false;

    protected boolean traverseDependsOnAllSuccessGroupList;

    protected SocialModel socialModel;
    protected SocialType socialType;
    protected TaskType taskType;
    protected final Map<TaskType,SocialParentCommonUtils> taskTypeInstanceMap = new ConcurrentHashMap<>();
    //protected AutomationContext automationContext;

    @Autowired
    public SocialParentCommonUtils(
                                 InitialSetup initialSetup,
                                 ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup);

        this.initialSetup = initialSetup;
        this.parallelWebDriverManager = parallelWebDriverManager;
        logger.info("SocialParentCommonUtils instance with instanceId :{} initialized", instanceId);
        // Initialize request scoped fields without accessing WebDriver
        logger.info("SocialParentCommonUtils initialized with  InitialSetup and ParallelWebDriverManager");
    }

   /* public void setAutomationContext(AutomationContext automationContext) {
        this.automationContext = automationContext;
    }
*/
    /**
     * Get the WebDriver instance, initializing it if necessary.
     * This ensures we don't try to access the request-scoped WebDriver during bean initialization.
     */
    protected WebDriver getDriver() {
        if (!isDriverInitialized) {
            initializeDriver();
        }
        return driver;
    }

    /**
     * Initialize the WebDriver and related components.
     * This should only be called when actually needed, not during bean initialization.
     */
    protected void initializeDriver() {
        if (!isDriverInitialized) {
            AutomationContext automationContext = ThreadLocalAutomationContext.getContext();
            if ( automationContext == null) {
                throw new IllegalStateException("AutomationContext must be set before initializing the driver.");
            }
            try {
                logger.info("Initializing WebDriver from SocialParentCommonUtils for profile: {}", automationContext.getTaskType());
                this.driver = parallelWebDriverManager.getDriver(automationContext.getTaskType());
                // Configure timeouts
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SEC));
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SEC));
                this.driverWait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SEC));
                
                // Additional WebDriver configurations
                try {
                    driver.manage().window().maximize();
                } catch (Exception e) {
                    logger.warn("Could not maximize window: {}", e.getMessage());
                }
                
                isDriverInitialized = true;

                logger.info("WebDriver initialized with timeouts - PageLoad: {}s, Implicit: {}s, Explicit: {}s", 
                          PAGE_LOAD_TIMEOUT_SEC, IMPLICIT_WAIT_SEC, EXPLICIT_WAIT_SEC);
                
            } catch (Exception e) {
                logger.error("Failed to initialize WebDriver: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize WebDriver", e);
            }
        }
    }

    protected Logger getLogger() {
        return logger;
    }


    protected void scrollAndSleepRandomly(long delay1, long delay2) throws InterruptedException {
        logger.debug("Sleeping for a random duration between {} and {}", delay1, delay2);
        Thread.sleep((long) (Math.random() * 3000 + delay1));
        scrollUpAndDown();
        randomDelay(3000, delay2);
    }

    protected String getRandomGreeting() {
        List<String> originalList = List.of("Hi", "Hey", "Hello", "Good day", "Hey there", "Hi there");
        // 1. Get a single random string
        int size = originalList.size();
        return originalList.get(getRandomNumber(0, size - 1));
    }

    protected String getRandomFinishing() {
        List<String> originalList = List.of("Happy Success", "Good Luck", "All the Best!", "Thank you", "Best Of Luck!", "All the Very Best", "Wish You Good Fortune");
        // 1. Get a single random string
        int size = originalList.size();
        return originalList.get(getRandomNumber(0, size - 1));
    }

    protected String getConclusionWarningAtFinishing() {
        String randomWarning = "Beware of fakes or inexperienced proxies, I can provide my profile details and otter recordings if required and ready to answer any technical questions as well.";
        List<String> originalList = List.of("Warning: " + randomWarning,"Note: "+randomWarning);
        // 1. Get a single random string
        int size = originalList.size();
        return originalList.get(getRandomNumber(0, size - 1));
    }

    protected String appendCurrentDateTimeToMessage() {
        return " _NL_ "+" Millis: "+ LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }

    protected int getRandomNumber(int lowerBoundInt, int upperBoundInt) {
        return lowerBoundInt + (int) (Math.random() * (upperBoundInt - lowerBoundInt + 1));
    }

    protected static void randomDelay(long minMillis, long maxMillis) {
        Random random = new Random();
        try {
            long bound = maxMillis - minMillis + 1;
            bound = bound< 0? -bound :bound;// bound always must be positive
            Thread.sleep(random.nextInt((int) bound) + minMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected static void gradualTypeMessage(WebElement element, String message) {
        for (char c : message.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            randomDelay(40, 100); // Small delay per character
        }
    }

    protected void gradualTypeMessageCustom(WebElement element, String message) {
        for (String word : message.split(" ")) {
            if(word.equals("_NL_")) {//replicate custom new line char if already not working
                Actions actions = new Actions(getDriver());
                //actions.sendKeys(Keys.SHIFT, Keys.ENTER).perform();//add new line for fb, wa, tg
                actions.keyDown(Keys.SHIFT).sendKeys(/*element,*/ Keys.ENTER).keyUp(Keys.SHIFT).build().perform();
                actions.release();
                randomDelay(20,200);
                continue;
            }

            for (char c : word.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                randomDelay(40, 100); // Small delay per character
            }
            element.sendKeys(" ");
            randomDelay(150, 200); // pause per word
        }
        addRandomNumAndDelete();
    }

    protected void gradualTypeMessageOnlyLastLine(WebElement element, String message) {
        String[] sentenceByNewLine = message.split("_NL_");
        int sentenceCount = sentenceByNewLine.length;
        int counter=0;

        for (String sentence : sentenceByNewLine) {
            if(counter < sentenceCount-1) {
                element.sendKeys(sentence);
                Actions actions = new Actions(getDriver());
                //actions.sendKeys(Keys.SHIFT, Keys.ENTER).perform();//add new line for fb, wa, tg
                actions.keyDown(Keys.SHIFT).sendKeys(/*element,*/ Keys.ENTER).keyUp(Keys.SHIFT).build().perform();
                actions.release();
                randomDelay(20, 200);
                counter++;
                continue;
            }

            logger.error("Executing last sentence char by char : " + sentence);
            for (char c : sentence.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                randomDelay(50, 300); // Small delay per character
            }
            addRandomNumAndDelete();
        }
    }

    private void addRandomNumAndDelete() {
        Actions actions = new Actions(getDriver());
        actions.sendKeys(""+((int) (Math.random() * 10))).build().perform();
        randomDelay(50, 300);
        actions.sendKeys(Keys.BACK_SPACE).build().perform();//Mimic human typing to add a 0-9 and delete it
        randomDelay(50, 300);
        actions.release();
    }

    public boolean writeMessageAfterPretendHumanTyping(String message, String groupId, WebElement postBox) throws InterruptedException {
        try {
            scrollAndSleepRandomly(2000, 3500);

            String greet = getRandomGreeting();
            message = greet + ", " + message;

            gradualTypeMessageCustom(postBox, message);
            Actions actions = new Actions(driver);
            actions.sendKeys(Keys.ARROW_DOWN, Keys.ENTER).perform();
            randomDelay(100, 200);
            actions.sendKeys(Keys.BACK_SPACE).perform();
            logger.info(FacebookCommonUtils.MSG_POST_STAGES_LOG._1_MSG_READY_TO_POST + " : " + "Message is ready to be posted in the group id :" + groupId);
            scrollAndSleepRandomly(3000, 5000);
        }catch (Exception e){
            logger.error("Exception in pretending human",e);
            postBox.sendKeys(message);
            scrollAndSleepRandomly(3000, 5000);
            return false;
        }

        return true;
    }


    protected static void gradualTypeMessageByWords(WebElement element, String message) {
        for (String s : message.split(" ")) {
            element.sendKeys(s+" ");//adding the space back
            randomDelay(20, 50); // Small delay per character
        }
    }

    private void scrollUpAndDown() {
        Random random = new Random();
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("window.scrollBy(0, " + random.nextInt(190) + ")");
        randomDelay(500, 1500);
        js.executeScript("window.scrollBy(0, -" + random.nextInt(170) + ")");
        randomDelay(300, 1000);
    }

    protected String waitIdentifyAndClickOnThePinnedElement(String waPinnedSpanTitle) throws InterruptedException {

        try {
            logger.info("waitIdentifyAndClickOnThePinnedElement::: {}", waPinnedSpanTitle);
            WebElement element = waitTillClickable(waPinnedSpanTitle);
            // interact with your element
            element.click();
            Thread.sleep(1000);
            return element.getText();

        } catch (Exception ex) {
            logger.error("Exception occurred in waitIdentifyAndClickOnThePinnedElement {}", ex.getLocalizedMessage());
            WebElement findElement = febx(waPinnedSpanTitle);
            findElement.click();
            Thread.sleep(1000);
            return findElement.getText();
        }
    }

    protected WebElement febx(String xPath) {
        logger.info("febx XPath :"+xPath);
        return getDriver().findElement(By.xpath(xPath));
        //return waitTillVisible(xPath); reliable for self healing but very slow
    }

    protected List<WebElement> febxs(String xPath) {
        logger.info("febxs XPath :"+xPath);
        return getDriver().findElements(By.xpath(xPath));
    }

    protected WebElement febxRetry(String xPath) {
        logger.info("febx XPath default retry :"+xPath);
        try {
            return RetryUtils.withRetry(()->getDriver().findElement(By.xpath(xPath)),"find febx element with xpath default retry: "+xPath, 3, 1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return waitTillVisible(xPath); reliable for self healing but very slow
    }

    protected List<WebElement> febxsRetry(String xPath) {
        logger.info("febxs XPath default retry :"+xPath);
        try {
            return RetryUtils.withRetry(()->getDriver().findElements(By.xpath(xPath)),"find febxs element with xpath default retry: "+xPath, 3, 1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected WebElement febxWithRetries(String xPath, int retryCount, int retryDelay) {
        logger.info("febx XPath specific retry :"+xPath);
        try {
            return RetryUtils.withRetry(()->getDriver().findElement(By.xpath(xPath)),"find febx element with xpath specific retry : "+xPath, retryCount, retryDelay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return waitTillVisible(xPath); reliable for self healing but very slow
    }

    protected List<WebElement> febxsWithRetries(String xPath, int retryCount, int retryDelay) {
        logger.info("febxs XPath specific retry :"+xPath);
        try {
            return RetryUtils.withRetry(()->getDriver().findElements(By.xpath(xPath)),"find febxs element with xpath specific retry : "+xPath, retryCount, retryDelay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds elements by XPath and clicks on them with retry logic
     */
    protected boolean febxsAndClick(String xPath, String xPathAlternate, boolean clickOnAllIfMultiplesFound, String callerBasicInformation) {
        final int MAX_RETRIES = 1;
        int retryCount = 0;
        String currentXPath = xPath;

        while (retryCount < MAX_RETRIES) {
            try {
                ensureWindowFocused();
                logger.info("febxsAndClick attempt {} - xPath: {}, alternate: {}, caller: {}",
                        retryCount + 1, currentXPath, xPathAlternate, callerBasicInformation);

                // Try to find elements
                List<WebElement> webElements = febxs(currentXPath);

                // If no elements found and alternate path provided, switch to alternate
                if (webElements.isEmpty() && xPathAlternate != null && !xPathAlternate.isEmpty() && retryCount == 0) {
                    currentXPath = xPathAlternate;
                    logger.info("Switching to alternate xPath: " + currentXPath);
                    continue;
                }

                if (!webElements.isEmpty()) {
                    if (clickOnAllIfMultiplesFound) {
                        webElements.forEach(webElement -> {
                            try {
                                webElement.click();
                                Thread.sleep(300); // Small delay between clicks
                            } catch (Exception e) {
                                logger.warn("Failed to click element: " + e.getMessage());
                            }
                        });
                        logger.info("Clicked {} elements for xPath: {}", webElements.size(), currentXPath);
                        return true;
                    } else {
                        webElements.get(0).click();
                        logger.info("Clicked first element for xPath: {}", currentXPath);
                        return true;
                    }
                }

            } catch (ElementClickInterceptedException ex) {
                logger.warn("Element click intercepted, retrying...");
                actionsKeyPerform(Keys.ESCAPE);
            } catch (WebDriverException e) {
                logger.warn("WebDriverException occurred: " + e.getMessage());
                ensureWindowFocused();
            } catch (Exception ex) {
                logger.error("Unexpected error in febxsAndClick", ex);
            }

            retryCount++;
            if (retryCount < MAX_RETRIES) {
                try {
                    Thread.sleep(1000 * retryCount); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        logger.error("Failed to complete febxsAndClick after {} attempts for xPath: {}", MAX_RETRIES, currentXPath);
        return false;
    }


    /**
     * Finds elements by XPath and clicks on them with retry logic
     */
    protected boolean febxsAndClickWithRetry(String xPath, String xPathAlternate, boolean clickOnAllIfMultiplesFound, String callerBasicInformation) {
        final int MAX_RETRIES = 3;
        int retryCount = 0;
        String currentXPath = xPath;

        while (retryCount < MAX_RETRIES) {
            try {
                ensureWindowFocused();
                logger.info("febxsAndClick attempt {} - xPath: {}, alternate: {}, caller: {}", 
                    retryCount + 1, currentXPath, xPathAlternate, callerBasicInformation);

                // Try to find elements
                List<WebElement> webElements = febxs(currentXPath);
                
                // If no elements found and alternate path provided, switch to alternate
                if (webElements.isEmpty() && xPathAlternate != null && !xPathAlternate.isEmpty() && retryCount == 0) {
                    currentXPath = xPathAlternate;
                    logger.info("Switching to alternate xPath: " + currentXPath);
                    continue;
                }

                if (!webElements.isEmpty()) {
                    if (clickOnAllIfMultiplesFound) {
                        webElements.forEach(webElement -> {
                            try {
                                webElement.click();
                                Thread.sleep(300); // Small delay between clicks
                            } catch (Exception e) {
                                logger.warn("Failed to click element: " + e.getMessage());
                            }
                        });
                        logger.info("Clicked {} elements for xPath: {}", webElements.size(), currentXPath);
                        return true;
                    } else {
                        webElements.get(0).click();
                        logger.info("Clicked first element for xPath: {}", currentXPath);
                        return true;
                    }
                }

            } catch (ElementClickInterceptedException ex) {
                logger.warn("Element click intercepted, retrying...");
                actionsKeyPerform(Keys.ESCAPE);
            } catch (WebDriverException e) {
                logger.warn("WebDriverException occurred: " + e.getMessage());
                ensureWindowFocused();
            } catch (Exception ex) {
                logger.error("Unexpected error in febxsAndClick", ex);
            }

            retryCount++;
            if (retryCount < MAX_RETRIES) {
                try {
                    Thread.sleep(1000 * retryCount); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        logger.error("Failed to complete febxsAndClick after {} attempts for xPath: {}", MAX_RETRIES, currentXPath);
        return false;
    }


    /* Use this as much as possible */
    /**
     * Ensures the browser window has focus before performing actions
     */
    protected void ensureWindowFocused() {
        try {
            String currentWindow = getDriver().getWindowHandle();
            getDriver().switchTo().window(currentWindow);
            ((JavascriptExecutor)getDriver()).executeScript("window.focus();");
            Thread.sleep(300); // Small delay for focus to take effect
        } catch (Exception e) {
            logger.warn("Warning: Could not ensure window focus", e);
        }
    }

    /**
     * Waits for an element to be present in the DOM
     */
    protected WebElement waitForElement(By locator, int timeoutInSeconds) {
        ensureWindowFocused();
        try {
            return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutInSeconds))
                    .ignoring(StaleElementReferenceException.class)
                    .ignoring(NoSuchElementException.class)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            ensureWindowFocused();
            throw new TimeoutException("Locator element not found: " + locator.toString());
        }
    }


    public void actionsKeyPerform(Keys keys) {
        Actions actions = new Actions(getDriver());
        actions.sendKeys(keys).perform();
        actions.release();
    }

    public WebElement waitTillVisible(String xPath) {
        return getDriverWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
    }

    public WebDriverWait getDriverWait() {
        if (driverWait == null) {
             if (!isDriverInitialized) {
                initializeDriver();
            }
        }
        return driverWait;
    }

    public WebElement waitTillClickable(String waPinnedSpanTitle) {
        return getDriverWait().until(ExpectedConditions.elementToBeClickable(By.xpath(waPinnedSpanTitle)));
    }

    public abstract void traverseGroupsTemplate(SocialParentCommonUtils instance)
            throws InterruptedException, AWTException, IOException;

    //Getters and Setters
    public XPathInterface getxPathInterface() {
        if (xPathInterface == null && socialModel != null) {
            xPathInterface = socialModel.getxPathInterface();
        }
        return xPathInterface;
    }
    public void setxPathInterface(XPathInterface xPathInterface) {
        this.xPathInterface = xPathInterface;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    // ...
    public boolean isTraverseDependsOnAllSuccessGroupList() {
        return traverseDependsOnAllSuccessGroupList;
    }
    public void setTraverseDependsOnAllSuccessGroupList(boolean traverseDependsOnAllSuccessGroupList) {
        this.traverseDependsOnAllSuccessGroupList = traverseDependsOnAllSuccessGroupList;
    }
    @Override
    public String toString() {
        return "SocialParentCommonUtils{" +
                "baseUrl='" + baseUrl + '\'' +
                '}';
    }

    public SocialModel getSocialModel() {
        SocialModel socialModelFromContext = ThreadLocalAutomationContext.getContext().getSocialModel();
        if (socialModelFromContext != null) {
            return socialModelFromContext;
        }
        if (socialModel == null) {
            socialModel = initialSetup.getSocialTypeToSocialModel().get(socialType);
        }
        ThreadLocalAutomationContext.getContext().setSocialModel(socialModel);
        return socialModel;
    }

    /**
     * Creates a new WebDriver instance with isolated profile for parallel execution
     * @param profileName Unique name for the browser profile
     * @param headless Whether to run in headless mode
     * @return Configured WebDriver instance
     */
    protected WebDriver createIsolatedWebDriver(String profileName, boolean headless) {
        try {
            ChromeOptions options = new ChromeOptions();
            
            // Set up user data directory with profile isolation
            String userDataDir = profileBaseDir + "/" + profileName;
            options.addArguments(
                "--user-data-dir=" + userDataDir,
                "--profile-directory=" + profileName,
                "--start-maximized",
                "--disable-infobars",
                "--disable-extensions",
                "--disable-notifications",
                "--no-sandbox",
                "--disable-dev-shm-usage"
            );
            
            if (headless) {
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
            }
            
            // Additional performance optimizations
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            
            // Disable automation flags to avoid detection
            options.setExperimentalOption("excludeSwitches", 
                java.util.Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
            
            WebDriver driver = new ChromeDriver(options);
            
            // Set timeouts
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SEC));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SEC));
            
            // Execute CDP commands to avoid detection
            ((ChromeDriver) driver).executeCdpCommand(
                "Page.addScriptToEvaluateOnNewDocument", 
                new HashMap<String, Object>() {{
                    put("source", 
                        "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
                }}
            );
            
            logger.info("Created new WebDriver instance with profile: {}", profileName);
            return driver;
            
        } catch (Exception e) {
            logger.error("Failed to create WebDriver instance: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initialize WebDriver", e);
        }
    }

    /**
     * Executes a task with proper context setup and cleanup.
     * @param task The task to execute
     * @param <T> The return type of the task
     * @return The result of the task
     */
    protected <T> T executeWithContext(WATGParent.ThrowingSupplier<T> task) {
        try (AutomationContext ignored = ThreadLocalAutomationContext.getContext()) {
            ThreadContext.push("Instance-" + instanceId);
            return task.get();
        } catch (Exception e) {
            logger.error("Error in task execution", e);
            throw new RuntimeException("Task execution failed", e);
        } finally {
            cleanupDriver();
            ThreadContext.pop();
            ThreadContext.clearStack();
        }
    }

    /**
     * Functional interface for tasks that can throw exceptions.
     * @param <T> The return type
     */
    @FunctionalInterface
    protected interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Cleans up the WebDriver instance for the current thread.
     */
    protected void cleanupDriver() {
        AutomationContext context = ThreadLocalAutomationContext.getContext();
        if (context != null && context.getTaskType() != null) {
            try {
                logger.debug("Cleaning up WebDriver for profile: {}", context.getTaskType());
                parallelWebDriverManager.quitDriver(context.getTaskType());
            } catch (Exception e) {
                logger.error("Error during WebDriver cleanup for profile: {}", context.getTaskType(), e);
            }
        }
    }
    public void setSocialModel(SocialModel socialModel) {
        this.socialModel = socialModel;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskTypeMapping(TaskType taskType, SocialParentCommonUtils socialParentCommonUtils) {
        this.taskType = taskType;
        this.taskTypeInstanceMap.put(taskType, socialParentCommonUtils);
    }
}