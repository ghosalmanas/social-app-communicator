package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.FacebookBroadcastParent;
import com.wtf.app.app.commons.FacebookCommonUtils;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.interfaces.IFacebookPluggable;
import com.wtf.app.app.xpaths.Facebook_Xpaths;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FB_B_BroadcastToSensitiveGroupAnnonym extends FacebookBroadcastParent
		implements IFacebookPluggable {

	public FB_B_BroadcastToSensitiveGroupAnnonym(String baseURL) {
		super(baseURL);
	}

	@Override
	public void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList, String message,
			int groupListSize, String groupId, boolean doYouWantToPostAnnonymously) throws InterruptedException {
		try {
				driver.get(instance.getxPathInterface().getGroupsLink().concat(groupId));
				Thread.sleep(10000);

				WebElement postBox=null;

				if(doYouWantToPostAnnonymously) {
					driver.findElement(By.xpath("//span[text()='Write something...']")).click();

					Thread.sleep(2000);
					driver.findElement(By.xpath("//input[@role='switch' and @aria-label='Anonymous post toggle']")).click();// Annonymously
					Thread.sleep(2000);
					try {
						driver.findElement(By.xpath("//div[@aria-label='Got it']")).click();
					} catch (Exception ex) {
						logger.fine("Annonymous Got it not there or Not required");
					}

					Thread.sleep(4000);
					postBox = driver.findElement(By.xpath("//div[contains(@aria-label,'Submit an anonymous post')]"));

				}else {
					driver.findElement(By.xpath("//span[text()='Write something...']")).click();

					Thread.sleep(4000);
					postBox = driver.findElement(By.xpath("//*[@aria-placeholder='Create a public post�']"));
					//"//*[@aria-label='Write something...' or text()='Write something...' or @aria-placeholder='Create a public post�'  ]"
				}

				Thread.sleep(4000);
				postBox.sendKeys(message);
				logger.fine( "Message is ready to be posted in the group id :" + groupId);

				Thread.sleep(3000);
				if(doYouWantToPostAnnonymously) {
					driver.findElement(By.xpath("//span[text()='Submit']//parent::span//parent::div//parent::div//parent::div//parent::div")).click();
				}else {
					driver.findElement(
							By.xpath("//span[text()='Post']//parent::span//parent::div//parent::div//parent::div//parent::div"))
							.click();
				}

				logger.fine( "Message is posted successfully for groupId : " + groupId + " index:"
						+ groupIdList.indexOf(groupId) + " out of " + groupListSize);
				Thread.sleep(8000);

				WhatsappContactUtils.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
			if(retryCounter==0) {
			retryCounter=retryCounter+1;
			//Retry only once
			logger.fine( "Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
			sendFacebookBroadcastToEachGroup( instance,groupIdList,  message, groupListSize, groupId, doYouWantToPostAnnonymously);
			}
		}catch(Exception e) {
			e.printStackTrace();
			retryCounter=0;
		}
	}



	@Override
	public FlowTypeEnum getEnumFlowType() {
		return FlowTypeEnum.BROADCAST_SELF_SKILLSET_AD_TO_SENSITIVE_FB_GROUPS_ANNONYM;
	}

	@Override
	public Collection<String> getBroadcastGroupIdNameFBSet() {
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

	public boolean isAnnonymous() { return false;}
}
