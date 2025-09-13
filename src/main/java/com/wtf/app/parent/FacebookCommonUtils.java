package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.FlowType;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.model.xpaths.XPathInterfaceFB;
import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.service.impls.FacebookBroadcastSelfSkillSetService;
import com.wtf.app.service.impls.FacebookBroadcastToSensitiveGroupAnnonymService;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
public abstract class FacebookCommonUtils extends SocialParentCommonUtils {

    private static final Logger logger = LogManager.getLogger(FacebookCommonUtils.class);
    private static final String STRING_SEPARATOR = "__";

    protected XPathInterfaceFB xPathInterfaceFB;

    private boolean traverseDependsOnAllSuccessGroupList;
    private boolean annonymous;
    protected int retryCounter = 0;
    protected List<MSG_POST_STATUS> msgPostedStatuses = new ArrayList<>();
    protected List<String> groupIdsFailed = new ArrayList<>();


    @Value("${onlyAllowAllBroadcastsWATGFB:false}")
    private boolean onlyAllowAllBroadcastsWATGFB;

    @Value("${facebook.group.broadcast.enabled:false}")
    private boolean facebookBroadcastEnabled;

    @Value("${facebook.group.broadcast.sensitive.enabled:false}")
    private boolean facebookSensitiveBroadcastEnabled;

    @Autowired
    @Lazy
    FacebookBroadcastToSensitiveGroupAnnonymService facebookBroadcastToSensitiveGroupAnnonymService;

    @Autowired
    @Lazy
    FacebookBroadcastSelfSkillSetService facebookBroadcastSelfSkillSetService;

    @Autowired
    protected FacebookCommonUtils(XPathInterfaceFB xPathInterfaceFB,
                                  InitialSetup initialSetup,
                                  ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup, parallelWebDriverManager);
        this.xPathInterfaceFB = xPathInterfaceFB;

        logger.info("FacebookCommonUtils initialized with  InitialSetup, and ParallelWebDriverManager");
    }

    protected enum MSG_POST_STATUS {
        SUCCESS, FAILED, ONLY_DUMPS_PENDING_NEVER_POSTED
    }

    ;

    public enum MSG_POST_STAGES_LOG {
        _1_MSG_READY_TO_POST, _2_MSG_POSTED_SUCCESSFULLY, _3_MSG_POST_FAILED, _4_MSG_POST_PENDING, _5_MSG_POSTED_PRIVATE_GROUP, _5_MSG_POSTING_PUBLIC_GROUP, _5_MSG_POSTING_GENERIC_GROUP, _5_MSG_POSTING_WRONG_GROUP
    }

    ;
    protected final int CONSECUTIVE_PENDING_TERMINATION_COUNTER = 20;
    protected boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast2DaysWA = getGroupsAlreadyMessageSentForTheDaySetFB()
            .contains("group_traverse_already_date_" + LocalDate.now())
            || getGroupsAlreadyMessageSentForTheDaySetFB()
            .contains("group_traverse_already_date_" + LocalDate.now().minusDays(1l));

    protected abstract Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate();


    // Initialization is now handled in the constructor
    // No need for @PostConstruct as we're using constructor injection

    public void runFacebookAction(FacebookCommonUtils instanceFB) throws InterruptedException {


        // Login Facebook
        boolean loginFacebookWebSuccess = loginFacebookWeb();
        /*if (!loginFacebookWebSuccess) {
            return;
        }*/

        logger.info("Logging into Facebook..." + instanceFB.getEnumFlowType().name());

        List<FacebookCommonUtils> facebookCommonUtilsList = new LinkedList<>();
        facebookCommonUtilsList.add(instanceFB);

        if (onlyAllowAllBroadcastsWATGFB || (facebookBroadcastEnabled && facebookSensitiveBroadcastEnabled)) {
            //utilize the same login and avoid duplicate login, instead append the other instance to the list
            if (instanceFB instanceof FacebookBroadcastSelfSkillSetService) {

                updateTaskSpecificFields(facebookBroadcastToSensitiveGroupAnnonymService, TaskType.FACEBOOK_BROADCAST_SENSITIVE_AD);
                facebookCommonUtilsList.add(facebookBroadcastToSensitiveGroupAnnonymService);
                logger.info("FacebookBroadcastToSensitiveGroupAnnonymService added to facebookCommonUtilsList");

            } else if (instanceFB instanceof FacebookBroadcastToSensitiveGroupAnnonymService) {
                facebookCommonUtilsList.add(facebookBroadcastSelfSkillSetService);
                logger.info("FacebookBroadcastSelfSkillSetService added to facebookCommonUtilsList");
            }
        }

        logger.info("facebookCommonUtilsList size {}", facebookCommonUtilsList.size());


        for (FacebookCommonUtils instance : facebookCommonUtilsList) {

            boolean success = executeWithContext(() -> {

                if(Thread.currentThread().isInterrupted()){
                    logger.error("FacebookCommonUtils was already interrupted");
                    return false;
                }

                logger.info("runFacebookAction : TaskType {}, flowType {}", instance.getTaskType(), instance.getEnumFlowType());

                //AutomationContext context = ThreadLocalAutomationContext.getContext();
                boolean wantToClearAllPendingPostsStaggedInEachGroup = false;//use it a separate initiator class

                // Set up Facebook groups to post, you must be a member of the group

                Result result = extractFBTaskInformation(instance);

                logger.info("The Message to be posted for All the FB Groups is...." + result.message());

                // # Set up paths of images to post
                // List images_list = {};


                Thread.sleep(20);

                //driver.findElement(By.xpath("//span[text()='Try Another Way']")).click();// anotherWayOTP

                Thread.sleep(90000);

                //delete All Pending Posts from each group


                if (wantToClearAllPendingPostsStaggedInEachGroup) {
                    deleteAllPendingPosts(result.groupIdList());
                }


                //waitIdentifyAndClickOnThePinnedElement("//div[@aria-label='Your profile']");

                Set<String> alreadyTraversedGroupsOnly = instance.getAlreadyTraversedGroupsOnlyAndUpdateDate();
                Set<String> pendingPostPiledGroupsFB = instance.getPendingPostPiledGroupsFB_set_();
                result.groupIdList().removeAll(alreadyTraversedGroupsOnly);
                result.groupIdList().removeAll(pendingPostPiledGroupsFB);

                int groupListSize = result.groupIdList().size();
                logger.info("groupListSize {}", groupListSize);


                // # Post adv skills into each group

                for (String groupId : result.groupIdList()) {
                    if (alreadyTraversedGroupsOnly.contains(groupId)) {//Should not be invoked anymore
                        logger.info("Ohh! Message Already sent to the Group today/yesterday...."
                                + xPathInterfaceFB.getGroupsLink().concat(groupId));
                        continue;
                    }

                    logger.info("FB Group Path Invoked by groupId....\n" + xPathInterfaceFB.getGroupsLink().concat(groupId) + "\n Group Name: " + result.groupIdNamesMapping().get(groupId));

                    try {
                        /* Uncomment if want to take any action seeing pending posts*/
                /*boolean isPendingPostsArePresent = verifyIfPendingPostAlpreadyPresent(groupId);
                if(isPendingPostsArePresent) {
                    continue;
                }*/
                        instance.sendFacebookBroadcastToEachGroup(instance, result.groupIdList(), result.message(), groupListSize, groupId, instance.isAnnonymous());

                    } catch (Exception ex) {
                        logger.error("Exception : " + ex.getMessage());
                    }
                }

                logger.info("Message is posted Completed Successfully in all the groups : " + groupListSize);
                return true;
            });
        }
        driver.close();

    }

    private void updateTaskSpecificFields(FacebookCommonUtils instance, TaskType taskType) {
        SocialType socialType = taskType.getSocialType();
        SocialModel socialModel = initialSetup.getSocialTypeToSocialModel().get(socialType);
        instance.setSocialModel(socialModel);
        instance.setxPathInterface(socialModel.getxPathInterface());
        instance.setBaseUrl(socialModel.getBaseURL());
        instance.setTaskTypeMapping(taskType, this);
    }

    private static Result extractFBTaskInformation(FacebookCommonUtils instance) {
        Map<String, String> groupIdNamesMapping = getGroupIdNamesMapping(instance);
        List<String> groupIdList = groupIdNamesMapping.keySet().stream().sorted().collect(Collectors.toList());

        //Function.identity() is same as String::new

        // Set up text content to post
        String message = instance.getMessageToBroadcast();
        Result result = new Result(groupIdNamesMapping, groupIdList, message);
        return result;
    }

    private record Result(Map<String, String> groupIdNamesMapping, List<String> groupIdList, String message) {
    }

    private static Map<String, String> getGroupIdNamesMapping(FacebookCommonUtils instance) {
        return instance.getBroadcastGroupIdNameFBSet().stream().filter(i -> i != null).peek(i -> System.out.println(i)).collect(Collectors.toMap(i -> i.split(STRING_SEPARATOR)[0], i -> i.split(STRING_SEPARATOR)[1]));
    }

    private boolean loginFacebookWeb() throws InterruptedException {

        try {
            WebElement emailElement = getDriver().findElement(By.xpath("//*[@id='email']"));
            emailElement.sendKeys(xPathInterfaceFB.getEmail());
            Thread.sleep(2);
            WebElement passElement = driver.findElement(By.xpath("//*[@id='pass']"));
            passElement.sendKeys(xPathInterfaceFB.getPassword());
            Thread.sleep(2);

            WebElement loginElement = driver.findElement(By.xpath("//button[@name='login' and @type='submit']"));
            loginElement.click();

            Thread.sleep(5000);
        } catch (Exception e) {
            logger.error("Exception occurred while login into facebook", e);
            return false;
        }
        return true;
    }

    /* Not in Use Directly*/
    boolean verifyIfPendingPostAlpreadyPresent(String groupId) {
        int pendingPostsNumber = getPendingPostsNumber();
        logger.info("Posts are Already Pending with " + pendingPostsNumber + ". Message can't be posted in the group id :" + groupId);
        WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(GROUPS_PENDING_POSTS_MESSAGE_PILED_UP_FB, Set.of(groupId));
        WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));
        return true;
    }

    int getPendingPostsNumber() {
        List<WebElement> pendingPostAlready = driver.findElements(By.xpath("//span[text()='Pending post' or text()='Pending content']"));
        if (!pendingPostAlready.isEmpty()) {
            WebElement pendingCountText = driver.findElement(By.xpath("//span[text()='Pending post' or text()='Pending content']//following-sibling::div//span"));
            //else if(!driver.findElements(By.xpath("//span[text()='You're at the limit for pending content in this group.']")).isEmpty())
            String text = pendingCountText.getText();
            return text.isEmpty() ? 0 : Integer.valueOf(text.substring(0, text.indexOf(" ")));
        }
        return 0;
    }


    public void deleteAllPendingPosts(List<String> groupIdList) throws InterruptedException {
        for (String groupId : groupIdList) {

            try {
                deletePendingPostsByGroupId(groupId);

                logger.info("Pending Messages are deleted for groupId : " + groupId + " index:"
                        + groupIdList.indexOf(groupId) + " out of " + groupIdList.size());

            } catch (Exception ex) {
                logger.error("Sorry! Pending Messages are not deleted for groupId : " + groupId + " index:"
                        + groupIdList.indexOf(groupId) + " out of " + groupIdList.size());
            }
        }

    }

    void deletePendingPostsByGroupId(String groupId) throws InterruptedException {
        boolean isPendingPostsPresent = isPendingPostsPresent(groupId);
        if (!isPendingPostsPresent) {
            logger.info("Now All Pending Messages are deleted for groupId : " + groupId);
            return;
        }

        List<WebElement> pendingDeleteBtns = driver.findElements(By.xpath("//div[@aria-label='Delete' and @role='button' and @tabindex='0']"));

        for (WebElement delete : pendingDeleteBtns) {
            delete.click();
            Thread.sleep((long) (Math.random() * 2000 + 1000));
            driver.findElement(By.xpath("//div[@aria-label='Cancel']//parent::div//preceding-sibling::div//child::div[@aria-label='Delete']")).click();
            Thread.sleep((long) (Math.random() * 2000 + 3000));
        }
        ;
        deletePendingPostsByGroupId(groupId);
    }

    protected boolean isPendingPostsPresent(String groupId) throws InterruptedException {
        driver.get(xPathInterfaceFB.getGroupsLink().concat(groupId) + "/my_pending_content");
        /* above one is a shortcut ready-made solution.Ebable when the above is retired*/
        //checkPendingPostsAndClickOnMagePosts(groupId);

        Thread.sleep((long) (Math.random() * 2000 + 1000));

        List<WebElement> pendingDeletes = driver.findElements(By.xpath("//span[text()='Delete']"));
        if (pendingDeletes.isEmpty()) {
            logger.info("Good News!! No Pending Posts are there to delete for groupId :" + groupId);
            return false;
        }
        logger.info("Posts are Already Pending with count: " + pendingDeletes.size() + " group id :" + groupId);
        return true;
    }

    /* Currently not in use, required if pending post url does not work*/
    void checkPendingPostsAndClickOnMagePosts(String groupId) throws InterruptedException {
        driver.get(xPathInterfaceFB.getGroupsLink().concat(groupId));
        logger.info("Group Path Invoked....\n" + xPathInterfaceFB.getGroupsLink().concat(groupId));
        Thread.sleep(10000);
        int pendingPostsCount = getPendingPostsNumber();
        if (pendingPostsCount == 0) {
            logger.info("Good News! No Pending Posts are there to delete for groupId :" + groupId);
            return;
        }
        Thread.sleep((long) (Math.random() * 3000 + 4000));
        driver.findElement(By.xpath("//span[text()='Manage posts']")).click();// we can use direct delete link as below
    }


    protected void clickOnWriteSomethingTextBox() {
        driver.findElement(By.xpath("//span[text()='Write something...']")).click();
    }

    protected WebElement toggleAnonymous() throws InterruptedException {
        WebElement postBox;
        driver.findElement(By.xpath("//span[text()='Write something...']")).click();

        Thread.sleep(2000);
        driver.findElement(By.xpath("//input[@role='switch' and @aria-label='Anonymous post toggle']")).click();// Annonymously
        Thread.sleep(2000);
        try {
            driver.findElement(By.xpath("//div[@aria-label='Got it']")).click();
        } catch (Exception ex) {
            logger.info("Annonymous Got it not there or Not required");
        }

        Thread.sleep(4000);
        postBox = driver.findElement(By.xpath("//div[contains(@aria-label,'Submit an anonymous post')]"));
        return postBox;
    }

    protected WebElement clickPostBoxToWriteFB(String groupId) {
        WebElement postBox = null;
        try {
            List<WebElement> postBoxes = driver.findElements(By.xpath("//*[contains(@aria-placeholder,'Create a public post')]"));
            if (!postBoxes.isEmpty()) {
                postBox = postBoxes.get(0);
                logger.info(MSG_POST_STAGES_LOG._5_MSG_POSTING_PUBLIC_GROUP + " : " + "Its a Public Group to be posted with the group id :" + groupId);
            } else {
                if (!driver.findElements(By.xpath("//div[text()='Private']")).isEmpty()) {
                    logger.info(MSG_POST_STAGES_LOG._5_MSG_POSTED_PRIVATE_GROUP + " : " + "Its a Private Group, skipping to be posted with the group id :" + groupId);
                    return null;
                }
                postBoxes = driver.findElements(By.xpath("//span[contains(text(),'Write something...')]"));

                if (!postBoxes.isEmpty()) {
                    postBox = postBoxes.get(0);
                    logger.info(MSG_POST_STAGES_LOG._5_MSG_POSTING_GENERIC_GROUP + " : " + "Its a GENERIC Group to be posted with the group id :" + groupId);
                }
            }

            if(postBox == null) {
                logger.info(MSG_POST_STAGES_LOG._5_MSG_POSTING_WRONG_GROUP + " : " + "SOMETHING IS WRONG, SKIPPING with the group id :" + groupId);
                return null;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
        return postBox;
    }

    protected void submitAnonymouslyInFB() {
        driver.findElement(By.xpath("//span[text()='Submit']//parent::span//parent::div//parent::div//parent::div//parent::div")).click();
    }

    protected void postTheMessageToSubmitInFB() {
        driver.findElement(By.xpath("//span[text()='Post']//parent::span//parent::div//parent::div//parent::div//parent::div")).click();
    }


    public Set<String> clearUpdateCurrDateAndReloadFile(String fileNameWithExtension, String messageFormatInsideFile, Set<String> groupsAlreadyMessageSentForTheDaySet, boolean clearExistingContent) {
        if (groupsAlreadyMessageSentForTheDaySet.contains(messageFormatInsideFile + LocalDate.now())
                || groupsAlreadyMessageSentForTheDaySet.contains(messageFormatInsideFile + LocalDate.now().minusDays(1l))/*|| groupsAlreadyMessageSentForTheDaySet
                                .contains(messageFormatInsideFile + LocalDate.now().minusDays(2l))*/) {
            logger.info("ClearUpdateCurrDateAndReloadFile : Message Already Posted within 2 Days : " + groupsAlreadyMessageSentForTheDaySet + " fileNameWithExtension: " + fileNameWithExtension);
            return groupsAlreadyMessageSentForTheDaySet;
        } else {
            if (clearExistingContent) {
                logger.info("ClearUpdateCurrDateAndReloadFile : clearExistingContent is true means message Posted long back.So clearning the file : clearExistingContent : " + clearExistingContent + " fileNameWithExtension: " + fileNameWithExtension);
                WhatsappDBFileRepository.clearContentInFile(fileNameWithExtension);
            }
            logger.info("ClearUpdateCurrDateAndReloadFile : exportAndMergeToExisingSingleFile : " + clearExistingContent + " fileNameWithExtension: " + messageFormatInsideFile + LocalDate.now());
            WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(fileNameWithExtension,
                    Set.of(messageFormatInsideFile + LocalDate.now()));
        }
        groupsAlreadyMessageSentForTheDaySet = extractListOfDataFromFile(fileNameWithExtension);
        return groupsAlreadyMessageSentForTheDaySet;
    }


    public boolean isTraverseDependsOnAllSuccessGroupList() {
        return traverseDependsOnAllSuccessGroupList;
    }

    public void setTraverseDependsOnAllSuccessGroupList(boolean traverseDependsOnAllSuccessGroupList) {
        this.traverseDependsOnAllSuccessGroupList = traverseDependsOnAllSuccessGroupList;
    }


    public XPathInterfaceFB getxPathInterface() {
        return xPathInterfaceFB;
    }

    public void setxPathInterface(XPathInterfaceFB xPathInterface) {
        this.xPathInterfaceFB = xPathInterface;
    }

    public abstract Collection<String> getBroadcastGroupIdNameFBSet_();

    public Set<String> getPendingPostPiledGroupsFB_set_() {
        return pendingPostPiledGroupsFB_set;
    }

    public abstract void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList,
                                                          String message, int groupListSize, String groupId, boolean annonymous) throws InterruptedException;

    public abstract String getMessageToBroadcast();

    public abstract FlowType getEnumFlowType();

    public abstract void traverseGroupsTemplate(FacebookCommonUtils instance)
            throws InterruptedException, AWTException, IOException;

    public abstract boolean isAnnonymous();

}