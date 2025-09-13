package com.wtf.app.service.impls;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.FacebookBroadcastParent;
import com.wtf.app.parent.FacebookCommonUtils;
import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.enums.FlowType;
import com.wtf.app.model.xpaths.Facebook_Xpaths;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.wtf.app.model.xpaths.XPathInterfaceFB;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.wtf.app.repository.WhatsappDBFileRepository;

@Service("facebookBroadcastSelfSkillSetService")
@Lazy
public class FacebookBroadcastSelfSkillSetService extends FacebookBroadcastParent {

    private static final Logger logger = LogManager.getLogger(FacebookBroadcastSelfSkillSetService.class);

    private volatile boolean shouldStopFacebookThread = false;

    @Value("${wantToAddMillisAtMessageEndToMakeEachMessageUnique:false}")
    private boolean wantToAddMillisAtMessageEndToMakeEachMessageUnique;

    @Autowired
    public FacebookBroadcastSelfSkillSetService(XPathInterfaceFB xPathInterfaceFB,
                                              @Lazy 
                                              InitialSetup initialSetup,
                                              ParallelWebDriverManager parallelWebDriverManager) {
        super(xPathInterfaceFB,  initialSetup, parallelWebDriverManager);
        logger.info("FacebookBroadcastSelfSkillSetService initialized with  InitialSetup, and ParallelWebDriverManager");
    }
    
    @Override
    public void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList, String message,
                                                 int groupListSize, String groupId, boolean annonymous) throws InterruptedException {

        try {


            driver.get(instance.getxPathInterface().getGroupsLink().concat(groupId));
            Thread.sleep(10000);

            clickOnWriteSomethingTextBox();

            scrollAndSleepRandomly(5000, 5000);

            WebElement postBox = clickPostBoxToWriteFB(groupId);
            if (postBox == null) return;

            if(wantToAddMillisAtMessageEndToMakeEachMessageUnique) {
                message = message + appendCurrentDateTimeToMessage();
            }

            boolean isMessagePretendingHumanTypingSuccess = writeMessageAfterPretendHumanTyping(message, groupId, postBox);

            if (!isMessagePretendingHumanTypingSuccess) {
                logger.error("Pretend human typing failed for groupId {}, so sending directly : ", groupId);
            }
            postTheMessageToSubmitInFB();

            scrollAndSleepRandomly(10000, 7000);

            if (isPendingPostsPresent(groupId)) {
                logger.info(MSG_POST_STAGES_LOG._4_MSG_POST_PENDING + " : " + "Ohh! Still Pending Post created after Posting the new Message for groupId : " + groupId + " index:"
                        + groupIdList.indexOf(groupId) + " out of " + groupListSize);
            } else {
                logger.info(MSG_POST_STAGES_LOG._2_MSG_POSTED_SUCCESSFULLY + " : " + "**************************Message is posted successfully for groupId : " + groupId + " index:"
                        + groupIdList.indexOf(groupId) + " out of " + groupListSize+" ***************************");
            }

            scrollAndSleepRandomly(5000, 4000);

            TerminateIfMessagesAreNotPostingAtAll(groupId);

            WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));
            Thread.sleep(4000);
            retryCounter = 0;

        } catch (NoSuchElementException nse) {
            nse.printStackTrace();
            if (retryCounter == 0) {
                retryCounter = retryCounter + 1;
                //Retry only once
                logger.info("Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
                sendFacebookBroadcastToEachGroup(instance, groupIdList, message, groupListSize, groupId, annonymous);
            }
        } catch (Exception e) {
            logger.info(MSG_POST_STAGES_LOG._3_MSG_POST_FAILED + " : " + "Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
            e.printStackTrace();
            retryCounter = 0;
        }
    }

    void TerminateIfMessagesAreNotPostingAtAll(String groupId) throws InterruptedException {
        boolean isPendingPostsPresent = isPendingPostsPresent(groupId);

        if (!msgPostedStatuses.contains(MSG_POST_STATUS.SUCCESS)) {
            if (isPendingPostsPresent) {
                msgPostedStatuses.add(MSG_POST_STATUS.FAILED);
                groupIdsFailed.add(groupId);
            } else {
                msgPostedStatuses.add(MSG_POST_STATUS.SUCCESS);
            }

            if (msgPostedStatuses.size() == CONSECUTIVE_PENDING_TERMINATION_COUNTER) {
                deleteAllPendingPosts(groupIdsFailed);
                logger.error( MSG_POST_STATUS.ONLY_DUMPS_PENDING_NEVER_POSTED.toString());
                logger.error("Continuous Failure with pending messages {} so interrupting the thread", CONSECUTIVE_PENDING_TERMINATION_COUNTER);
                //Kill the facebook thread
                shouldStopFacebookThread = true;
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public FlowType getEnumFlowType() {
        return FlowType.BROADCAST_SELF_SKILLSET_AD_TO_GROUPS;
    }

    @Override
    public Collection<String> getBroadcastGroupIdNameFBSet_() {
        return broadcastGroupIdNameFBSet;
    }

    @Override
    public String getMessageToBroadcast() {
        return new Facebook_Xpaths().getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT();
    }

    @Override
    protected Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
        // TODO Auto-generated method stub
        return clearUpdateCurrDateAndReloadFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB,
                GROUP_STRING_TRAVERSE_ALREADY_DATE_FB, groupsAlreadyMessageSentForTheDaySetFB, true);
    }

    public boolean isAnnonymous() {
        return false;
    }
    
    /**
     * Traverses through Facebook groups and performs the required actions.
     * This method is called from the starter class to initiate the group traversal process.
     * 
     * @throws InterruptedException if the thread is interrupted
     * @throws AWTException if there's an AWT exception
     * @throws IOException if there's an I/O error
     */
    public void traverseGroups() throws InterruptedException, AWTException, IOException {
        logger.info("Starting to traverse Facebook groups for self-skills broadcast");
        
        try {
            // Call the parent class's traverseGroupsTemplate method with this instance
            // This will trigger the Facebook group traversal and posting process
            traverseGroupsTemplate(this);
            
            logger.info("Successfully completed Facebook groups traversal");
        } catch (Exception e) {
            String errorMsg = "Error during Facebook groups traversal: " + e.getMessage();
            logger.error(errorMsg, e);
            throw e;
        }
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
                this.setSocialModel(socialModel);
                this.setxPathInterface(socialModel.getxPathInterface());
                this.setBaseUrl(socialModel.getBaseURL());
                this.setTaskTypeMapping(taskType, this);

                // Your existing logic here
                traverseGroupsTemplate(this);
            } catch (Exception e) {
                logger.error("Error during WATG fetch all contacts and group names service execution", e);
            }
        });
    }

    @Override
    public Set<String> getBroadcastGroupIdNameFBSet() {
        return broadcastGroupIdNameFBSet;
    }

}
