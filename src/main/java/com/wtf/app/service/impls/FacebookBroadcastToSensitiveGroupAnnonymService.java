package com.wtf.app.service.impls;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.enums.FlowType;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.model.xpaths.Facebook_Xpaths;
import com.wtf.app.model.xpaths.XPathInterfaceFB;
import com.wtf.app.parent.FacebookBroadcastParent;
import com.wtf.app.parent.FacebookCommonUtils;
import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.service.interfaces.IFacebookPluggable;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service("facebookBroadcastToSensitiveGroupAnnonymService")
@Lazy
public class FacebookBroadcastToSensitiveGroupAnnonymService extends FacebookBroadcastParent
        implements IFacebookPluggable {

    private static final Logger logger = LogManager.getLogger(FacebookBroadcastToSensitiveGroupAnnonymService.class);

    @Autowired
    public FacebookBroadcastToSensitiveGroupAnnonymService(XPathInterfaceFB xPathInterfaceFB,

                                                           InitialSetup initialSetup,
                                                           ParallelWebDriverManager parallelWebDriverManager) {
        super(xPathInterfaceFB, initialSetup, parallelWebDriverManager);
        logger.info("FacebookBroadcastToSensitiveGroupAnnonymService initialized with  InitialSetup, and ParallelWebDriverManager");
    }


    @Override
    public void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList, String message,
                                                 int groupListSize, String groupId, boolean doYouWantToPostAnnonymously) throws InterruptedException {
        try {
            logger.info("Starting sensitive broadcast.................................");

            if (driver == null) {
                logger.info("driver is null, retrieving from driverManager");
                driver = initialSetup.getDriver();
            }
            driver.get(instance.getxPathInterface().getGroupsLink().concat(groupId));
            Thread.sleep(10000);

            WebElement postBox = null;

            logger.info("BroadcastToSensitiveGroups doYouWantToPostAnnonymously : {}", doYouWantToPostAnnonymously);

            if (doYouWantToPostAnnonymously) {
                postBox = toggleAnonymous();

            } else {
                clickOnWriteSomethingTextBox();
                Thread.sleep(4000);
                postBox = clickPostBoxToWriteFB(groupId);
                //"//*[@aria-label='Write something...' or text()='Write something...' or @aria-placeholder='Create a public post'  ]"
            }

            scrollAndSleepRandomly(5000, 5000);

            Thread.sleep(4000);

            boolean isPretendHumanTypingSuccess = writeMessageAfterPretendHumanTyping(message, groupId, postBox);
            if (!isPretendHumanTypingSuccess) {
                logger.error("Pretend human typing failed for groupId {}, so sending directly : ", groupId);
            }

            Thread.sleep(3000);
            if (doYouWantToPostAnnonymously) {
                submitAnonymouslyInFB();
            } else {
                postTheMessageToSubmitInFB();
            }

            logger.info("********************Message is posted successfully for groupId : " + groupId + " index:"
                    + groupIdList.indexOf(groupId) + " out of " + groupListSize+" ********************");
            scrollAndSleepRandomly(8000, 5000);

            WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));
        } catch (NoSuchElementException nse) {
            nse.printStackTrace();
            if (retryCounter == 0) {
                retryCounter = retryCounter + 1;
                //Retry only once
                logger.info("Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
                sendFacebookBroadcastToEachGroup(instance, groupIdList, message, groupListSize, groupId, doYouWantToPostAnnonymously);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in FB sensitive broadcast.............");
            retryCounter = 0;
        }
    }

    @Override
    public FlowType getEnumFlowType() {
        return FlowType.BROADCAST_SELF_SKILLSET_AD_TO_SENSITIVE_FB_GROUPS_ANNONYM;
    }

    @Override
    public Collection<String> getBroadcastGroupIdNameFBSet_() {
        return broadcastSensitiveGroupIdNameFBSet;
    }


    @Override
    public String getMessageToBroadcast() {
        return new Facebook_Xpaths()
                .getMESSAGE_TO_EACH_RESTRICTED_SENSITIVE_TRAINING_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT_ANNONYM();
    }

    @Override
    protected Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
        return clearUpdateCurrDateAndReloadFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB,
                GROUP_STRING_TRAVERSE_ALREADY_DATE_FB, groupsAlreadyMessageSentForTheDaySetFB, true);
    }

    public boolean isAnnonymous() {
        return false;
    }

    /**
     * Traverses through Facebook sensitive groups and performs the required actions.
     * This method is called from the starter class to initiate the group traversal process.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws AWTException         if there's an AWT exception
     * @throws IOException          if there's an I/O error
     */
    public void traverseGroups() throws InterruptedException, AWTException, IOException {
        logger.info("Starting to traverse Facebook sensitive groups for self-skills broadcast");

        try {
            // Call the parent class's traverseGroupsTemplate method with this instance
            // This will trigger the Facebook sensitive group traversal and posting process
            traverseGroupsTemplate(this);

            logger.info("Successfully completed Facebook sensitive groups traversal for self-skills broadcast");
        } catch (Exception e) {
            String errorMsg = "Error during Facebook sensitive groups traversal for self-skills broadcast: " + e.getMessage();
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
                logger.error("Error during FB Sensitive fetch all contacts and group names service execution", e);
            }
        });
    }

    @Override
    public Set<String> getBroadcastGroupIdNameFBSet() {
        return broadcastSensitiveGroupIdNameFBSet;
    }

}
