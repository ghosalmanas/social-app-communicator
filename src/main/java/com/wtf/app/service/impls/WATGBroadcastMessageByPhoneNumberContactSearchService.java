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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("watgBroadcastMessageByPhoneNumberContactSearchService")
public class WATGBroadcastMessageByPhoneNumberContactSearchService extends WATGParent implements IBroadcasteAllSupportGroups {

    private static final Logger logger = LogManager.getLogger(WATGBroadcastMessageByPhoneNumberContactSearchService.class);
    private static final int SUPPORT_GROUP_SIZE_CREATED_BY_ME_FOR_SUPPORT_OR_BY_MEDIATOR_FOR_PROXY = 5;

    private final Set<String> alreadyTraversedAndMessageSentGroups = new LinkedHashSet<>();
    private final FlowType enumFlowType = FlowType.BROADCAST_SELF_SKILLSET_AD_TO_GROUPS;
    private boolean isSuccessfulSupportGroupContainsCheckRequired = false;
    private WATGCommonUtils instance;
    private final WhatsappDBFileRepository whatsappDBFileRepository;

    @Autowired
    private SocialModel whatsappDTO;

    @Value("${wantToSendMessageToIndianConsultants}")
    private boolean wantToSendMessageToIndianConsultants;

    @Value("${wantToSendMessageToUSAConsultants}")
    private boolean wantToSendMessageToUSAConsultants;

    @Value("${wantToSendMessageToUSAConsultantsItsSevereAndRisky}")
    private boolean wantToSendMessageToUSAConsultantsItsSevereAndRisky;

    private final String MESSAGE_TO_EACH_CONSULTANT =

            " _NL_ "
                    + "*Hey, how are you ?"
                    + " _NL_ "
                    + "*>*As you know, I am an individual *Fullstack Proxy and Support* Person with 15+ Years of *Expertise IT Industry Experience* "
                    + " _NL_ "
                    + "*>* you can Call/Message me directly (*Direct Contact*) or refer to your friend for any *Fullstack or Backend requirements* on - "
                    + "*INTERVIEW SUPPORT (PROXY Call)*/ Assignment/ *Job Support/ Coding Test*/ Training/ Task"
                    + " _NL_ "
                    + "*>* WhatsApp:- https://wa.me/+9007765487"
                    + " _NL_ "
                    + "*>* I am a *Java Fullstack Technical Lead Developer* expertise in"
                    + " _NL_ "
                    + "*>* JAVA 8,11,17,21, Spring Boot, Spring AI, Microservices, Kafka, Angular, React, Javascript, AWS, Docker, Kubernetes, Redis*, JPA, Data Structures and Algorithms "
                    + " _NL_ "
                    + "*>* Process: - *Prompting & Transcript with Pro Premium Otter* with Pro Premium Screen Control for live coding.";


    @Autowired
    public WATGBroadcastMessageByPhoneNumberContactSearchService(
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            WhatsappDBFileRepository whatsappDBFileRepository) {
        super(initialSetup, parallelWebDriverManager);
        this.whatsappDBFileRepository = whatsappDBFileRepository;
        logger.info("WATGBroadcastMessageToAllGroupsService initialized with  InitialSetup, and WebDriver");
    }

    public void start(AutomationContext context, TaskType taskType) {
        logger.info("Starting broadcast service for {} with context ID SearchContactAndSendMessage : {}", socialType, context.getTaskId());
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

            // isTest is not used in this implementation, but is part of the signature

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

        logger.info("overriden takeActionIfTheGroupIsEligibleToProceed SearchContactAndSendMessage  SearchContactAndSendMessage : " + groupNameExceptIndv + " hasNewGroupJoinedOrMsgSentButNotRestoredToPreviousSearchContactAndSendMessage : " + hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious + " groupNameExceptIndvSearchContactAndSendMessage : " + groupNameExceptIndv + " allPhoneContacts sizeSearchContactAndSendMessage : " + allPhoneContacts.size() + " counterSearchContactAndSendMessage : " + counter + " repeatCounterSearchContactAndSendMessage : " + repeatCounter+ " TaskType:"+ instance.getTaskType());

        logger.info("**************** MUST SCAN and LOGIN USING +919007765487 for KNOWN CONTACTS**************");
        this.instance = instance;
            try {

                Set<String> allPhNumsFromCsvFromEachPersonMessage = null;
                Set<String> errorPhoneNums= new TreeSet<>();

                Set<String> consultantContactsToSendMessageIndividually = new LinkedHashSet<>();//When market is down. it's a Bramhastra
                try {

                    if(wantToSendMessageToIndianConsultants) {
                        Set<String> knownClientConsultantNamesInd = new TreeSet<>(WhatsappDBFileRepository
                                .getAllGroupsFromCsv(Paths.get("dbfiles/master_fixed_files/known_client_consultant_names_IND.txt")));
                        consultantContactsToSendMessageIndividually.addAll(knownClientConsultantNamesInd);
                        logger.info( "knownClientConsultantNamesInd size :" + knownClientConsultantNamesInd.size());
                    }

                    if(wantToSendMessageToUSAConsultants) {
                        Set<String> knownClientConsultantNamesUSA = new TreeSet<>(WhatsappDBFileRepository
                                .getAllGroupsFromCsv(Paths.get("dbfiles/master_fixed_files/known_client_consultant_names_USA.txt")));
                        consultantContactsToSendMessageIndividually.addAll(knownClientConsultantNamesUSA);
                        logger.info("knownClientConsultantNamesUSA size :" + knownClientConsultantNamesUSA.size());
                    }

                    Function<String, String> extractPhNumsFromSplitReplace = i->i.split("_____")[1].replaceAll("\\+", "").replaceAll("-", "").replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "");

                    allPhNumsFromCsvFromEachPersonMessage = consultantContactsToSendMessageIndividually.stream().map(s->extractPhNumsFromSplitReplace.apply(s)).collect(Collectors.toSet());

                    logger.info( "Modified allGroupsFromCsv :: " + allPhNumsFromCsvFromEachPersonMessage);

                    if(wantToSendMessageToUSAConsultantsItsSevereAndRisky) {
                        Set<String> UNKNOWN_client_consultant_names_USA = new TreeSet<>(WhatsappDBFileRepository
                                .getAllGroupsFromCsv(Paths.get("dbfiles/master_fixed_files/Unknown_client_consultant_names_USA.txt")));
                        //allPhNumsFromCsvFromEachPersonMessage.addAll(UNKNOWN_client_consultant_names_USA); unknown should be handled separately replacing 9007765487(login) by 8274848227(login)
                        logger.info("Unknown_client_consultant_names_USA size :" + UNKNOWN_client_consultant_names_USA.size());
                    }


                    Thread.sleep(DURATION_DRIVER_SETUP_AND_LOADING_AFTER_SCANNED);


                    for (String phNum : allPhNumsFromCsvFromEachPersonMessage) {
                        //Search the contact
                        searchAndClickOnLastVisitedGroup(phNum);
                        // Broadcast to consultant searched by PhoneNumber
                        broadcastMessageToSupportGroups();
                        checkPendingStatus(0);
                        // update success status in already message sent file
                        // Export Contacts to a file
                    }

                    hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (Exception ex) {
                logger.info("Exception in broadcastMessageToSupportGroups SearchContactAndSendMessage  SearchContactAndSendMessage : " + ex.getLocalizedMessage());
                ex.printStackTrace();
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
            message = greet + ", " + message;

            //webElementTypeMsg.sendKeys(message);
            gradualTypeMessageOnlyLastLine(webElementTypeMsg, message);
            //gradualTypeMessageByWords(webElementTypeMsg, message);

            logger.info("Message is ready to be posted with greet" + message);
            scrollAndSleepRandomly(2000, 3500);

            Thread.sleep(2000);
            febx(getxPathInterface().getSPAN_DATA_TESTID_SEND()).click();
            Thread.sleep(2000);
            logger.info("Message Broadcasted Successfully............."+ " TaskType SearchContactAndSendMessage :"+ instance.getTaskType());
        } catch (Exception e) {
            logger.error("Error during message broadcast  SearchContactAndSendMessage :", e);
        }
    }

    private void checkPendingStatus(int counter) throws InterruptedException {
        logger.info("checkPendingStatus counter  SearchContactAndSendMessage :: {}",counter);
        if (counter % 2 == 0 && !febxs(getxPathInterface().getMessageDeliveryPendingStatus()).isEmpty()){
            logger.info("Message Delivery status is still PENDING for the taskType  SearchContactAndSendMessage :"+ instance.getTaskType());

            this.wait(4000);
            runActionClassForPreviousChat();
            this.wait(4000);
            runActionClassForNextChat();
            this.wait(4000);
            checkPendingStatus(counter);
        }
    }

    @Override
    public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
        logger.info("getAlreadyTraversedGroupsOnlyAndUpdateDate  SearchContactAndSendMessage : ClearUpdateCurrDateAndReloadFile : this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() SearchContactAndSendMessage : " + this.getBroadcastGroupsAlreadyMessageSentForTheDayTxt() + " groupsAlreadyMessageSentForTheDaySetWASearchContactAndSendMessage : " + this.getBroadcastGroupsAlreadyMessageSentForTheDaySet().size());
        return clearUpdateCurrDateAndReloadFile(this.getBroadcastGroupsAlreadyMessageSentForTheDayTxt(), GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG, this.getBroadcastGroupsAlreadyMessageSentForTheDaySet(), true);
    }


    @Override
    public boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping) {
        logger.info("exportDataToFile  SearchContactAndSendMessage : exportAndMergeToExisingSingleFile : this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() SearchContactAndSendMessage : " + fileName + " SUCCESSFUL_GROUP_NAMESSearchContactAndSendMessage : " + setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
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

    void broadcastToGroups(String groupNameExceptIndv, String BROADCAST_MESSAGE_TO_SEND) {

        if (allSuccessfulSupportGroupsFromFile.contains(groupNameExceptIndv)) {

            //Broadcast to Group
            WebElement webElementTypeMsg = febx(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
            webElementTypeMsg.sendKeys(BROADCAST_MESSAGE_TO_SEND);
            febx(getxPathInterface().getSPAN_DATA_TESTID_SEND()).click();
        }
    }



    /* ========== WA_MsgSenderByPhoneNumber Start =================*/

    private static final boolean isSpecialCharacterSplitRequired = false;
    private XPathInterfaceWATG xPathInterfaceWatg;


    public void updateContactsOrBroadcastMessages(String FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT,
                                                  Boolean doWeReallyNeedToSendMessageToEveryGroup, String BROADCAST_MESSAGE_TO_SEND)
            throws InterruptedException, IOException {


        Set<String> allPhNumsFromCsv = null;
        Set<String> errorPhoneNums= new TreeSet<>();
        Set<String> allPhoneContacts = new TreeSet<>();

        try {
            Set<String> allGroupsFromCsvSet = new TreeSet<>(WhatsappDBFileRepository
                    .getAllGroupsFromCsv(Paths.get(FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT)));
            // logger.info( "allGroupsFromCsv :: "+allGroupsFromCsvSet);

            logger.info( "group_names size :" + allGroupsFromCsvSet.size());

            Function<String, String> phNumsSplitReplace = i->i.split("_____")[1].replaceAll("\\+", "").replaceAll("-", "").replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "");

            allPhNumsFromCsv = allGroupsFromCsvSet.stream().map(s->phNumsSplitReplace.apply(s)).collect(Collectors.toSet());

            logger.info( "Modified allGroupsFromCsv :: " + allPhNumsFromCsv);

            Thread.sleep(DURATION_DRIVER_SETUP_AND_LOADING_AFTER_SCANNED);

            // search, click, and process the contacts From Groups
            searchClickAndExtractContactsFromGroups(allPhNumsFromCsv, errorPhoneNums,
                    GROUP_EXECUTION_INTERVAL_MS, BROADCAST_MESSAGE_TO_SEND);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (/*yesReporocessErrorList*/ false) {
            logger.info( "Reprocessing error list" + errorPhoneNums);

            // Re-process and retry the error group
            Set<String> errorGroupToReProcess = new HashSet<>(errorPhoneNums);
            searchClickAndExtractContactsFromGroups(errorGroupToReProcess, errorPhoneNums, _ERROR_GROUP_PROCESS_SLEEP_TIME, BROADCAST_MESSAGE_TO_SEND);
        } else {
            logger.info( "Skipping error list from reprocessing " + errorPhoneNums);

        }

        logger.info( "ErrorPhoneNum\n" + errorPhoneNums);
        logger.info( "allPhoneContacts \n" + allPhoneContacts);
        Set<String> successfulGroups = new HashSet<>();
        successfulGroups.addAll(allPhNumsFromCsv);

        successfulGroups.removeAll(errorPhoneNums);
        if (!doWeReallyNeedToSendMessageToEveryGroup) {
            // 4. Export Contacts to a file
            WhatsappDBFileRepository.exportToFile(getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS(), allPhoneContacts, successfulGroups,
                    errorPhoneNums);
        }

    }


    private void searchClickAndExtractContactsFromGroups(Set<String> allGroupsFromCsv, Set<String> errorPhoneNums,
                                                         int SLEEP_TIME,String BROADCAST_MESSAGE_TO_SEND) {

        logger.info( "allGroupsFromCsv.size() Before Processing :: " + allGroupsFromCsv.size());
        logger.info( "errorGroup" + errorPhoneNums+ " Before Processing with sleep time: " + SLEEP_TIME);

        Collections.synchronizedSet(allGroupsFromCsv);

        int[] counter = { 0 };
        errorPhoneNums.clear();

        try {
            logger.info( "allGroupsFromCsv :: " + allGroupsFromCsv);
            allGroupsFromCsv.stream().forEach(eachGroupString -> {
                sendMessageOrExtractContactsFromEachGroup(errorPhoneNums, allGroupsFromCsv,SLEEP_TIME, counter, eachGroupString,BROADCAST_MESSAGE_TO_SEND);

            });
        } catch (Exception e) {
            logger.info( "error external phoneNum: ");
            e.printStackTrace();
        }
    }

    private void sendMessageOrExtractContactsFromEachGroup(Set<String> errorGroup, Set<String> allGroupsFromCsv,int SLEEP_TIME, int[] counter,
                                                           String eachPhNumString,String BROADCAST_MESSAGE_TO_SEND) {
        counter[0] = counter[0] + 1;
        logger.info( "*******************phoneNumprocssesing " + counter[0] + " Out of total PhoneNums : " + allGroupsFromCsv.size()+" ****************");

        WebElement webElement=null;
        WebDriver driver=null;
        try {
            driver.findElement(By.xpath(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH())).click();
            webElement = driver.findElement(By.xpath(getxPathInterface().getXPATH_SEARCH()));
            logger.info( "LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH : " +eachPhNumString);

            try {// 1.Search groupName in SearchBox

                webElement.sendKeys(eachPhNumString + "\n");
                Thread.sleep(SLEEP_TIME);

                webElement.sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER);
                // Send a Message
                logger.info( "webElementPhoneNumsCheck driver : " +eachPhNumString);

                WebElement webElementPhoneNumsCheck = driver
                        .findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
                if (webElementPhoneNumsCheck!=null) {
                    logger.info( "webElementPhoneNumsCheck.get : " +eachPhNumString);

                    WebElement webElementTypeMsg = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
                    webElementTypeMsg.sendKeys(BROADCAST_MESSAGE_TO_SEND);
                    driver.findElement(By.xpath(getxPathInterface().getSPAN_DATA_TESTID_SEND())).click();
                    Thread.sleep(SLEEP_TIME/2);
                }

                Thread.sleep(SLEEP_TIME);

            } catch (Exception e) {
                logger.error("exception out of allGroupsFromCsv loop : " + e.getMessage());
                errorGroup.add(eachPhNumString);
            }
            webElement.clear();
        } catch (Exception e) {
            try {
                Thread.sleep(SLEEP_TIME);
                logger.info("\n\n\n");
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();}
    }

    public XPathInterfaceWATG getxPathInterface() {
        return xPathInterfaceWatg;
    }


    public void setxPathInterface(XPathInterfaceWATG xPathInterface) {
        this.xPathInterfaceWatg=xPathInterface;
    }

/* ========== WA_MsgSenderByPhoneNumber end=================*/


    @Override
    public FlowType getEnumFlowType() {
        return enumFlowType;
    }

    @Override
    public boolean isSuccessfulSupportGroupContainsCheckRequired() {
        return isSuccessfulSupportGroupContainsCheckRequired;
    }
}
