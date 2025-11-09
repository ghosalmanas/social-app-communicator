package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.*;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for Web Automation Task Group (WATG) operations with thread-safe WebDriver management.
 * Each thread gets its own WebDriver instance to enable parallel execution.
 */
@Component("watgParent")
public abstract class WATGParent extends WATGCommonUtils {
    private static final Logger logger = LogManager.getLogger(WATGParent.class);
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private final int instanceId = instanceCounter.incrementAndGet();
    private final String pinnedGroup = "MG Communications";//Hard-coded
    private final String pinnedGroupSecond = "Akash Kumar";//Hard-coded

    private final ThreadLocal<Set<String>> alreadyTraversedGroupsOnlyThreadLocal = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Set<String>> alreadyTraversedGroupsAndIndividualsThreadLocal = ThreadLocal.withInitial(TreeSet::new);

    @Value("${wa.waitForLoadingAllChatsForGroupJoiningDuringTraversal:false}")
    private boolean shouldWaitForLoadingInWhatsapp;
    /**
     * Gets the WebDriver instance for the current thread, creating it if necessary.
     *
     * @return WebDriver instance for the current thread
     */
    @Override
    public WebDriver getDriver() {
        AutomationContext context = ThreadLocalAutomationContext.getContext();
        if (context == null || context.getTaskType() == null) {
            throw new IllegalStateException("AutomationContext or profile name is not set in ThreadLocal");
        }
        logger.debug("Getting WebDriver from WATGParent for profile: {}", context.getTaskType());
        return parallelWebDriverManager.getDriver(context.getTaskType());
    }


    /**
     * Gets the JavascriptExecutor for the current thread's WebDriver.
     *
     * @return JavascriptExecutor instance
     */
    protected JavascriptExecutor getJsExecutor() {
        AutomationContext context = ThreadLocalAutomationContext.getContext();
        if (context == null || context.getTaskType() == null) {
            throw new IllegalStateException("AutomationContext or profile name is not set in ThreadLocal");
        }
        return parallelWebDriverManager.getJsExecutor(context.getTaskType());
    }


    /**
     * Clears all thread-local sets for both the current thread and all registered contexts.
     * This ensures no stale group data remains across thread pools or request boundaries.
     *
     * @param taskType The type of task being executed (unused in current implementation)
     * @return Always returns true
     */
    protected boolean clearAlreadyTraversedOrBroadcastedGroupsInExecution(TaskType taskType) {
        try {
            // Clear current thread's data
            Set<String> currentThreadGroups = alreadyTraversedGroupsOnlyThreadLocal.get();
            if (currentThreadGroups != null) {
                currentThreadGroups.clear();
            }

            Set<String> currentThreadGroupsAndIndividuals = alreadyTraversedGroupsAndIndividualsThreadLocal.get();
            if (currentThreadGroupsAndIndividuals != null) {
                currentThreadGroupsAndIndividuals.clear();
            }

            // Clear data for all registered contexts
            if (!ThreadLocalAutomationContext.getAllContexts().isEmpty()) {
                ThreadLocalAutomationContext.getAllContexts().forEach(ctx -> {
                    if (ctx != null && ctx.getInstance() instanceof WATGParent) {
                        WATGParent instance = (WATGParent) ctx.getInstance();
                        Set<String> groups = instance.getAlreadyTraversedGroupsOnlyThreadLocal().get();
                        if (groups != null) {
                            groups.clear();
                        }

                        Set<String> groupsAndIndividuals = instance.getAlreadyTraversedGroupsAndIndividualsThreadLocal().get();
                        if (groupsAndIndividuals != null) {
                            groupsAndIndividuals.clear();
                        }
                    }
                });
            }

            return true;
        } catch (Exception e) {
            logger.error("Error while clearing thread-local groups:", e);
            return false;
        }
    }

    /**
     * Removes specific groups from both alreadyTraversedGroupsOnlyThreadLocal and
     * alreadyTraversedGroupsAndIndividualsThreadLocal across all threads.
     * This is useful when you want to re-process specific groups without clearing all data.
     *
     * @param groupNames List of group names to remove from the already traversed sets
     * @return true if operation completed successfully, false otherwise
     */
    protected boolean removeGroupsFromAlreadyTraversed(List<String> groupNames) {
        if (groupNames == null || groupNames.isEmpty()) {
            logger.warn("No group names provided to remove from already traversed groups");
            return false;
        }

        try {
            int removedFromGroupsOnly = 0;
            int removedFromGroupsAndIndividuals = 0;

            // Remove from current thread's data
            Set<String> currentThreadGroups = alreadyTraversedGroupsOnlyThreadLocal.get();
            if (currentThreadGroups != null) {
                removedFromGroupsOnly += currentThreadGroups.removeAll(groupNames) ? 1 : 0;
            }

            Set<String> currentThreadGroupsAndIndividuals = alreadyTraversedGroupsAndIndividualsThreadLocal.get();
            if (currentThreadGroupsAndIndividuals != null) {
                removedFromGroupsAndIndividuals += currentThreadGroupsAndIndividuals.removeAll(groupNames) ? 1 : 0;
            }

            // Remove from all registered contexts
            if (!ThreadLocalAutomationContext.getAllContexts().isEmpty()) {
                ThreadLocalAutomationContext.getAllContexts().forEach(ctx -> {
                    if (ctx != null && ctx.getInstance() instanceof WATGParent) {
                        WATGParent instance = (WATGParent) ctx.getInstance();

                        // Remove from groups only
                        Set<String> groups = instance.getAlreadyTraversedGroupsOnlyThreadLocal().get();
                        if (groups != null) {
                            groups.removeAll(groupNames);
                        }

                        // Remove from groups and individuals
                        Set<String> groupsAndIndividuals = instance.getAlreadyTraversedGroupsAndIndividualsThreadLocal().get();
                        if (groupsAndIndividuals != null) {
                            groupsAndIndividuals.removeAll(groupNames);
                        }
                    }
                });
            }

            logger.info("Removed {} groups from already traversed sets ({} from groups only, {} from groups and individuals)",
                    removedFromGroupsOnly + removedFromGroupsAndIndividuals,
                    removedFromGroupsOnly,
                    removedFromGroupsAndIndividuals);

            return true;

        } catch (Exception e) {
            logger.error("Error while removing groups from already traversed sets:", e);
            return false;
        }
    }


    @Autowired
    public WATGParent(
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup, parallelWebDriverManager);
        logger.info("WATGParent instance with instanceId :{} initialized", instanceId);
    }


    @Override
    public void traverseGroupsTemplate(SocialParentCommonUtils instance) throws InterruptedException, AWTException, IOException {

        traverseGroupsTemplate((WATGCommonUtils) instance);

    }

    @Override
    public void traverseGroupsTemplate(WATGCommonUtils instancePassed)
            throws InterruptedException, AWTException, IOException {

        logger.info("WATGParent.traverseGroupsTemplate called for taskType:{}", instancePassed.getTaskType());
        // Execute with proper context and cleanup
        boolean success = executeWithContext(() -> {
            AutomationContext context = ThreadLocalAutomationContext.getContext();
            if (context == null) {
                logger.error("AutomationContext is null");
                return false;
            }
            try {
                logger.info("WATGParent.traverseGroupsTemplate inside context for taskType:{}, taskId:{} {} {}", context.getTaskType(), context.getTaskId(), taskTypeInstanceMap, taskTypeInstanceMap
                        .keySet());

                WATGCommonUtils instance;
                if (context.getInstance() != null) {
                    instance = (WATGCommonUtils) context.getInstance();
                    logger.info("WATGParent instance read from context:{}", context.getTaskType());
                } else {
                    instance = (WATGCommonUtils) taskTypeInstanceMap.get(context.getTaskType());//pick the instance by taskType from context. we can use this mapping in AutomationContext as well
                    logger.info("WATGParent instance read from taskTypeInstanceMap:{}", context.getTaskType());

                }

                if (instance == null) {
                    logger.info("REALLY UNEXPECTED: WATGParent.traverseGroupsTemplate instance is null for taskType:{}", context.getTaskType());
                    return false;
                }

                logger.info("WATGParent.traverseGroupsTemplate called for taskType:{},{},{}, instance: {} socialType:{}",
                        context.getTaskType(), context.getTaskId(), instance.getTaskType(), instance.getBaseUrl(), instance.getSocialModel().getSocialType());

                // Use context.getTraversedGroupsOnly() and context.getTraversedGroupsAndIndividuals()
                // ... existing traversal logic ...
                // Request-scoped collections stored in RequestContext

                SocialModel socialModel = context.getSocialModel();//initialSetup.getSocialTypeToSocialModel().get(instance.getSocialModel().getSocialType());
                XPathInterfaceWATG xPathInterfaceWATG = (XPathInterfaceWATG) socialModel.getxPathInterface();


                logger.info("WATGParent.traverseGroupsTemplate called for socialModel:{}", socialModel.getSocialType());
                //update the fields in parent
                super.setxPathInterface(socialModel.getxPathInterface());
                super.setBaseUrl(socialModel.getBaseURL());


                // Initialize thread-local driver
                WebDriver driver = initialSetup.setupAndExecuteSocialBySocialType(socialModel.getSocialType()); // This will initialize both driver and jsExecutor
                logger.info("Thread Local context values for driver:{} taskType:{} profile:{} driver:{} taskId:{} thread name:{}", driver.getCurrentUrl(), context.getTaskType(), context.getTaskType(), context.getDriver().getCurrentUrl(), context.getTaskId(), Thread.currentThread().getName());
                logger.info("Are we in the same context ThreadLocalAutomationContext.getContext()==context ? {}", ThreadLocalAutomationContext.getContext() == context);

                if (ThreadLocalAutomationContext.getContext() == context && context.getDriver().getCurrentUrl().contains("new-tab-page")) {
                    logger.info("New tab page detected");
                    //switchToMainWindow();
                    logger.info("Current thread is moving to sleep for 20 seconds for taskType:{}", context.getTaskType());
                    Thread.yield();
                    this.wait(120000);
                }


                //check if landed on the correct scanner page
                if (instance.getTaskType() != context.getTaskType() || !instance.getxPathInterfaceWATG().getBASE_URL().equals(context.getDriver().getCurrentUrl())) {
                    logger.info("Landed on the wrong scanner page for instance.getTaskType:{} context.getTaskType:{} instance.getBaseUrl:{} context.getDriver.getCurrentUrl:{}", context.getTaskType(), instance.getTaskType(), instance.getBaseUrl(), context.getDriver().getCurrentUrl());
                    return false;
                }


                if (instance.getEnumFlowType() == FlowType.SYNC_GROUP_NAMES_WITH_SENT_FOR_THE_DAY_FILE) {
                    instance.takeActionIfTheGroupIsEligibleToProceed(instance, false, null, null, 0, 0);
                    return true;
                }

                if (instance.getTaskType() == TaskType.WHATSAPP_BROADCAST_AD_TO_CONSULTANTS) {
                    instance.takeActionIfTheGroupIsEligibleToProceed(instance, false, null, null, 0, 0);
                    return true;
                }


                if (instance.getEnumFlowType() == FlowType.UN_ARCHIVE_SUPPORT_GROUP) {
                    instance.takeActionIfTheGroupIsEligibleToProceed(instance, false, null, null, 0, 0);
                    return true;
                }


                String pinnedGroupInXpath = null;

                try {
                    if (instance.getEnumFlowType() == FlowType.UN_ARCHIVE_SUPPORT_GROUP) {
                        //Lets wait for the pinned element to kick off
                        pinnedGroupInXpath = waitIdentifyAndClickOnThePinnedElement(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE());
                    } else {
                        pinnedGroupInXpath = waitIdentifyAndClickOnThePinnedElement(xPathInterfaceWATG.getWA_PINNED_SPAN_TITLE()); //MG Communications
                    }
                } catch (NoSuchElementException e) {
                    logger.error("NoSuchElementException while waiting for the pinned element to kick off", e);
                } catch (Exception e) {
                    logger.error("Exception while waiting for the pinned element to kick off", e);
                }

                logger.info("PINNED_GROUP_RETRIEVED :{}", pinnedGroupInXpath);

                logger.info("PINNED_GROUP_HARD_CODED :{}", pinnedGroup);

                logger.info("PINNED_GROUP_SECOND_HARD_CODED :{}", pinnedGroupSecond);

                // Mainly useful for traversal and message posting
                // Use the injected JavascriptExecutor bean which is already properly configured
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(420));
                // The jsExecutor is now injected as a constructor parameter
                WebElement leftChatPanelContainerWithScroll = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN())));


                /* NOTE: URGENT search with //* and Move all the hardcoded xpath into xpaths files, else TG works and WA fails or vice versa*/

                // Traversal with Robot class each and every groups and chats

                Properties properties = getConfigProperties();

                Set<String> allPhoneContacts = new TreeSet<>();
                Set<String> alreadyTraversedGroupsOnly = new LinkedHashSet<>(instance.getAlreadyTraversedGroupsOnlyAndUpdateDate());
                Set<String> alreadyTraversedGroupsAndIndividuals = new LinkedHashSet<>();
                Set<String> invalidGroupNames = new TreeSet<>();
                Set<String> setOfDataToExport = new LinkedHashSet<>();


                boolean waitForLoadingAllChatsForGroupJoiningDuringTraversal = shouldWaitForLoadingInWhatsapp;//Boolean.parseBoolean(properties.getProperty("wa.waitForLoadingAllChatsForGroupJoiningDuringTraversal", "false"));
                String filePathToExportContactsData = instance.getFilePathToExportContactsData();
                Tika tika = new Tika();

                int robotExecutionCounter = 0;
                int prevCounter = 0;
                int counter = 0;
                int repeatCounter = 0;
                int continuousAlreadyTraversedStoredCounter = 0;
                int continuousAlreadyPresentDiffGroupAndIndCounter = 0;
                int continuousSameGroupRepeatCounter = 0;

                long totalDurationForEachIteration = 0L;

                String lastVisitedGroup = "";
                String lastVisitedButNonSentGroupPrevToSuccessToMoveBack = "";
                String lastPerfect1stTimeVisitedGroup = "";
                //Stack<String> stackOfAlreadyVisitedGroups= new Stack<String>();

                boolean hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = false;
                boolean success_takeActionIfTheGroupIsEligibleToProceed = false;

                boolean hasMoreElementsToTraverse = true;
                int countAlreadyExists = 0;
                String groupNameExceptIndvForLastVisitedTracker = "";
                boolean robotFasteningOccurred = false;
                int processCompletedSuccessfullyCounter = 0;
                int howManyFreshGroupTraversedCounter = 0;
                String localDateTime = getLocalTimeInFormat.get();
                LocalDateTime currLocalDateTime = LocalDateTime.now();
                boolean clearAlreadyTraversedSetInRuntime = false;

		/*Predicate<String> predicateCollectGroupNamesOrGroupMustBeInSupportList = groupNameExceptIndvid -> ifSuccessfulGroupsContainsCheckRequired(
				instance, groupNameExceptIndvid);*/
                // instance.conditionValidationForGroupContains(groupNameExceptIndvid); //delete whenever cleaning

                isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = TASK_SOCIAL_DATA.get(context.getTaskType())
                        .isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days();

                logger.info("Before Traversal: alreadyTraversedGroupsOnly : "
                        + String.join(",", alreadyTraversedGroupsOnly));

                logger.info(
                        "Before Traversal: isAlreadyUpdatedThePersonalGroupAndContactsForTheDay : "
                                + isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA + " instance.getEnumFlowType() : "
                                + instance.getEnumFlowType().name());

                logger.info("WaitForLoadingAllChatsForGroupJoiningDuringTraversal : " + waitForLoadingAllChatsForGroupJoiningDuringTraversal + " instance.getSocialType() : " + instance.getxPathInterface().getSocialType().name() + " instance.getEnumFlowType() : " + instance.getEnumFlowType().name());
                //Wait for the chats to load
                if (waitForLoadingAllChatsForGroupJoiningDuringTraversal && instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP && instance.getEnumFlowType() == FlowType.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS) {
                    int chat_download_millis = 60000 * 10;
                    logger.error("SLEEPING for " + chat_download_millis + "_MILLI_SECONDS before traversal so that the chats are DOWNLOADED to make the GROUP LINKS to JOIN instead of incomplete work...");
                    Thread.sleep(chat_download_millis);//Adjust as per the situation
                    logger.error("Ignoring & clearing alreadyTraversedGroupsOnly from file for fresh traversal each time... for SOCIAL_TYPE: {}", instance.getSocialModel().getSocialType().name());
                    alreadyTraversedGroupsOnly.clear();
                }


                Thread.sleep(getSLEEP_TIME_MS() * 10L);





                logger.info("*****=========================================== STARTING TRAVERSAL WITH WHILE LOOP...DATE-TIME: " + getLocalTimeInFormat.get() + " TASKTYPE: " + taskType + " ============================**********");

                while (hasMoreElementsToTraverse) {

                    Thread.sleep(getSLEEP_TIME_MS()/ 10L);

                    localDateTime = getLocalTimeInFormat.get(); //update time for each execution

                    logger.info("\n\n");
                    logger.error("Iteration Started...Date-Time: " + localDateTime + " TaskType: " + taskType + " FreshGroupTraversed : " + howManyFreshGroupTraversedCounter + " counter:" + counter + " lastVisitedGroup:" + lastVisitedGroup + " lastVisitedButNonSentGroupPrevToSuccessToMoveBack :" + lastVisitedButNonSentGroupPrevToSuccessToMoveBack);

                    if (howManyFreshGroupTraversedCounter != 0 && howManyFreshGroupTraversedCounter % 10 == 0) {
                        //Clear the alreadyTraversedGroups in runtime by property injection
                        if (!clearAlreadyTraversedSetInRuntime) {
                            properties = getConfigProperties();
                            clearAlreadyTraversedSetInRuntime = Boolean.parseBoolean(properties.getProperty("wa.clear_the_already_traversed_set", "false"));
                            if (clearAlreadyTraversedSetInRuntime) {
                                alreadyTraversedGroupsOnly.clear();
                                logger.info("AlreadyTraversedGroupsOnly is cleared by injection in GetConfigProperties clearAlreadyTraversedSetInRuntime :  " + clearAlreadyTraversedSetInRuntime + " TaskType: " + taskType);
                            }

                        }
                        try {
                            //Scroll down after certain number of groups traversal
                            getJsExecutor().executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];", leftChatPanelContainerWithScroll, 200);
                        } catch (Exception e) {
                            logger.error("Exception in Javascript executor", e);
                        }
                    }

                    //Restore to previous group if needed
                    if (hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious && (counter >= 10 || !getDriver().findElements(By.xpath("//*[text()='" + pinnedGroup + "']")).isEmpty()) /*checking if the group pushed at the top*/) {
                        String lastVisitedEligibleRestoreGroup = (!lastVisitedButNonSentGroupPrevToSuccessToMoveBack.isEmpty() && !lastVisitedButNonSentGroupPrevToSuccessToMoveBack.equalsIgnoreCase(pinnedGroup) && !lastVisitedButNonSentGroupPrevToSuccessToMoveBack.equalsIgnoreCase(pinnedGroupSecond)) ? lastVisitedButNonSentGroupPrevToSuccessToMoveBack : lastVisitedGroup;
                        logger.info("searchAndClickOnLastVisitedGroup ? counter : " + counter + " TaskType: " + taskType + " hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious : " + hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious + " lastVisitedEligibleRestoreGroup : " + lastVisitedEligibleRestoreGroup);
                        hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = instance.searchAndClickOnLastVisitedGroup(counter, hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious, lastVisitedEligibleRestoreGroup);
                    }

                    try {
                        // ROBOT CLASS
                        if (instance.getEnumFlowType() != FlowType.ARCHIVE_SUPPORT_GROUP) {
                            if (continuousAlreadyPresentDiffGroupAndIndCounter == 40) {
                                // Achieve basic blockers and try to go to-fro to refresh the page
                                // removeBlockersStuckAndRefreshToMoveToNextChat(lastVisitedGroup);

                            } else {
                                //runRobotClassForNextChat();
                                runActionClassForNextChat();
                            }
                        }

                        // ROBOT CLASS
                        if (instance.getEnumFlowType() != FlowType.ARCHIVE_SUPPORT_GROUP) {
                            /*
                             * if (getxPathInterface().isSocialTypeWhatsApp()) { if (continuousNextCounter %
                             * 3 == 0) { robotFasteningOccurred = true; runRobotClassForNextChat(500,
                             * robotFasteningOccurred);// fastening causing not loading the chat for //
                             * telegram } } else if (robotFasteningOccurred && continuousNextCounter == 1)
                             */
                            if (getxPathInterface().getSocialType() == SocialType.WHATSAPP) {
                                if (continuousSameGroupRepeatCounter > 0 && continuousSameGroupRepeatCounter % 5 == 0) {
                                    Thread.sleep(getSLEEP_TIME_MS() / 2);
                                    logger.info("continuousSameGroupRepeatCounter :::: " + continuousSameGroupRepeatCounter + " TaskType: " + taskType);
                                    //doSomeAwaking(lastVisitedGroup, continuousSameGroupRepeatCounter / 5);
                                    continuousSameGroupRepeatCounter = 0;//
                                    Thread.sleep(getSLEEP_TIME_MS() / 2);
                                }

                                totalDurationForEachIteration = ChronoUnit.SECONDS.between(LocalDateTime.now(), currLocalDateTime);
                                System.out.println("The total sleep time in each iteration in seconds is: " + totalDurationForEachIteration);
                                currLocalDateTime = LocalDateTime.now();


                                if ((continuousAlreadyPresentDiffGroupAndIndCounter % 10 == 0 || continuousAlreadyTraversedStoredCounter % 10 == 0) && continuousSameGroupRepeatCounter < 2 && (getSLEEP_TIME_MS() > 200 || totalDurationForEachIteration > 1000)) {
                                    robotFasteningOccurred = true;
                                    setSLEEP_TIME_MS(getSLEEP_TIME_MS() - 200);
                                }
                                /*
                                 * if(continuousNextCounter< 10) { setSLEEP_TIME_MS(getSLEEP_TIME_MS_RESET());
                                 * robotFasteningOccurred = false; }
                                 */

                                logger.info("*------ ContinuousSameGroupNextCounter::" + continuousSameGroupRepeatCounter + " TaskType: " + taskType + " ContinuousNextCounter :: " + continuousAlreadyPresentDiffGroupAndIndCounter + " robotFasteningOccurred::" + robotFasteningOccurred +" getSLEEP_TIME_MS :"+getSLEEP_TIME_MS());
                            }

                            //robotFasteningOccurred = false;
                            //runRobotClassForNextChat(0, robotFasteningOccurred); //enable

                        }

                        /* ===================Group Name Retrieval logic============================================= */
                        String findRightTopGroupName = decideGroupOrIndvUserAnnouncement_and_findRightTopWebElementForGroupOnly(lastVisitedGroup, instance.getEnumFlowType());
                        //Individual User or invalid cases for Admins
                        if (findRightTopGroupName == null) {
                            continue;
                        }

                        //Handle the sudden break and loaded in top case without sent situation like by awakening
                        if(howManyFreshGroupTraversedCounter>=10 && (findRightTopGroupName.equalsIgnoreCase(pinnedGroup) || findRightTopGroupName.equalsIgnoreCase(pinnedGroupSecond))){
                            String lastVisitedEligibleRestoreGroup = (!lastVisitedButNonSentGroupPrevToSuccessToMoveBack.isEmpty() && !lastVisitedButNonSentGroupPrevToSuccessToMoveBack.equalsIgnoreCase(pinnedGroup) && !lastVisitedButNonSentGroupPrevToSuccessToMoveBack.equalsIgnoreCase(pinnedGroupSecond)) ? lastVisitedButNonSentGroupPrevToSuccessToMoveBack : lastVisitedGroup;
                            logger.info("searchAndClickOnLastVisitedGroup by accidental awakening for fresh group traversal ? howManyFreshGroupTraversedCounter : " + howManyFreshGroupTraversedCounter + " TaskType: " + taskType + " hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious : " + hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious + " lastVisitedEligibleRestoreGroup : " + lastVisitedEligibleRestoreGroup);
                            hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = instance.searchAndClickOnLastVisitedGroup(counter, hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious, lastVisitedEligibleRestoreGroup);
                            continue;
                        }
                        // findRightTopGroupName = getTopGroupNameInUtf8(tika, findRightTopGroupName);

                        String currentGroupNameExceptIndv = findRightTopGroupName;

                        if (currentGroupNameExceptIndv.equals(lastVisitedGroup)) {
                            continuousSameGroupRepeatCounter = continuousSameGroupRepeatCounter + 1;
                            logger.info("SAME_GROUP_REPEATED_ " + currentGroupNameExceptIndv + " TaskType: " + taskType + " pinnedGroup: " + pinnedGroup);
                        } else if (!lastVisitedGroup.isEmpty() && currentGroupNameExceptIndv.trim().equals(pinnedGroup.trim())) {
                            logger.info("PINNED_GROUP_TRAVERSE_FOUND_ " + pinnedGroup + " TaskType: " + taskType + " MOVING_TO PREVIOUS_1ST_TIME_VISITED_GROUP_" + lastVisitedGroup);
                            //searchAndClickOnGroup(lastPerfect1stTimeVisitedGroup);
                            continuousSameGroupRepeatCounter = continuousSameGroupRepeatCounter + 1;
                        } else {
                            logger.info("PROPER_NEXT_EXECUTION_FOUND_ " + currentGroupNameExceptIndv + " TaskType: " + taskType + " PREVIOUS_GROUP_WAS_" + lastVisitedGroup + " pinnedGroup: " + pinnedGroup);
                            continuousSameGroupRepeatCounter = 0;
                        }

                        // ROBOT CLASS for ARCHIVE_SUPPORT_GROUP
                        if (instance.getEnumFlowType() == FlowType.ARCHIVE_SUPPORT_GROUP) {

                            if (!success_takeActionIfTheGroupIsEligibleToProceed
                                    || /* robotExecutionCounter%2==0 || */ currentGroupNameExceptIndv
                                    .equals(groupNameExceptIndvForLastVisitedTracker)) {
                                //runRobotClassForNextChat();
                                //runActionClassForNextChat();
                                Thread.sleep(getSLEEP_TIME_MS());
                            }

                            robotExecutionCounter = robotExecutionCounter + 1;
                            alreadyTraversedGroupsOnly.clear();
                            alreadyTraversedGroupsAndIndividuals.clear();

                            if (!neglectToJoinGroupsWithTextSetLowerCase.isEmpty()) {
                                neglectToJoinGroupsWithTextSetLowerCase.clear();
                            }

                        }


                        logger.info(
                                "Start inside While : lastVisitedGroup :" + lastVisitedGroup + " TaskType: " + taskType + " countAlreadyExists :" + countAlreadyExists + " has/MoreElementsToTraverse: " + hasMoreElementsToTraverse
                                        + " alreadyTraversedGroupsOnly Size : " + alreadyTraversedGroupsOnly.size());

                        String groupInfo = null;
                        lastVisitedGroup = groupNameExceptIndvForLastVisitedTracker;

                        groupNameExceptIndvForLastVisitedTracker = currentGroupNameExceptIndv;
                        logger.info(
                                "lastVisitedGroup: " + lastVisitedGroup + " TaskType: " + taskType + " groupNameExceptIndv :" + currentGroupNameExceptIndv);

                        if (alreadyTraversedGroupsOnly.contains(currentGroupNameExceptIndv)) {
                            continuousAlreadyTraversedStoredCounter = continuousAlreadyTraversedStoredCounter + 1;
                        } else {
                            continuousAlreadyTraversedStoredCounter = 0;
                        }

                        // Handle continuous next try on last element : start
                        if (alreadyTraversedGroupsAndIndividuals.contains(currentGroupNameExceptIndv)) {

                            @SuppressWarnings("unchecked")
                            Map<String, Object> prevValRepeatLimit = (Map<String, Object>) checkIfPrevCurrGroupSameOrRepeatExhausted(instance,
                                    currentGroupNameExceptIndv, lastVisitedGroup, hasMoreElementsToTraverse, countAlreadyExists);
                            hasMoreElementsToTraverse = (boolean) prevValRepeatLimit.get("hasMoreElementsToTraverse");
                            countAlreadyExists = (Integer) prevValRepeatLimit.get("countAlreadyExists");
                            continuousAlreadyPresentDiffGroupAndIndCounter = continuousAlreadyPresentDiffGroupAndIndCounter + 1;

                            logger.info(
                                    "Check Already Exists:: Continuous next targetted for first/last group : groupNameExceptIndv :" + currentGroupNameExceptIndv
                                            + " countAlreadyExists: " + countAlreadyExists + " hasMoreElementsToTraverse: " + hasMoreElementsToTraverse);
                            String reason = "ALREDY_TRAVERSED_FOR_THE_DAY";
                            logger.info("\n\n ************ Eligibility Failed......Reason : ***** " + reason + " *****" + " TaskType: " + taskType+"**************************");
                            logger.info("\nCurrent Group " + currentGroupNameExceptIndv + " has been " + reason + " ::Present in the TraversedList: \n" + alreadyTraversedGroupsOnly + " TaskType: " + taskType);
                            lastVisitedButNonSentGroupPrevToSuccessToMoveBack = currentGroupNameExceptIndv;
                            Thread.sleep(getSLEEP_TIME_MS()/10);

                            continue;

                        } else {
                            alreadyTraversedGroupsAndIndividuals.add(currentGroupNameExceptIndv);
                            counter++;
                            continuousAlreadyPresentDiffGroupAndIndCounter = 0;
                            setSLEEP_TIME_MS(getSLEEP_TIME_MS_RESET());
                            robotFasteningOccurred = false;
                        }


                        // Handle continuous next try on last element : end
                        List<WebElement> elemGroupInfoOrPhOrAnnouncements = driver
                                .findElements(By.xpath(getxPathInterface()
                                        .getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT()));

                        if (elemGroupInfoOrPhOrAnnouncements.isEmpty()) {
                            logger.info("webElement.isEmpty::: " + currentGroupNameExceptIndv + " TaskType: " + taskType);
                            continue;
                        } else {

                            groupInfo = elemGroupInfoOrPhOrAnnouncements.getFirst().getText();

                            logger.info(
                                    "is groupInfo a group?: " + instance.getxPathInterface().getGroupInfo().test(groupInfo)
                                            + " alreadyTraversedGroupsOnly :" + alreadyTraversedGroupsOnly + " TaskType: " + taskType);

                            if (instance.getxPathInterface().getGroupInfo().test(groupInfo)) {
                                // Start: Add functionality to avoid newly added/created personal groups or
                                // personal contact
                                //if (isRequiredToUpdatePersonalGroupsAndContacts(instance)) {
                                // updateExludePersonalContactListFile(groupNameExceptIndv);//todo:
                                // uncomment/work
                                //}
                                // End

                                //printLogEligibilityValidationResult(alreadyTraversedGroupsOnly,predicateCollectGroupNamesOrGroupMustBeInSupportList, groupNameExceptIndv);

                                if (validateTheEligibility(alreadyTraversedGroupsOnly, currentGroupNameExceptIndv)) {
                                    logger.info("============================Start=======================================" + " TaskType: " + taskType);
                                    logger.info(
                                            "-----*--- ELIGIBLE ---*---- Group present in SupportGroup and Not Present in Exclusion/neglect group list : "
                                                    + currentGroupNameExceptIndv + " not present in alreadyTraversedGroupsOnly:"
                                                    + alreadyTraversedGroupsOnly + " TaskType: " + taskType);

                                    // if (!alreadyTraversedGroupsOnly.contains(groupNameExceptIndv))

                                    /*----------Actual Task for each group for FB, WA, TG--------------------*/
                                    @SuppressWarnings("unchecked")
                                    Map<ActionIfTheGroupIsEligible, Object> result = (Map<ActionIfTheGroupIsEligible, Object>) instance
                                            .takeActionIfTheGroupIsEligibleToProceed(instance,
                                                    hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious, currentGroupNameExceptIndv,
                                                    allPhoneContacts, counter, repeatCounter);

                                    success_takeActionIfTheGroupIsEligibleToProceed = (boolean) result
                                            .get(ActionIfTheGroupIsEligible.FLAG_TRUE);
                                    hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = success_takeActionIfTheGroupIsEligibleToProceed;
                                    // groupNameExceptIndv = (String)
                                    // result.get(ActionIfTheGroupIsEligible.GROUP_NAME);
                                    Object phoneContactsObj = result.get(ActionIfTheGroupIsEligible.SET_OF_DATA);
                                    if (phoneContactsObj instanceof Set) {
                                        try {
                                            @SuppressWarnings("unchecked")
                                            Set<String> phoneContacts = (Set<String>) phoneContactsObj;
                                            allPhoneContacts = new HashSet<>(phoneContacts);
                                        } catch (ClassCastException e) {
                                            logger.warn("Failed to cast phone contacts to Set<String>", e);
                                            allPhoneContacts = new HashSet<>();
                                        }
                                    } else {
                                        allPhoneContacts = new HashSet<>();
                                    }
                                    counter = (int) result.get(ActionIfTheGroupIsEligible.COUNTER);
                                    repeatCounter = (int) result.get(ActionIfTheGroupIsEligible.REPEAT_COUNTER);
                                    logger.info(
                                            "After Retrival :: hasNewGroupJoinedButNotRestored : "
                                                    + hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious + " allPhoneContacts size: "
                                                    + allPhoneContacts.size() + " counter: " + counter + " repeatCounter: "
                                                    + repeatCounter + " TaskType: " + taskType);
                                    logger.info(
                                            "Meet criteria checks : " + currentGroupNameExceptIndv + " counter: " + counter
                                                    + " prevCounter: " + prevCounter + " alreadyTraversedGroupsOnly.size "
                                                    + alreadyTraversedGroupsOnly.size() + " TaskType: " + taskType);
                                    alreadyTraversedGroupsOnly.add(currentGroupNameExceptIndv);
                                    logger.info("Eligible Group : " + currentGroupNameExceptIndv
                                            + " added in alreadyTraversedGroupsOnly" + " TaskType: " + taskType);
                                    logger.info("========================End===========================================" + " TaskType: " + taskType);

                                    countAlreadyExists = 0;
                                    howManyFreshGroupTraversedCounter++;
                                    lastPerfect1stTimeVisitedGroup = currentGroupNameExceptIndv;

                                } else {
                                    lastVisitedButNonSentGroupPrevToSuccessToMoveBack = currentGroupNameExceptIndv;
                                    logger.error(
                                            "Oh No! this Group does not meet eligibility criteria checks : " + currentGroupNameExceptIndv + " counter: " + counter
                                                    + " prevCounter: " + prevCounter + " alreadyTraversedGroupsOnly.size "
                                                    + alreadyTraversedGroupsOnly.size() + " TaskType: " + taskType);
                                }

                                logger.info("Data Export Eligible? alreadyTraversedGroupsOnly has data: " + !alreadyTraversedGroupsOnly.isEmpty() + " filePathToExportContactsData: " + filePathToExportContactsData + " ExpectedSizeToExport: " + instance.getExpectedSizeToModuloToInsertExportToFile() + " TaskType: " + taskType);

                                if (!alreadyTraversedGroupsOnly.isEmpty() && filePathToExportContactsData != null && !filePathToExportContactsData.isEmpty() && alreadyTraversedGroupsOnly.size()
                                        % instance.getExpectedSizeToModuloToInsertExportToFile() == 0) {

                                    logger.info("alreadyTraversedGroups after " + instance.getExpectedSizeToModuloToInsertExportToFile() + " occurances : " + alreadyTraversedGroupsOnly.toString() + " TaskType: " + taskType);

                                    @SuppressWarnings("unchecked")
                                    Map<EnumStringToExport, Set<String>> exportData = (Map<EnumStringToExport, Set<String>>) getHashTableToExportData(allPhoneContacts,
                                            alreadyTraversedGroupsOnly, invalidGroupNames, setOfDataToExport);
                                    logger.info("fileNameToExportData : " + filePathToExportContactsData + " exportDataToFile : " + alreadyTraversedGroupsOnly.toString() + " TaskType: " + taskType);

                                    instance.exportDataToFile(filePathToExportContactsData, exportData);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        logger.info("Exception occured in catch :" + ex.getLocalizedMessage() + " TaskType: " + taskType);
                        ex.printStackTrace();
                        continue;
                    }
                }


                if ((alreadyTraversedGroupsOnly == null || alreadyTraversedGroupsOnly.isEmpty())
                        && (allPhoneContacts == null || allPhoneContacts.isEmpty())) {
                    logger.info("Fail: No Data Found alreadyTraversedGroups : " + alreadyTraversedGroupsOnly
                            + "allPhoneContacts : " + allPhoneContacts + " TaskType: " + taskType);
                    return false;
                }

                allPhoneContacts.add("Execution Completed..........." + localDateTime);
                alreadyTraversedGroupsOnly.add("Execution Completed..........." + localDateTime);
                logger.info("Success: alreadyTraversedGroups : " + alreadyTraversedGroupsOnly
                        + " allPhoneContacts : " + allPhoneContacts.size() + " TaskType: " + taskType);

                // Template4
                // WhatsappContactUtils.exportToFile(SUPPORT_EXTRACTED_CONTACTS,
                // allPhoneContacts, groupNames, groupNamesInvalid);
                @SuppressWarnings("unchecked")
                Map<EnumStringToExport, Set<String>> exportData = (Map<EnumStringToExport, Set<String>>) getHashTableToExportData(allPhoneContacts,
                        alreadyTraversedGroupsOnly, invalidGroupNames, setOfDataToExport);

                instance.exportDataToFile(filePathToExportContactsData, exportData);

                logger.info(
                        "Success: isAlreadyUpdatedThePersonalGroupAndContactsForTheDay : "
                                + isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA + " instance.getEnumFlowType() : "
                                + instance.getEnumFlowType().name() + " TaskType: " + taskType);


                alreadyTraversedGroupsAndIndividualsThreadLocal.set(alreadyTraversedGroupsAndIndividuals);
                alreadyTraversedGroupsOnlyThreadLocal.set(alreadyTraversedGroupsOnly);
                // todo:instance.updateCurrDateAndReloadFile with impl and empty for others
                /*
                 * if(isRequiredToUpdatePersonalGroupsAndContacts(instance)) {
                 * clearUpdateCurrDateAndReloadFile(excludePersonalGroupContactNamesFromCsv,
                 * "group_traverse_already_date_",excludePersonalGroupContactNamesOrNumbersSet,
                 * false); isAlreadyUpdatedThePersonalGroupAndContactsForTheDay=true; }
                 */
                logger.info(
                        "Process Completed Successfully instance.getEnumFlowType() : " + instance.getEnumFlowType().name() + " TaskType: " + taskType);

                if (processCompletedSuccessfullyCounter < 5) {
                    traverseGroupsTemplate(instance);
                }
            } catch (Exception e) {
                logger.error("Error during group traversal" + " TaskType: " + taskType, e);
                throw e;
            } finally {
                //quitDriver();
            }
            //driver.close();
            return false;
        });
    }


    private void switchToMainWindow() {
        Set<String> windowHandles = driver.getWindowHandles();
        for (String windowHandle : windowHandles) {
            driver.switchTo().window(windowHandle);
        }
    }


    public void quitDriver() {
        try {
            cleanupDriver();
            logger.info("Successfully cleaned up WebDriver for thread: {}" + " TaskType: " + taskType, Thread.currentThread().getName());
        } catch (Exception e) {
            logger.error("Error during WebDriver cleanup" + " TaskType: " + taskType, e);
        }
    }

    protected static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public void setWebDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public WebDriver getWebDriver() {
        return driverThreadLocal.get();
    }


    public String decideGroupOrIndvUserAnnouncement_and_findRightTopWebElementForGroupOnly(String lastVisitedGroup, FlowType flowTypeEnum) throws InterruptedException {

        boolean isAnyExceptionOccurrred = false;
        String perfectGroupName = null;
        String decideAGroup = null;
        try {
            /* Remember that code change is better than xPath change*/
            /* Allow admins for WA for Contact extractions.. Write all Use cases for clarity and avoid blocking any use case*/
            /*
             * Use Cases:
             * 1.Message Posting: Allow Only Groups. Not admins, Subscribers, MessageNotAllowed and individual users
             * 2.Traverse and contacts extractions and join new groups: Allow Groups, admins and subscribers as well. Not user.
             * 3.Archive: Allow Groups and Admins for WA
             * 4.UnArchieve: All
             * 5.Sync Group Names: Possible/applicable only for Message posting case 1. so admins not required
             *   --- So we must need to have Groups and Admins. No use of individual user in ay case
             */

            //Thread.sleep(1000);//safe guard to populate the final sub-header like ph Nos but NA

            //Retrieve only group case and Decide if a Group or an individual user or an admin group for WA
            List<WebElement> subHeaderToDecideAGroups = febxs(
                    getxPathInterface().getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT());

            //Includes Perfect group and Admins as well
            List<WebElement> groupNames = febxs(
                    getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP());//All for WA but only members and Subscribers for TG, not user

            //It it an admin group
            boolean isItsAnAdminGroup = !febxs(
                    getxPathInterface().getFIND_ADMIN_GROUP_NAME_RIGHT_TOP()).isEmpty();//All for WA but only members and Subscribers for TG, not user

            //Exceptional ad-hoc cases where it says members but message is not allowed : Not required as of now as the impact is exception in the log only
            /*boolean isMessageTextNotAllowedInPostBox = !febxs(
                    getxPathInterface().getFIND_MESSAGE_TEXT_NOT_ALLOWED_IN_POST_BOX()).isEmpty();*/

            //check if a group with subscribers in TG
            if (!subHeaderToDecideAGroups.isEmpty()) {
                perfectGroupName = subHeaderToDecideAGroups.getFirst().getText();
            }

            //Cover the explicit cases first like user and then admin and return.

            //individual users. May be for announcement as well
            if (subHeaderToDecideAGroups.isEmpty() || subHeaderToDecideAGroups.getFirst().getText().contains("contact info") || subHeaderToDecideAGroups.getFirst().getText().contains("last seen") || subHeaderToDecideAGroups.getFirst().getText().equals("online")) {
                List<WebElement> individualUsers = febxs(
                        getxPathInterface().getFIND_INDV_USER_NAME_RIGHT_TOP());
                if (individualUsers.isEmpty()) {
                    logger.error("ALERT: individualUsers is Empty: URGENT_INTERVENTION_REQUIRED: ERROR due to may be PAGE_FOCUS OR PAGE_LOAD" + " TaskType: " + taskType+" : lastVisitedGroup : "+lastVisitedGroup+" : "+ThreadLocalAutomationContext.getContext().getTaskType());//null for TG.NA
                    if(!lastVisitedGroup.isEmpty() && !lastVisitedGroup.equalsIgnoreCase(pinnedGroup) && !lastVisitedGroup.equalsIgnoreCase(pinnedGroupSecond)) {
                       Thread.sleep(5000);
                        doSomeAwaking(lastVisitedGroup, 4);//Simply search and reload
                    }
                    return null;
                }
                String individualGroupName = individualUsers.isEmpty() ? null : individualUsers.getFirst().getText();//Optional. Not required
                logger.info("Skip Individual Users :: " + individualGroupName + " TaskType: " + taskType);//null for TG.NA
                return null;
            }

            //Traverse and contacts extractions: Allows admins
            if (isItsAnAdminGroup && !groupNames.isEmpty()) {
                String adminGroupName = groupNames.getFirst().getText();
                if (flowTypeEnum == FlowType.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS || flowTypeEnum == FlowType.ARCHIVE_SUPPORT_GROUP || flowTypeEnum == FlowType.UN_ARCHIVE_SUPPORT_GROUP) {
                    logger.info("Allowing Admin Group for Archive and Traversal :: " + adminGroupName + " TaskType: " + taskType);//null for TG.NA
                    return adminGroupName;
                } else {
                    //Skip the Admin for rest of the cases
                    logger.info("********* Skipping Admin Group for PostingMsg, SyncGroupNames :: " + adminGroupName + " TaskType: " + taskType);//null for TG.NA
                    return null;
                }
            }

            //in immediate time, individual users also has a value like 'Business User' or 'Click Here to see contact info' and after a second its empty means
            //whereas Group should have either 'Click here to see Group info' or comma separated contacts

            //Now Finally its turn for the Actual and Perfect Group name
            if (!subHeaderToDecideAGroups.isEmpty() && !groupNames.isEmpty()) {
                decideAGroup = subHeaderToDecideAGroups.iterator().next().getText();
                if (decideAGroup != null && decideAGroup.contains("group info") || decideAGroup.contains(",") || decideAGroup.contains("members")) {
                    perfectGroupName = groupNames.getFirst().getText();
                    logger.info("Perfect eligible group name retrived :: " + perfectGroupName + " TaskType: " + taskType);
                    return perfectGroupName;
                }
            }

            logger.error("Something is wrong retrieving the group name :: " + perfectGroupName + " subHeaderToDecideAGroups is empty? :" + subHeaderToDecideAGroups.isEmpty() + " isItsAnAdminGroup : " + isItsAnAdminGroup + " groupnames is empty? " + groupNames.isEmpty() + " TaskType: " + taskType);

            //Worst case if a group but some discrepancy.So to invoke the exceptions, print traces and handling
            perfectGroupName = febx(
                    getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP()).getText();

            return perfectGroupName;


        } catch (MoveTargetOutOfBoundsException ex) {
            logger.error("Exception retrieving top right header MoveTargetOutOfBoundsException : " + " TaskType: " + taskType
                    + ex.getLocalizedMessage());
            isAnyExceptionOccurrred = true;
        } catch (StaleElementReferenceException ex) {
            logger.error("Exception retrieving top right header StaleElementReferenceException : " + " TaskType: " + taskType
                    + ex.getLocalizedMessage());
            isAnyExceptionOccurrred = true;
        } catch (NoSuchElementException nse) {
            logger.error("Exception retrieving top right header NSE : "
                    + nse.getLocalizedMessage());
            isAnyExceptionOccurrred = true;
        } catch (NoSuchSessionException nss) {
            logger.error("Exception retrieving top right header NoSuchSessionException: " + " TaskType: " + taskType + nss.getLocalizedMessage());
            isAnyExceptionOccurrred = true;
            doSomeAwaking(null, 5);
        } catch (Exception ex) {
            logger.error("Exception retrieving top right header: " + " TaskType: " + taskType + ex.getLocalizedMessage());
            isAnyExceptionOccurrred = true;
        } finally {
            if (isAnyExceptionOccurrred) {
                doSomeAwaking("", 3);
                List<WebElement> groupOrIndvEls = febxs(getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP());
                if (!groupOrIndvEls.isEmpty()) {
                    return groupOrIndvEls.getFirst().getText();
                }
            }
        }
        return null;
    }


    boolean validateTheEligibility(Set<String> alreadyTraversedGroupsOnly, String groupNameExceptIndv) throws InterruptedException {
/*
        boolean neverTraversed = alreadyTraversedGroupsOnly.stream()
                .noneMatch(sentGroup -> sentGroup.trim().toLowerCase()
                        .contains(groupNameExceptIndv.toLowerCase())
                        || groupNameExceptIndv.trim().toLowerCase()
                        .contains(sentGroup.trim().toLowerCase()));
        boolean notPresentInPersonalGroupList = excludePersonalGroupNamesSet.stream()
                .noneMatch(sentGroup -> sentGroup.trim().toLowerCase()
                        .contains(groupNameExceptIndv.toLowerCase())
                        || groupNameExceptIndv.trim().toLowerCase()
                        .contains(sentGroup.trim().toLowerCase()));
        boolean notPresentInExclusionGroupList = excludeSupportGroupNamesSet.stream()
                .noneMatch(sentGroup -> sentGroup.trim().toLowerCase()
                        .contains(groupNameExceptIndv.toLowerCase())
                        || groupNameExceptIndv.trim().toLowerCase()
                        .contains(sentGroup.trim().toLowerCase()));
        boolean notPresentInNeglectToJoinGroupList = neglectToJoinGroupsWithTextSetLowerCase.stream()
                .noneMatch(sentGroup -> groupNameExceptIndv.trim().toLowerCase()
                        .contains(sentGroup.trim().toLowerCase()));

*/

        boolean neverTraversed = alreadyTraversedGroupsOnly.stream()
                .noneMatch(sentGroup -> sentGroup.trim()
                        .equalsIgnoreCase(groupNameExceptIndv)
                        || groupNameExceptIndv.trim()
                        .equalsIgnoreCase(sentGroup.trim()));
        boolean notPresentInPersonalGroupList = excludePersonalGroupNamesSet.stream()
                .noneMatch(sentGroup -> sentGroup.trim()
                        .equalsIgnoreCase(groupNameExceptIndv)
                        || groupNameExceptIndv.trim()
                        .equalsIgnoreCase(sentGroup.trim()));
        boolean notPresentInExclusionGroupList = excludeSupportGroupNamesSet.stream()
                .noneMatch(sentGroup -> sentGroup.trim()
                        .equalsIgnoreCase(groupNameExceptIndv)
                        || groupNameExceptIndv.trim()
                        .equalsIgnoreCase(sentGroup.trim()));
        boolean notPresentInNeglectToJoinGroupList = neglectToJoinGroupsWithTextSetLowerCase.stream()
                .noneMatch(sentGroup -> groupNameExceptIndv.trim()
                        .equalsIgnoreCase(sentGroup.trim()));
        logger.info("Eligibility to Process started... All must be true :::: neverTraversed: " + neverTraversed + " TaskType: " + taskType + " notPresentInPersonalGroupList: " + notPresentInPersonalGroupList + " notPresentInExclusionGroupList: " + notPresentInExclusionGroupList + " notPresentInNeglectToJoinGroupList: " + notPresentInNeglectToJoinGroupList);


        boolean isEligibleToShootToEachGroup = /*predicateCollectGroupNamesOrGroupMustBeInSupportList.test(groupNameExceptIndv)*///fixed false
                neverTraversed
                        && notPresentInPersonalGroupList
                        && notPresentInExclusionGroupList
                        && notPresentInNeglectToJoinGroupList;
        String reason = "";
        if (!isEligibleToShootToEachGroup) {
            if (!neverTraversed) {
                reason = "ALREDY_TRAVERSED_FOR_THE_DAY";
                logger.info("Current Group " + groupNameExceptIndv + " has been " + reason + " ::Present in the TraversedList: \n" + alreadyTraversedGroupsOnly + " TaskType: " + taskType);
            }
            if (!notPresentInPersonalGroupList) {
                reason = "ALREDY_PRESENT_IN_PERSONAL_GROUP_LIST";
            }
            if (!notPresentInExclusionGroupList) {
                reason = "ALREDY_PRESENT_IN_EXCLUSION_GROUP_LIST";
            }
            if (!notPresentInNeglectToJoinGroupList) {
                reason = "ALREDY_PRESENT_IN_NEGLECT_TO_JOIN_GROUP_LIST";
            }

            logger.info("\n************ Eligibility Failed......Reason : ***** " + reason + " *****" + " TaskType: " + taskType+"**************************\n");
            Thread.sleep(getSLEEP_TIME_MS()/10L);
        }

        return isEligibleToShootToEachGroup;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public ThreadLocal<Set<String>> getAlreadyTraversedGroupsOnlyThreadLocal() {
        return alreadyTraversedGroupsOnlyThreadLocal;
    }

    public ThreadLocal<Set<String>> getAlreadyTraversedGroupsAndIndividualsThreadLocal() {
        return alreadyTraversedGroupsAndIndividualsThreadLocal;
    }
}
