package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.FacebookBroadcastParent;
import com.wtf.app.app.commons.FacebookCommonUtils;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.xpaths.Facebook_Xpaths;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class FB_A_BroadcastSelfSkillset extends FacebookBroadcastParent {

    public FB_A_BroadcastSelfSkillset(String baseURL) {
        super(baseURL);
    }

    @Override
    public void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList, String message,
                                                 int groupListSize, String groupId, boolean annonymous) throws InterruptedException {

        try {
            driver.get(instance.getxPathInterface().getGroupsLink().concat(groupId));
            Thread.sleep(10000);

            driver.findElement(By.xpath("//span[text()='Write something...']")).click();

            scrollAndSleepRandomly(5000, 5000);


            WebElement postBox = null;
            try {
                List<WebElement> postBoxes = driver.findElements(By.xpath("//*[contains(@aria-placeholder,'Create a public post')]"));
                if (!postBoxes.isEmpty()) {
                    postBox = postBoxes.getFirst();
                    logger.fine(MSG_POST_STAGES_LOG._5_MSG_POSTING_PUBLIC_GROUP + " : " + "Its a Public Group to be posted with the group id :" + groupId);
                } else {
                    if (!driver.findElements(By.xpath("//div[text()='Private']")).isEmpty()) {
                        logger.fine(MSG_POST_STAGES_LOG._5_MSG_POSTED_PRIVATE_GROUP + " : " + "Its a Private Group, skipping to be posted with the group id :" + groupId);
                        return;
                    }
                    postBoxes = driver.findElements(By.xpath("//span[contains(text(),'Write something...')]"));

                    if (!postBoxes.isEmpty()) {
                        postBox = postBoxes.getFirst();
                        logger.fine(MSG_POST_STAGES_LOG._5_MSG_POSTING_GENERIC_GROUP + " : " + "Its a GENERIC Group to be posted with the group id :" + groupId);
                    }
                }

                if(postBox == null) {
                    logger.fine(MSG_POST_STAGES_LOG._5_MSG_POSTING_WRONG_GROUP + " : " + "SOMETHING IS WRONG, SKIPPING with the group id :" + groupId);
                    return;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                return;
            }

            scrollAndSleepRandomly(2000, 3500);

            String greet = getRandomGreeting();
            message = greet + ", " + message;

            // postBox.sendKeys(message);
            gradualTypeMessageCustom(postBox, message);

            Actions actions = new Actions(driver);
            actions.sendKeys(Keys.ARROW_DOWN, Keys.ENTER).perform();
            randomDelay(100,200);
            actions.sendKeys(Keys.BACK_SPACE).perform();
            logger.fine(MSG_POST_STAGES_LOG._1_MSG_READY_TO_POST + " : " + "Message is ready to be posted in the group id :" + groupId);

            scrollAndSleepRandomly(3000, 5000);

            driver.findElement(
                            By.xpath("//span[text()='Post']//parent::span//parent::div//parent::div//parent::div//parent::div"))
                    .click();

            scrollAndSleepRandomly(10000, 7000);

            if (isPendingPostsPresent(groupId)) {
                logger.fine(MSG_POST_STAGES_LOG._4_MSG_POST_PENDING + " : " + "Ohh! Still Pending Post created after Posting the new Message for groupId : " + groupId + " index:"
                        + groupIdList.indexOf(groupId) + " out of " + groupListSize);
            } else {
                logger.fine(MSG_POST_STAGES_LOG._2_MSG_POSTED_SUCCESSFULLY + " : " + "Message is posted successfully for groupId : " + groupId + " index:"
                        + groupIdList.indexOf(groupId) + " out of " + groupListSize);
            }

            scrollAndSleepRandomly(5000, 4000);

            TerminateIfMessagesAreNotPostingAtAll(groupId);

            WhatsappContactUtils.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));
            Thread.sleep(4000);
            retryCounter = 0;

        } catch (NoSuchElementException nse) {
            nse.printStackTrace();
            if (retryCounter == 0) {
                retryCounter = retryCounter + 1;
                //Retry only once
                logger.fine("Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
                sendFacebookBroadcastToEachGroup(instance, groupIdList, message, groupListSize, groupId, annonymous);
            }
        } catch (Exception e) {
            logger.fine(MSG_POST_STAGES_LOG._3_MSG_POST_FAILED + " : " + "Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
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
                logger.log(Level.SEVERE, MSG_POST_STATUS.ONLY_DUMPS_PENDING_NEVER_POSTED.toString());
                System.exit(-1);
            }
        }
    }

    @Override
    public FlowTypeEnum getEnumFlowType() {
        return FlowTypeEnum.BROADCAST_SELF_SKILLSET_AD_TO_GROUPS;
    }

    @Override
    public Collection<String> getBroadcastGroupIdNameFBSet() {
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
}
