package com.wtf.app.service.impls;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.*;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.parent.WATGParent;
import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.service.interfaces.IAllWAGroupNamesRetrievalAndJoinNewGroups;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import javax.xml.transform.Result;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/*@Service("watgFetchAllWAContactsWGroupNamesService")
@Lazy*/
public class WATGFetchAllWAContactsWGroupNamesService extends WATGParent implements IAllWAGroupNamesRetrievalAndJoinNewGroups {

    private static final Logger logger = LogManager.getLogger(WATGFetchAllWAContactsWGroupNamesService.class);
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private final int instanceId = instanceCounter.incrementAndGet();

    private static final String YOU_CANT_ACCESS_THIS_CHAT_NOT_A_MEMBER = "//*[contains(text(), 'Unfortunately, you can')]";

    private final ThreadLocal<Set<String>> alreadyJoinedGroupLinksThreadLocal = ThreadLocal.withInitial(LinkedHashSet::new);
    private final ThreadLocal<Integer> continuousLoadingCounterTL = ThreadLocal.withInitial(() -> 0);
    private static final FlowType enumFlowType = FlowType.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS;
    boolean isSuccessfulSupportGroupContainsCheckRequired = false;


    @Autowired
    public WATGFetchAllWAContactsWGroupNamesService(InitialSetup initialSetup,
                                                    ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup, parallelWebDriverManager);
        logger.info("WATGFetch instance with instanceId :{} initialized", instanceId);
        logger.info("WATGFetchAllWAContactsWGroupNamesService initialized with  InitialSetup, and WebDriver");
    }

    @Async("automationTaskExecutor")
    public CompletableFuture<Void> start(AutomationContext context, TaskType taskType) {
        return CompletableFuture.runAsync(() -> {
            try {

                SocialType socialType = taskType.getSocialType();
                SocialModel socialModel = initialSetup.getSocialTypeToSocialModel().get(socialType);
                // Set context for the thread
                context.setInstance(this);
                context.setSocialModel(socialModel);//redundant but useful for centralized information
                ThreadLocalAutomationContext.setContext(context);

                this.setSocialType(taskType.getSocialType());
                this.setFlowTypeEnum(flowType);
                this.setSocialModel(socialModel);
                this.setxPathInterface(socialModel.getxPathInterface());
                this.setxPathInterfaceWATG((XPathInterfaceWATG) socialModel.getxPathInterface());
                this.setBaseUrl(socialModel.getBaseURL());
                this.setTaskTypeMapping(taskType, this);

                // Your existing logic here
                fetchAllWAGroupNamesAndJoinNewGroups(this);
            } catch (Exception e) {
                logger.error("Error during WATG fetch all contacts and group names service execution", e);
            }
        });
    }

    @Override
    public String getFilePathToExportContactsData() {
        return getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS();
    }

    @Override
    public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
        TaskSocialData taskSocialData = getTaskSocialData();
        return clearUpdateCurrDateAndReloadFile(taskSocialData.getVisitedGroupFileName(), GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG, taskSocialData.getVisitedGroupNames(), true);
    }

    private static TaskSocialData getTaskSocialData() {
        return TASK_SOCIAL_DATA.get(ThreadLocalAutomationContext.getContext().getTaskType());
    }

    @Override
    public int getExpectedSizeToModuloToInsertExportToFile() {
        return 5;
    }


    @Override
    public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored, String lastVisitedGroup) {
        searchAndClickOnGroup(lastVisitedGroup);
        hasNewGroupJoinedButNotRestored = false;
        return hasNewGroupJoinedButNotRestored;
    }

    @Override
    public boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping) {
        WhatsappDBFileRepository.exportToFile(fileName, setOfDataMapping.get(EnumStringToExport.NEW_PH_CONTACTS), setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES), setOfDataMapping.get(EnumStringToExport.ERROR_GROUP_NAMES));
        WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(getTaskSocialData().getVisitedGroupFileName(), setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
        return true;
    }

    @Override
    public void fetchAllWAGroupNamesAndJoinNewGroups(WATGCommonUtils instance) throws InterruptedException, AWTException, IOException {
        try {

            // Set flow type for this operation
            setEnumFlowType(FlowType.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS);

            //instance.setSocialModel(socialModel);
            // Call the template method from parent
            instance.traverseGroupsTemplate(instance);//enable later if required

            // Export the collected data
            String exportPath = getFilePathToExportContactsData();

            logger.info("Successfully exported group data to: {}", exportPath);

        } catch (Exception e) {
            logger.error("Error in fetchAllWAGroupNames_JoinNewGroups: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Async
    public CompletableFuture<Result> asyncMethod() {
        // This will run in a separate thread
        // and can use the asyncWebDriver
        Result result = null;
        return CompletableFuture.completedFuture(result);
    }

    @Override
    protected int getMaxAllowedRepeatOfSameGroup() {
        return 100;
    }

    @Override
    protected int processCompletedSuccessfullyCounter() {
        return 5;
    }

    @Override
    public boolean clearAlreadyTraversedOrBroadcastedGroupsFromDBExecution(TaskType taskType) {
        super.clearAlreadyTraversedOrBroadcastedGroupsInExecution(taskType);
        clearAllAlreadyTraversedInDBFile(TaskType.WHATSAPP_FETCH_CONTACTS);
        return clearAllAlreadyTraversedInDBFile(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
    }

    @Override
    public boolean clearAlreadyTraversedOrBroadcastedGroupsInExecution(TaskType taskType) {
         return super.clearAlreadyTraversedOrBroadcastedGroupsInExecution(taskType);
    }

    @Override
    public boolean clearAllAlreadyTraversedInDBFile(TaskType taskType) {
        TaskSocialData taskSocialData = TASK_SOCIAL_DATA.get(taskType);
        if (taskSocialData != null) {
            //Take backup
            WhatsappDBFileRepository.takeBackup(taskSocialData.getVisitedGroupFileName());
            return WhatsappDBFileRepository.clearFileContent(taskSocialData.getVisitedGroupFileName());
        }
        return false;
    }

    @Override
    public boolean removeGroupsFromAlreadyTraversed(List<String> groups) {
        return super.removeGroupsFromAlreadyTraversed(groups);
    }

    @Async
    @Override
    public void traverseGroups(WATGCommonUtils instance) throws InterruptedException, AWTException, IOException {
        logger.info("Starting to traverse groups in WATGFetchAllWAContactsWGroupNamesService");

        try {

            // Call the main processing method
            fetchAllWAGroupNamesAndJoinNewGroups(instance);

            logger.info("Successfully traversed groups in WATGFetchAllWAContactsWGroupNamesService");

        } catch (Exception e) {
            logger.error("Error while traversing groups: " + e.getMessage(), e);
            throw e;
        }
    }

    public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(WATGCommonUtils instance, boolean hasNewGroupJoinedButNotRestored, String groupNameExceptIndv, Set<String> allPhoneContacts, int counter, int repeatCounter) throws InterruptedException {

        logger.info("overriden takeActionIfTheGroupIsEligibleToProceed > hasNewGroupJoinedButNotRestored: " + hasNewGroupJoinedButNotRestored + " groupNameExceptIndv: " + groupNameExceptIndv + " allPhoneContacts size: " + allPhoneContacts.size() + " counter: " + counter + " repeatCounter: " + repeatCounter);

        updateWAAndTGSpecificDifferentSleepTime(null, instance, 2000, 200);

        if (instance.getxPathInterface().getSocialType() == SocialType.TELEGRAM) {
            febxsAndClickWithRetry("//*[@title='Go to bottom']", null, false, "The_Go_To_Button_For_Unread_Chats ");
        } else if (instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP) {

            if (continuousLoadingCounterTL.get() >=-10) {
                executeIfSyncingRequired(instance);

                boolean chatsStillLoading = !febxs("//*[contains(@title,'Loading messages')]").isEmpty()
                        || !febxs("//*[text()='Waiting for this message. This may take a while.']").isEmpty()
                        || !febxs("//*[text()='Syncing paused. Open WhatsApp on your phone to continue syncing.']").isEmpty();

                if (chatsStillLoading) {
                    continuousLoadingCounterTL.set(continuousLoadingCounterTL.get() + 1);
                    logger.info("ChatsStillLoading..So delaying for longer periods for SOCIAL_TYPE: {}", instance.getSocialModel().getSocialType().name());
                    updateWAAndTGSpecificDifferentSleepTime(null, instance, 5000/*10000*//*10000*/, 5000);
                    doSomeAwaking(groupNameExceptIndv, 3);
                }else{
                    continuousLoadingCounterTL.set(continuousLoadingCounterTL.get() - 1);
                }
            }else{
                logger.info("ChatsStillLoading not loaded as its -ve times for SOCIAL_TYPE: {}", instance.getSocialModel().getSocialType().name());
            }

            logger.info("Just After ChatsStillLoading check for SOCIAL_TYPE: {}", instance.getSocialModel().getSocialType().name());
        }
        boolean isCurrGroupANewPersonalCreatedGroup = false;
        //UseCases to handle
        //handle TELANGANA US IT STAFFING  REMOTE No Hotlist

        //for WA
        if (instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP) {

            //1. extractContacts
            Set<String> phContactsChunk = extractContactsByGroupMouseHover(3000);

            //Start : Avoid accessing newly created Personal Group
            isCurrGroupANewPersonalCreatedGroup = validateBroadcastRiskIfNewPersonalGroupFound(groupNameExceptIndv,
                    phContactsChunk);

            //Merge with allContacts if newExtractcted Contacts are valid
            if (!isCurrGroupANewPersonalCreatedGroup) {
                allPhoneContacts = addMergePhChunksIntoAllPhoneContacts(allPhoneContacts, phContactsChunk);
            }
            //End : Avoid accessing newly created Personal Group

            logger.info("allPhoneContacts updated size: in overriden takeActionIfTheGroupIsEligibleToProceed:  " + allPhoneContacts.size() + " isCurrGroupANewPersonalCreatedGroup : " + isCurrGroupANewPersonalCreatedGroup + " for SOCIAL_TYPE: " + instance.getxPathInterface().getSocialType().name());
        }

        if (!isCurrGroupANewPersonalCreatedGroup) {
            //2. Join group inside a group
            try {
                hasNewGroupJoinedButNotRestored = joinAllGroups(instance, groupNameExceptIndv, getTaskSocialData().getJoinGroupNames(), neglectToJoinGroupsWithTextSetLowerCase, 3000);

            } catch (Exception exception) {
                exception.printStackTrace();
                logger.error("Exception in joinAllGroups : " + exception.getLocalizedMessage());
                hasNewGroupJoinedButNotRestored = true;
            }
        }

        Map<ActionIfTheGroupIsEligible, Object> actionMap = Map.ofEntries(Map.entry(ActionIfTheGroupIsEligible.FLAG_TRUE, hasNewGroupJoinedButNotRestored), Map.entry(ActionIfTheGroupIsEligible.GROUP_NAME, groupNameExceptIndv), Map.entry(ActionIfTheGroupIsEligible.SET_OF_DATA, allPhoneContacts), Map.entry(ActionIfTheGroupIsEligible.COUNTER, counter++), Map.entry(ActionIfTheGroupIsEligible.REPEAT_COUNTER, repeatCounter));
        return actionMap;
    }

    private void executeIfSyncingRequired(WATGCommonUtils instance) throws InterruptedException {
        List<WebElement> syncing = febxs("//*[text()='Syncing older messages. Click to see progress.']");
        if (!syncing.isEmpty()) {
            logger.info("Syncing older messages clicked..So delaying for longer periods");
            try {
                syncing.getFirst().click();
                Thread.sleep(getSLEEP_TIME_MS()*3L);
            } catch (Exception e) {
                logger.error("Exception in syncing older messages : " + e.getLocalizedMessage());
            }
            updateWAAndTGSpecificDifferentSleepTime(null, instance, 1000/*5000*//*10000*/, 100);
            febxsAndClick("//div[text()='OK']/parent::div/parent::button", null, false, "Syncing older messages. Click to see progress. ");
            executeIfSyncingRequired(instance);
        }
    }


    private boolean joinAllGroups(WATGCommonUtils instance, String currentGroup, Set<String> alreadyJoinedGroupLinksFromFileDB,
                                  Set<String> neglectToJoinGroupsWithTextSetLowerCase, long sleep) throws InterruptedException {

        String previousGroupLink = "";
        String previousGroup = "";

        Set<String> alreadyJoinedGroupLinks = alreadyJoinedGroupLinksThreadLocal.get();

        alreadyJoinedGroupLinks.addAll(alreadyJoinedGroupLinksFromFileDB);
        boolean[] hasNewGroupJoinedButNotRestored = new boolean[]{false};

        int whileCounter = 1;
        int joinGroupLinksSize = getDriver().findElements(By.xpath(instance.getxPathInterface().getJOIN_NEW_GROUP_LINK())).size();
        boolean isAnyPendingGroupLinksRemainingToBeCovered = true;
        int sameGroupLinkInSameGroupCounter = 0;

        //Ideally loop not required as we are clicking a single group at a time
        while (isAnyPendingGroupLinksRemainingToBeCovered) {

            if (whileCounter % 50 == 0) {//To avoid infinity flow. Considering 50 links cant be in a group
                logger.info("Sorry !! BREAKING the while loop as the counter modulo of 20 currentGroup: " + currentGroup + " whileCounter :" + whileCounter);
                isAnyPendingGroupLinksRemainingToBeCovered = false;
                return false;
                //break;
            }
            whileCounter++;
            List<WebElement> webElements = getDriver().findElements(By.xpath(instance.getxPathInterface().getJOIN_NEW_GROUP_LINK()));

            //Thread.sleep(1000);// SafeSide
            if (webElements.isEmpty()) {
                logger.error("**** Sorry !! No Join Group Links Present in this group: " + currentGroup);
                return false;
            }

            try {
                Set<String> groupsLinks = webElements.stream().map(WebElement::getText).collect(Collectors.toSet());//convert to for loop if further stream recieved
                logger.info("joinAllGroupsLinks found links: counts" + groupsLinks.size() + " groupsLinks: " + groupsLinks);
                logger.info("joinLinkCurrentCount_webElements_size: " + webElements.size() + " whileCounter : " + whileCounter + " joinGroupLinksPreviousCount :" + joinGroupLinksSize);
            } catch (Exception ex) {
                logger.info("Exception in grouNamesToJoin link extractions: NOT error");
            }

            Optional<WebElement> joinGroupWEOptional = webElements.stream().filter(joinGroup1 -> !joinGroup1.getText().isEmpty() && !alreadyJoinedGroupLinks.contains(joinGroup1.getText())).findFirst();

            /* Break The Loop*/
            if (joinGroupWEOptional.isEmpty()) {
                logger.error("**** WOW  GOOD NO_GROUP_LINK_LEFT GROUP_LINK_ALREADY_CLICKED_EARLIER. BREAKING the while loop as groupLinks joinGroupWEOptional value not present. **   " + currentGroup);
                isAnyPendingGroupLinksRemainingToBeCovered = false;
                return false;
            }

            //Click on single groupLink at a time
            WebElement joinGroupWE = joinGroupWEOptional.get();
            //.forEach(joinGroupWE -> {

            try {
                //The Group is about to be clicked And This group is a fresh group to join. Not present in alreadyJoinedGroups file
                String groupLinkToJoin = joinGroupWE.getText();
                logger.info("Trying to click to join " + groupLinkToJoin);

                // Validating if checking consecutive SAME_LINK in SAME_GROUP again and again
                if (previousGroupLink.equals(groupLinkToJoin) && previousGroup.equals(currentGroup) && sameGroupLinkInSameGroupCounter >= 4) {
                    logger.error("BREAKING as SAME_GROUP and SAME_LINK the while loop consecutively used inside same group: " + currentGroup + " groupLinkToJoin: " + groupLinkToJoin + " sameGroupLinkInSameGroupCounter :" + sameGroupLinkInSameGroupCounter);
                    isAnyPendingGroupLinksRemainingToBeCovered = false;
                    return true;//break;
                } else if (previousGroupLink.equals(groupLinkToJoin) && previousGroup.equals(currentGroup)) {
                    logger.info("SAME_GROUP and SAME_LINK consecutively inside the while loop " + previousGroupLink + " sameGroupLinkInSameGroupCounter" + sameGroupLinkInSameGroupCounter);
                    sameGroupLinkInSameGroupCounter++;
                    continue;
                } else if (previousGroupLink.equals(groupLinkToJoin) && !previousGroup.equals(currentGroup)) {
                    logger.info("SAME_LINK but not SAME_GROUP and consecutively inside the while loop " + previousGroupLink + " sameGroupLinkInSameGroupCounter" + sameGroupLinkInSameGroupCounter);
                    continue;
                } else {
                    logger.error("Perfect Case: NOT_SAME_GROUP and NOT_SAME_LINK. Update previousGroupLink by new to verify consecutive SAME_LINK " + previousGroupLink);
                    previousGroupLink = groupLinkToJoin;
                    previousGroup = currentGroup;
                }

                logger.info("JOIN_GROUP_LINK **joinGroupWE** fresh link : ***" + groupLinkToJoin);
                //Thread.sleep(1000);// remove them if slowing
                if (joinGroupClickAndHandleExcs(currentGroup, joinGroupWE) == null) {//Click and verify
                    alreadyJoinedGroupLinks.add(groupLinkToJoin);// add into the alreadyJoinedGroup and continue the parent while loop
                    continue;
                }

                /*start----Group name or info validation for eligibility against neglect groups to join*/
                List<WebElement> groupJoinNameEls = getDriver().findElements(By.xpath(instance.getxPathInterface().getXPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON())).stream().filter(i -> !i.getText().isEmpty()).collect(Collectors.toList());
                //"//span[@data-testid='group-join-modal-group-name']"
                groupJoinNameEls.stream().map(i -> i.getText().toString()).forEach(str -> {
                    logger.info("JOIN_GROUP_NAME eligibility validation text if eligible : " + str);
                });

                boolean isGroupNameValidToJoin = groupJoinNameEls.stream().noneMatch(neglectGroupWL ->
                        neglectToJoinGroupsWithTextSetLowerCase.stream().anyMatch(neglectStr -> neglectGroupWL.getText().trim().toLowerCase().contains(neglectStr)));

                logger.info("JOIN_GROUP_NAME text content valid to join avoiding neglectWords/AlreadyExistWords ?  ..." + isGroupNameValidToJoin);

                if (isGroupNameValidToJoin) {
                    /*end --- Group name or Group info validation for eligibility against neglect groups to join*/
                    logger.info("Hurray !! valid Group Name Found in the link to join : " + groupLinkToJoin);
                    try {
                        if (popUp_joinGroupOrCancelForOtherCases_all("", "")) {
                            alreadyJoinedGroupLinks.add(groupLinkToJoin);
                            logger.error("FINALLY !!Join Group final clicked...text: " + groupLinkToJoin + " for group: " + currentGroup);
                        }

                    } catch (Exception ex) {
                        logger.info(String.format("Exception in joining new groupLink : ", getxPathInterface().getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN() + "//child::div" + " :::  " + alreadyJoinedGroupLinks));

                        if (instance.getxPathInterface().getSocialType() == SocialType.TELEGRAM) {
                            try {
                                if (popUp_joinGroupOrCancelForOtherCases_all("//child::div", "")) {
                                    alreadyJoinedGroupLinks.add(groupLinkToJoin);
                                    logger.info("join Group child div click...text: " + groupLinkToJoin);
                                }

                            } catch (Exception ex1) {
                                logger.info(String.format("Exception in joining new groupLink : ", getxPathInterface().getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN() + "//child::div" + " :::  " + alreadyJoinedGroupLinks));
                                ex1.printStackTrace();
                            }
                        } else {
                            throw ex;
                        }
                    }

                    if (!alreadyJoinedGroupLinks.isEmpty()) {
                        alreadyJoinedGroupLinks.add(groupLinkToJoin);
                        logger.info("New Group added to alreadyJoinedGroups :" + alreadyJoinedGroupLinks);

                        WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(getTaskSocialData().getJoinGroupFileName(), alreadyJoinedGroupLinks);
                        logger.info("New Group..exported: " + alreadyJoinedGroupLinks);
                        Thread.sleep(2000);
                    } else {
                        logger.info("Sorry! New Group ..link was unable to be joined.So NOT_ADDED into the alreadyJoinedGroups");
                    }
                    try {
                        if (instance.getxPathInterface().getSocialType() == SocialType.TELEGRAM) {
                            febxsAndClickWithRetry(EXACT_BACK_BUTTON, "", false, "the_exact_back_buton");//its same as back button line 295
                            logger.info("Back Button Executed...");
                        }
                    } catch (Exception exc) {
                        logger.info("exception : back-button...");
                    }

                    hasNewGroupJoinedButNotRestored[0] = true;
                    try {
                        if (instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP) {
                            clickOnCancelOrClose();
                        }
                    } catch (Exception ex) {
                        logger.info("exception : Cancel if any error or long hold...");
                        ex.printStackTrace();
                    }
                    logger.info("searchAndClickOnGroup to restore old group");
                    hasNewGroupJoinedButNotRestored[0] = !searchAndClickOnGroup(currentGroup);

                } else {
                    logger.info("JOIN_GROUP_NAME is INVALID to join So click: Close/Cancel/back");
                    try {
                        alreadyJoinedGroupLinks.add(groupLinkToJoin);
                        febxsAndClickWithRetry(instance.getxPathInterface().getBUTTON_CANCEL_CLICK_BACK(), "", false, "the_cancel_close_buton");
                    } catch (Exception ex) {
                        logger.info("Exception join Group final click: Cancel/back : " + ex.getMessage());
                        hasNewGroupJoinedButNotRestored[0] = !searchAndClickOnGroup(currentGroup);
                    }
                }
            } catch (Exception ex) {
                logger.info("Exception at the beginning of link join: : " + ex.getMessage());
                ex.printStackTrace();
                hasNewGroupJoinedButNotRestored[0] = !searchAndClickOnGroup(currentGroup);
            }
        }

        //});
        logger.info("joinAllGroupsLinks whileCounter : " + whileCounter + " joinGroupLinksPreviousCount :" + joinGroupLinksSize + " Difference groupLinks not eligible count: " + (joinGroupLinksSize - whileCounter));
        hasNewGroupJoinedButNotRestored[0] = !searchAndClickOnGroup(currentGroup);
        alreadyJoinedGroupLinksThreadLocal.set(alreadyJoinedGroupLinks);
        return hasNewGroupJoinedButNotRestored[0];
    }

    public Boolean joinGroupClickAndHandleExcs(String currentGroup, WebElement joinGroupWE) throws InterruptedException {
        try {
            joinGroupWE.click();
            if (!febxs(YOU_CANT_ACCESS_THIS_CHAT_NOT_A_MEMBER).isEmpty()) {
                return null;// add into the alreadyJoinedGroup and continue the parent while loop
            }
            Thread.sleep(1000);
            return true;
        } catch (Exception e) {
            try {
                logger.info("Exception in joinGroupWE.click() : " + e);
                Thread.sleep(2000);
                joinGroupWE.click();
                Thread.sleep(2000);
            } catch (Exception ex) {
                try {
                    logger.info("Exception 2 layers in joinGroupWE.click() and search and clicking: " + ex);
                    doSomeAwaking(currentGroup, 3);
                } catch (ExceptionInInitializerError exx) {
                    logger.info("Exception 3 layers in joinGroupWE.click() and search and clicking: " + ex);
                    searchAndClickOnGroup(currentGroup);
                }
            }
        }
        return false;
    }


    private Set<String> extractGroupNamesForCurrView(Set<String> groupNames) {
        List<WebElement> webElements = getDriver().findElements(By.xpath(
                "//span[starts-with(text(),':')]/parent::span[1]//parent::div[1]//parent::div[1]/preceding-sibling::div[1]/child::div[1]/child::span[1]"));
        groupNames.addAll(webElements.stream().map(i -> i.getText()).collect(Collectors.toSet()));
        return groupNames;
    }

    @Override
    public FlowType getEnumFlowType() {
        return enumFlowType;
    }

    @Override
    public boolean isSuccessfulSupportGroupContainsCheckRequired() {
        return isSuccessfulSupportGroupContainsCheckRequired;
    }


    /**
     * Gets the path to the already joined groups file
     * @return path to the file
     */
    /**
     * Gets the path to the already joined groups file
     *
     * @return path to the file
     */
    private String getALREADY_JOINED_GROUPS_TXT() {
        return getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS() + "\\already_joined_groups.txt";
    }


    /**
     * Navigates to WhatsApp Web
     */
    private void navigateToWhatsAppWeb() {
        try {
            getDriver().get("https://web.whatsapp.com/");
            logger.info("Navigated to WhatsApp Web");
        } catch (Exception e) {
            logger.error("Failed to navigate to WhatsApp Web", e);
            throw e;
        }
    }

    /**
     * Waits for WhatsApp Web to load
     *
     * @param instance WATGCommonUtils instance
     */
    private void waitForWhatsAppToLoad(WATGCommonUtils instance) throws InterruptedException {
        // Wait for the main WhatsApp Web UI to load
        int attempts = 0;
        while (attempts < 30) { // 30 * 2 seconds = 1 minute max wait
            if (febxs("//div[@data-testid='chat-list']").size() > 0) {
                logger.info("WhatsApp Web loaded successfully");
                return;
            }
            Thread.sleep(2000);
            attempts++;
        }
        throw new RuntimeException("Timed out waiting for WhatsApp Web to load");
    }


    /**
     * Navigates to WhatsApp Web
     */
    private void navigateToWhatsAppWeb_1() {
        try {
            // Implementation for navigating to WhatsApp Web
            getDriver().get("https://web.whatsapp.com/");
            waitForWhatsAppToLoad(this);
        } catch (Exception e) {
            logger.error("Error navigating to WhatsApp Web: " + e.getMessage(), e);
            throw new RuntimeException("Failed to navigate to WhatsApp Web", e);
        }
    }

    /**
     * Extracts group names from the current view
     *
     * @param groupNames Set to store the extracted group names
     */
    private void extractGroupNamesForCurrView_1(Set<String> groupNames) {
        try {
            // Implementation for extracting group names from the current view
            List<WebElement> groupElements = getDriver().findElements(By.xpath("//div[@role='row']//div[@role='row']//div[@role='gridcell']"));
            for (WebElement element : groupElements) {
                String groupName = element.getText().trim();
                if (!groupName.isEmpty()) {
                    groupNames.add(groupName);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting group names: " + e.getMessage(), e);
        }
    }

    // joinGroup() implementation removed - use joinGroupClickAndHandleExcs() instead
}
