package com.wtf.app.app.commons;

import com.wtf.app.app.enums.*;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.xpaths.XPathInterfaceFB;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class FacebookCommonUtils extends SocialCommonUtils {

	private static final String STRING_SEPARATOR = "__";

	private XPathInterfaceFB xPathInterfaceFB;

	private boolean traverseDependsOnAllSuccessGroupList;
	private boolean annonymous;
	protected int retryCounter=0;
	protected List<MSG_POST_STATUS> msgPostedStatuses = new ArrayList<>();
	protected List<String> groupIdsFailed = new ArrayList<>();

	protected enum MSG_POST_STATUS {
		SUCCESS, FAILED, ONLY_DUMPS_PENDING_NEVER_POSTED
	};

	protected enum MSG_POST_STAGES_LOG {
		_1_MSG_READY_TO_POST, _2_MSG_POSTED_SUCCESSFULLY, _3_MSG_POST_FAILED, _4_MSG_POST_PENDING, _5_MSG_POSTED_PRIVATE_GROUP,_5_MSG_POSTING_PUBLIC_GROUP,_5_MSG_POSTING_GENERIC_GROUP,_5_MSG_POSTING_WRONG_GROUP
	};
	protected final int CONSECUTIVE_PENDING_TERMINATION_COUNTER = 20;
	protected boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast2DaysWA = groupsAlreadyMessageSentForTheDaySetWA
			.contains("group_traverse_already_date_" + LocalDate.now())
			|| groupsAlreadyMessageSentForTheDaySetWA
					.contains("group_traverse_already_date_" + LocalDate.now().minusDays(1l));

	protected abstract Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate();

	public FacebookCommonUtils(String baseURL) {
		super(baseURL, SocialType.FACEBOOK);
	}


	public void runFacebookAction(FacebookCommonUtils instance) throws InterruptedException {

		logger.fine( "Logging into Facebook..." + instance.getEnumFlowType().name());

		boolean wantToClearAllPendingPostsStaggedInEachGroup = false;//use it a separate initiator class

		// Set up Facebook groups to post, you must be a member of the group

		Map<String,String> groupIdNamesMapping = getBroadcastGroupIdNameFBSet().stream().filter(i-> i!=null).peek(i->System.out.println(i)).collect(Collectors.toMap(i->i.split(STRING_SEPARATOR)[0], i->i.split(STRING_SEPARATOR)[1]));
		List<String> groupIdList = groupIdNamesMapping.keySet().stream().sorted().collect(Collectors.toList());

		//Function.identity() is same as String::new

		// Set up text content to post
		String message = instance.getMessageToBroadcast();

		logger.fine( "The Message to be posted for All the FB Groups is...." + message);

		// # Set up paths of images to post
		// List images_list = {};

		// Login Facebook
		WebElement emailElement = driver.findElement(By.xpath("//*[@id='email']"));
		emailElement.sendKeys(xPathInterfaceFB.getEmail());
		Thread.sleep(2);
		WebElement passElement = driver.findElement(By.xpath("//*[@id='pass']"));
		passElement.sendKeys(xPathInterfaceFB.getPassword());
		Thread.sleep(2);

		WebElement loginElement = driver.findElement(By.xpath("//button[@name='login' and @type='submit']"));
		loginElement.click();

		Thread.sleep(20);

		//driver.findElement(By.xpath("//span[text()='Try Another Way']")).click();// anotherWayOTP

		Thread.sleep(90000);

		//delete All Pending Posts from each group


		if(wantToClearAllPendingPostsStaggedInEachGroup) {
				deleteAllPendingPosts(groupIdList);
		}


		//waitIdentifyAndClickOnThePinnedElement("//div[@aria-label='Your profile']");

		Set<String> alreadyTraversedGroupsOnly = instance.getAlreadyTraversedGroupsOnlyAndUpdateDate();
		Set<String> pendingPostPiledGroupsFB = instance.getPendingPostPiledGroupsFB_set();
		groupIdList.removeAll(alreadyTraversedGroupsOnly);
		groupIdList.removeAll(pendingPostPiledGroupsFB);

		int groupListSize = groupIdList.size();


		// # Post adv skills into each group

		for (String groupId : groupIdList) {
			if (alreadyTraversedGroupsOnly.contains(groupId)) {//Should not be invoked anymore
				logger.fine( "Ohh! Message Already sent to the Group today/yesterday...."
						+ xPathInterfaceFB.getGroupsLink().concat(groupId));
				continue;
			}

			logger.fine( "FB Group Path Invoked by groupId....\n" + xPathInterfaceFB.getGroupsLink().concat(groupId)+"\n Group Name: "+ groupIdNamesMapping.get(groupId));

			try {
				/* Uncomment if want to take any action seeing pending posts*/
				/*boolean isPendingPostsArePresent = verifyIfPendingPostAlpreadyPresent(groupId);
				if(isPendingPostsArePresent) {
					continue;
				}*/
				sendFacebookBroadcastToEachGroup(instance, groupIdList, message, groupListSize, groupId, instance.isAnnonymous());

			} catch (Exception ex) {
				logger.log(Level.SEVERE,"Exception : "+ex.getMessage());
			}
		}

		logger.fine( "Message is posted Completed Successfully in all the groups : " + groupListSize);
		driver.close();
		return;
	}

	/* Not in Use Directly*/
	boolean verifyIfPendingPostAlpreadyPresent(String groupId) {
			int pendingPostsNumber = getPendingPostsNumber();
			logger.fine( "Posts are Already Pending with "+pendingPostsNumber+". Message can't be posted in the group id :" + groupId);
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(GROUPS_PENDING_POSTS_MESSAGE_PILED_UP_FB, Set.of(groupId));
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));
			return true;
		}

	int getPendingPostsNumber() {
		List<WebElement> pendingPostAlready = driver.findElements(By.xpath("//span[text()='Pending post' or text()='Pending content']"));
		if(!pendingPostAlready.isEmpty()) {
			WebElement pendingCountText = driver.findElement(By.xpath("//span[text()='Pending post' or text()='Pending content']//following-sibling::div//span"));
		//else if(!driver.findElements(By.xpath("//span[text()='You're at the limit for pending content in this group.']")).isEmpty())
		String text = pendingCountText.getText();
		return text.isEmpty()? 0: Integer.valueOf(text.substring(0,text.indexOf(" ")));
		}
		return 0;
	}



	public void deleteAllPendingPosts(List<String> groupIdList) throws InterruptedException {
		for(String groupId : groupIdList) {

			try {
				deletePendingPostsByGroupId(groupId);

				logger.fine( "Pending Messages are deleted for groupId : " + groupId + " index:"
						+ groupIdList.indexOf(groupId) + " out of " + groupIdList.size());

			  }catch(Exception ex) {
				  logger.log(Level.SEVERE, "Sorry! Pending Messages are not deleted for groupId : " + groupId + " index:"
							+ groupIdList.indexOf(groupId) + " out of " + groupIdList.size());
			}
		}

	}

	void deletePendingPostsByGroupId(String groupId) throws InterruptedException {
		boolean isPendingPostsPresent = isPendingPostsPresent(groupId);
		if(!isPendingPostsPresent) {
			logger.fine( "Now All Pending Messages are deleted for groupId : " + groupId );
			return;
		}

		List<WebElement> pendingDeleteBtns = driver.findElements(By.xpath("//div[@aria-label='Delete' and @role='button' and @tabindex='0']"));

		for(WebElement delete : pendingDeleteBtns) {
			delete.click();
			Thread.sleep((long) (Math.random()*2000+1000));
			driver.findElement(By.xpath("//div[@aria-label='Cancel']//parent::div//preceding-sibling::div//child::div[@aria-label='Delete']")).click();
			Thread.sleep((long) (Math.random()*2000+3000));
		};
		deletePendingPostsByGroupId(groupId);
	}

	protected boolean isPendingPostsPresent(String groupId) throws InterruptedException {
		driver.get(xPathInterfaceFB.getGroupsLink().concat(groupId)+"/my_pending_content");
		/* above one is a shortcut ready-made solution.Ebable when the above is retired*/
		//checkPendingPostsAndClickOnMagePosts(groupId);

		Thread.sleep((long) (Math.random()*2000+1000));

		List<WebElement> pendingDeletes = driver.findElements(By.xpath("//span[text()='Delete']"));
		if(pendingDeletes.isEmpty()){
			logger.fine( "Good News!! No Pending Posts are there to delete for groupId :" + groupId);
			return false;
		}
		logger.fine( "Posts are Already Pending with count: "+pendingDeletes.size()+" group id :" + groupId);
		return true;
	}

	/* Currently not in use, required if pending post url does not work*/
	void checkPendingPostsAndClickOnMagePosts(String groupId) throws InterruptedException {
		driver.get(xPathInterfaceFB.getGroupsLink().concat(groupId));
			logger.fine( "Group Path Invoked....\n" + xPathInterfaceFB.getGroupsLink().concat(groupId));
			Thread.sleep(10000);
		int pendingPostsCount = getPendingPostsNumber();
		if(pendingPostsCount==0) {
			logger.fine( "Good News! No Pending Posts are there to delete for groupId :" + groupId);
			return;
		}
		Thread.sleep((long) (Math.random()*3000+4000));
		driver.findElement(By.xpath("//span[text()='Manage posts']")).click();// we can use direct delete link as below
	}



	public Set<String> clearUpdateCurrDateAndReloadFile(String fileNameWithExtension, String messageFormatInsideFile,
			Set<String> groupsAlreadyMessageSentForTheDaySet, boolean clearExistingContent) {

		if (groupsAlreadyMessageSentForTheDaySet.contains(messageFormatInsideFile + LocalDate.now())
				|| groupsAlreadyMessageSentForTheDaySet.contains(messageFormatInsideFile + LocalDate.now().minusDays(1l))|| groupsAlreadyMessageSentForTheDaySet
								.contains(messageFormatInsideFile + LocalDate.now().minusDays(2l))) {
			logger.fine("ClearUpdateCurrDateAndReloadFile : Message Already Posted within 2 Days : "+groupsAlreadyMessageSentForTheDaySet+ " fileNameWithExtension: "+fileNameWithExtension);
			return groupsAlreadyMessageSentForTheDaySet;
		} else {
			if (clearExistingContent) {
				logger.fine("ClearUpdateCurrDateAndReloadFile : clearExistingContent is true means message Posted long back.So clearning the file : clearExistingContent : "+clearExistingContent+ " fileNameWithExtension: "+fileNameWithExtension);
				WhatsappContactUtils.clearContentInFile(fileNameWithExtension);
			}
			logger.fine("ClearUpdateCurrDateAndReloadFile : exportAndMergeToExisingSingleFile : "+clearExistingContent+ " fileNameWithExtension: "+messageFormatInsideFile + LocalDate.now());
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(fileNameWithExtension,
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

	public abstract Collection<String> getBroadcastGroupIdNameFBSet();

	public Set<String> getPendingPostPiledGroupsFB_set(){
		return pendingPostPiledGroupsFB_set;
	}

	public abstract void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList,
			String message, int groupListSize, String groupId, boolean annonymous) throws InterruptedException;

	public abstract String getMessageToBroadcast();

	public abstract FlowTypeEnum getEnumFlowType();

	public abstract void traverseGroupsTemplate(FacebookCommonUtils instance)
			throws InterruptedException, AWTException, IOException;

	public abstract boolean isAnnonymous();

}