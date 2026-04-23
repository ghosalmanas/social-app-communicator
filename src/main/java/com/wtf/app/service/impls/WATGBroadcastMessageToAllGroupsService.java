package com.wtf.app.service.impls;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.*;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.parent.WATGParent;
import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.service.interfaces.IBroadcasteAllSupportGroups;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import com.wtf.app.web.driver.WebDriverHealthMonitor;
import com.wtf.app.web.driver.WebDriverHealthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;


@Service("watgBroadcastMessageToAllGroupsService")
public class WATGBroadcastMessageToAllGroupsService extends WATGParent implements IBroadcasteAllSupportGroups {

    private static final Logger logger = LogManager.getLogger(WATGBroadcastMessageToAllGroupsService.class);
    private static final int SUPPORT_GROUP_SIZE_CREATED_BY_ME_FOR_SUPPORT_OR_BY_MEDIATOR_FOR_PROXY = 5;

    private final Set<String> alreadyTraversedAndMessageSentGroups = new LinkedHashSet<>();
    private final FlowType enumFlowType = FlowType.BROADCAST_SELF_SKILLSET_AD_TO_GROUPS;
    private final WebDriverHealthService webDriverHealthService;
    private final ParallelWebDriverManager driverManager;
    private boolean isSuccessfulSupportGroupContainsCheckRequired = false;
    private WATGCommonUtils instance;
    private final WhatsappDBFileRepository whatsappDBFileRepository;

    @Autowired
    WebDriverHealthMonitor webDriverHealthMonitor;

    @Autowired
    private SocialModel whatsappDTO;

    @Value("${isWarningNoteToAddAsMarketIsDownAndConsultantsAreNotContacting:false}")
    private boolean isWarningNoteToAddAsMarketIsDownAndConsultantsAreNotContacting;

    @Value("${wantToAddMillisAtMessageEndToMakeEachMessageUnique:false}")
    private boolean wantToAddMillisAtMessageEndToMakeEachMessageUnique;

    @Value("${wantToClearIfDraftedBeforeSend:false}")
    private boolean wantToClearIfDraftedBeforeSend;

    @Autowired
    public WATGBroadcastMessageToAllGroupsService(
            InitialSetup initialSetup,
            ParallelWebDriverManager driverManager,
            WebDriverHealthService webDriverHealthService,
            WhatsappDBFileRepository whatsappDBFileRepository) {
        super(initialSetup, driverManager);
        this.driverManager = driverManager;
        this.webDriverHealthService = webDriverHealthService;
        this.whatsappDBFileRepository = whatsappDBFileRepository;
        logger.info("WATGBroadcastMessageToAllGroupsService initialized with  InitialSetup, and WebDriver");
    }

    public void start(AutomationContext context, TaskType taskType) {
        logger.info("Starting broadcast service for {} with context ID: {}", socialType, context.getTaskId());
        try {
            SocialType socialType = taskType.getSocialType();
            SocialModel socialModel = initialSetup.getSocialTypeToSocialModel().get(socialType);

            // Set context and other properties before execution
            context.setInstance(this);
            context.setSocialModel(socialModel);
            ThreadLocalAutomationContext.setContext(context);

            this.setSocialType(taskType.getSocialType());
            this.setFlowTypeEnum(flowType);
            this.setSocialModel(socialModel);
            this.setxPathInterface(socialModel.getxPathInterface());
            this.setxPathInterfaceWATG((XPathInterfaceWATG) socialModel.getxPathInterface());
            this.setBaseUrl(socialModel.getBaseURL());
            this.setTaskTypeMapping(taskType, this);

            // Get or create WebDriver for this profile
            WebDriver driver = driverManager.getDriver(taskType);
            if (driver == null) {
                logger.error("Failed to initialize WebDriver for profile: {}", socialType);
                return;
            }
            logger.info("Now Register session with health monitor");

            // Register session with health monitor
            webDriverHealthService.registerSession(taskType);

            this.traverseGroupsTemplate((WATGCommonUtils) this);
        } catch (Exception e) {
            logger.error("Error during broadcast execution for {}", socialType, e);
            throw new RuntimeException("Broadcast failed for " + socialType, e);
        }
    }

    @Override
    public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(WATGCommonUtils instance, boolean hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious,
                                                                                           String groupNameExceptIndv, Set<String> allPhoneContacts, int counter, int repeatCounter)
            throws InterruptedException {

        logger.info("overriden takeActionIfTheGroupIsEligibleToProceed: " + groupNameExceptIndv + " hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious: " + hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious + " groupNameExceptIndv: " + groupNameExceptIndv + " allPhoneContacts size: " + allPhoneContacts.size() + " counter: " + counter + " repeatCounter: " + repeatCounter + " TaskType:" + instance.getTaskType());

        this.instance = instance;

        boolean isCurrGroupANewPersonalCreatedGroup = false;
        //for WA
        if (instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP) {

            //Intention : Extract Contacts from Group by Mouse Hover
            Set<String> phContactsChunk = extractContactsByGroupMouseHover(3000);

            //Intention : Avoid accessing newly created Personal Group
            Predicate<Set<String>> isCurrGroupNotANewPersonalCreatedGroup = phContactsChunks -> !validateBroadcastRiskIfNewPersonalGroupFound(groupNameExceptIndv,
                    phContactsChunks);

            //Intention : Avoid sending broadcast proxy message to created Personal Support Group having total 3 persons or proxy group for me by mediator
            Predicate<Set<String>> isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe = phContactsChunks -> phContactsChunks.size() > SUPPORT_GROUP_SIZE_CREATED_BY_ME_FOR_SUPPORT_OR_BY_MEDIATOR_FOR_PROXY;

            //Intention : Either the flag for successful groups check is not required or if required, then the group list must contain it
            Predicate<String> shouldBroadcastChecksAllSuccessful = groupNameExceptInd -> !instance.isTraverseDependsOnAllSuccessGroupList() || (instance.isTraverseDependsOnAllSuccessGroupList() && allSuccessfulSupportGroupsFromFile.contains(groupNameExceptInd));

            if (isCurrGroupNotANewPersonalCreatedGroup.test(phContactsChunk) && isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe.test(phContactsChunk) && shouldBroadcastChecksAllSuccessful.test(groupNameExceptIndv)) {
                isCurrGroupANewPersonalCreatedGroup = false;
                logger.info("Its a perfect contacts groups {} with contacts size {}", groupNameExceptIndv, phContactsChunk.size());
                allPhoneContacts.addAll(phContactsChunk);
            } else {
                isCurrGroupANewPersonalCreatedGroup = true;
                logger.info("Not a perfect contacts groups, its a personal group {}", groupNameExceptIndv);

            }
            logger.info("allPhoneContacts updated size: in overriden takeActionIfTheGroupIsEligibleToProceed:  " + allPhoneContacts.size() + " isCurrGroup_NOT_ANewPersonalCreatedGroup : " + isCurrGroupNotANewPersonalCreatedGroup.test(phContactsChunk) + " isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe : " + isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe.test(phContactsChunk) + " shouldBroadcastChecksAllSuccessful : " + shouldBroadcastChecksAllSuccessful.test(groupNameExceptIndv) + " TaskType:" + instance.getTaskType());
        }

        if (!isCurrGroupANewPersonalCreatedGroup) {
            try {
                // Navigate to WhatsApp Web and register with health monitor
                webDriverHealthService.updateActivityTime(taskType);
                Thread.sleep(200);
                logger.info("about to broadcast message to group");
                // Broadcast to Group
                broadcastMessageToSupportGroups();
                checkPendingAndErrorStatus(groupNameExceptIndv, counter, 0);
                hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = true;
            } catch (Exception ex) {
                logger.info("Exception in broadcastMessageToSupportGroups : " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }
        Map<ActionIfTheGroupIsEligible, Object> actionMap = Map.ofEntries(Map.entry(ActionIfTheGroupIsEligible.FLAG_TRUE, hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious), Map.entry(ActionIfTheGroupIsEligible.GROUP_NAME, groupNameExceptIndv), Map.entry(ActionIfTheGroupIsEligible.SET_OF_DATA, allPhoneContacts), Map.entry(ActionIfTheGroupIsEligible.COUNTER, counter++), Map.entry(ActionIfTheGroupIsEligible.REPEAT_COUNTER, repeatCounter));
        return actionMap;
    }

    @Override
    public void broadcastMessageToSupportGroups() throws InterruptedException {

        try {
            WebElement webElementTypeMsg = febx(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
            Thread.sleep(200);//NA

            /* Human Like behavior added in WATG following FB*/

            String message = getxPathInterface().getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT();
            String greet = getRandomGreeting();
            message = MessageFormat.format("{0}, {1}", greet, message);
            if (isWarningNoteToAddAsMarketIsDownAndConsultantsAreNotContacting) {
                String warning = getConclusionWarningAtFinishing();
                message = MessageFormat.format("{0}_NL_{1}", message, warning);
            }


            if(wantToAddMillisAtMessageEndToMakeEachMessageUnique) {
                message = message + appendCurrentDateTimeToMessage();
            }
            logger.info("wantToClearIfDraftedBeforeSend : " + wantToClearIfDraftedBeforeSend);

            if(wantToClearIfDraftedBeforeSend) {
                clearTheDraftCreatedByAlertPopup(webElementTypeMsg);
            }
            Thread.sleep(200);
            //webElementTypeMsg.sendKeys(message);
            gradualTypeMessageOnlyLastLine(webElementTypeMsg, message);
            //gradualTypeMessageByWords(webElementTypeMsg, message);

            logger.info("Message is ready to be posted with greet {}", message);
            scrollAndSleepRandomly(getSLEEP_TIME_MS(), getSLEEP_TIME_MS() + 1500L);

            Thread.sleep(getSLEEP_TIME_MS() * 2);

            try{
                pressEnterToSend(webElementTypeMsg);
                logger.info("Message Broadcasted Successfully by simply pressing enter instead of pressing Send button............. TaskType: {}", instance.getTaskType());
                return;
            }catch(Exception e){
                logger.error("Failed to send message by sendKeys ENTER",e);
            }

            boolean isSendButtonAvailableAndClicked = febxsAndClickWithRetry(getxPathInterface().getSPAN_DATA_TESTID_SEND(), "",true,"Clicking Send Button");
            if(!isSendButtonAvailableAndClicked){
                logger.info("Send Button is not available due to private group or PayStar or upcoming popups alerts for taskType :{}", instance.getTaskType());
                pressEnterToSend(webElementTypeMsg);

                logger.error("TaskType :{} Executing OK or CANCEL to avoid private group or PayStar or upcoming popups", instance.getTaskType());
                removePopUpLikePrivateGroupBlockingClick();
                isSendButtonAvailableAndClicked = febxsAndClickWithRetry(getxPathInterface().getSPAN_DATA_TESTID_SEND(), "",true,"Clicking Send Button");
                logger.info("Message Broadcast Succeeded Now? {}.............for TaskType: {}",isSendButtonAvailableAndClicked, instance.getTaskType());
                return;
            }
            Thread.sleep(getSLEEP_TIME_MS());
            logger.info("Message Broadcasted Successfully............. TaskType: {}", instance.getTaskType());
        } catch (Exception e) {
            logger.error("Error during message broadcast :", e);
        }
    }

    private void pressEnterToSend(WebElement webElementTypeMsg) throws InterruptedException {
        logger.info("Clicking Enter With Actions to send the message for taskType :{}", instance.getTaskType());
        Actions action = new Actions(ThreadLocalAutomationContext.getContext().getDriver());
        action.keyDown(Keys.ENTER).perform();
        action.release();
        Thread.sleep(getSLEEP_TIME_MS());
        logger.info("Clicking Enter With sendKeys to send the message for taskType :{}", instance.getTaskType());
        webElementTypeMsg.sendKeys(Keys.ENTER);
        Thread.sleep(getSLEEP_TIME_MS());
    }

    private static void clearTheDraftCreatedByAlertPopup(WebElement webElementTypeMsg) {
        try {
            if(webElementTypeMsg.getText()!=null && !webElementTypeMsg.getText().isEmpty()){
                webElementTypeMsg.click();
                Thread.sleep(200);
                webElementTypeMsg.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                Thread.sleep(200);
                webElementTypeMsg.sendKeys(Keys.BACK_SPACE);
                Thread.sleep(200);
                logger.info("Cleared the message box by CTRL+A and BACKSPACE");
                webElementTypeMsg.clear();
                Thread.sleep(200);
                logger.info("Cleared the message box by clear method");
            }else{
                logger.info("Message box is empty. Lets add our message");
            }

        }catch (Exception e) {
            logger.error("Failed to clear the message box ",e);
            logger.info("trying to clear the message box using keyboard using ctrl + a and backspace");
            try{
                Actions action = new Actions(ThreadLocalAutomationContext.getContext().getDriver());
                action.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
                action.keyDown(Keys.BACK_SPACE).perform();
                action.release();
                Thread.sleep(200);
                logger.info("Cleared the message box by keyboard using ctrl + a and backspace");
            }catch (Exception ex) {
                logger.error("Failed to clear the message box using keyboard using ctrl + a and backspace",ex);
            }
        }
    }

    private void checkPendingAndErrorStatus(String groupName, int counter, int repeatErrorCounter) throws InterruptedException {
        logger.info("checkPendingStatus counter :: {}", counter);
        if (/*counter % 2 == 0 && */(!febxs(getxPathInterface().getMessageDeliveryPendingStatus()).isEmpty() || !febxs(getxPathInterface().getMessageDeliveryErrorStatus()).isEmpty())) {
            logger.error("Message Delivery status is still PENDING/ERROR for group {} for the taskType :{}", groupName, instance.getTaskType());

            Thread.sleep(4000);
            //runActionClassForPreviousChat();
            Thread.sleep(4000);
            //runActionClassForNextChat();
            Thread.sleep(4000);
            if (repeatErrorCounter > 20 && repeatErrorCounter % 10 == 0) {
                // Try to recover by refreshing the page
                logger.info("webDriverHealthMonitor checkSessionHealth {}", webDriverHealthMonitor.checkSessionHealth(taskType));
                webDriverHealthMonitor.recoverSession(taskType);
                Thread.sleep(10000);
            } else {
                checkPendingAndErrorStatus(groupName, counter, repeatErrorCounter + 1);
                Thread.sleep(5000);
            }
        }

    }

    @Override
    public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
        logger.info("getAlreadyTraversedGroupsOnlyAndUpdateDate : ClearUpdateCurrDateAndReloadFile : this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() : " + this.getBroadcastGroupsAlreadyMessageSentForTheDayTxt() + " groupsAlreadyMessageSentForTheDaySetWA: " + this.getBroadcastGroupsAlreadyMessageSentForTheDaySet().size());
        return clearUpdateCurrDateAndReloadFile(this.getBroadcastGroupsAlreadyMessageSentForTheDayTxt(), GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG, this.getBroadcastGroupsAlreadyMessageSentForTheDaySet(), true);
    }


    @Override
    public boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping) {
        logger.info("exportDataToFile : exportAndMergeToExisingSingleFile : this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() : " + fileName + " SUCCESSFUL_GROUP_NAMES: " + setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
        WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(fileName, setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
        return true;

    }

    @Override
    public int getExpectedSizeToModuloToInsertExportToFile() {
        return 1;
    }

    @Override
    public String getFilePathToExportContactsData() {
        return this.getBroadcastGroupsAlreadyMessageSentForTheDayTxt();
    }

    @Override
    protected int getMaxAllowedRepeatOfSameGroup() {
        return 100;
    }

    @Override
    protected int processCompletedSuccessfullyCounter() {
        return 5;
    }

    public boolean clearAlreadyTraversedOrBroadcastedGroupsInExecution(TaskType taskType) {
        return super.clearAlreadyTraversedOrBroadcastedGroupsInExecution(taskType);
    }

    @Override
    public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored, String lastVisitedGroup) {
        searchAndClickOnGroup(lastVisitedGroup);
        hasNewGroupJoinedButNotRestored = false;
        return hasNewGroupJoinedButNotRestored;
    }

    public boolean clearAllAlreadyTraversedInDBFile(TaskType taskType) {
        TaskSocialData taskSocialData = TASK_SOCIAL_DATA.get(taskType);
        if (taskSocialData != null) {
            //Take backup
            WhatsappDBFileRepository.takeBackup(taskSocialData.getVisitedGroupFileName());
            return WhatsappDBFileRepository.clearFileContent(getBroadcastGroupsAlreadyMessageSentForTheDayTxt(taskType));
        }
        return false;
    }

    @Override
    public FlowType getEnumFlowType() {
        return enumFlowType;
    }

    @Override
    public boolean isSuccessfulSupportGroupContainsCheckRequired() {
        return isSuccessfulSupportGroupContainsCheckRequired;
    }
}
