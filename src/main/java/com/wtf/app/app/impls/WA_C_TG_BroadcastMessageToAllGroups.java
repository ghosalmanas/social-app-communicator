package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.ActionIfTheGroupIsEligible;
import com.wtf.app.app.enums.EnumStringToExport;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.interfaces.IBroadcasteAllSupportGroups;
import org.openqa.selenium.WebElement;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public class WA_C_TG_BroadcastMessageToAllGroups extends WhatsappParent implements IBroadcasteAllSupportGroups {

	public WA_C_TG_BroadcastMessageToAllGroups(String baseURL, SocialType socialType) {
		super(baseURL, socialType);
	}

	private static final int SUPPORT_GROUP_SIZE_CREATED_BY_ME_FOR_SUPPORT_OR_BY_MEDIATOR_FOR_PROXY = 5;

	private FlowTypeEnum enumFlowType = FlowTypeEnum.BROADCAST_SELF_SKILLSET_AD_TO_GROUPS;

	Set<String> alreadyTraversedAndMessageSentGroups = new TreeSet<>();
	private boolean isSuccessfulSupportGroupContainsCheckRequired = false;
	private WhatsappCommonUtils instance;

	@Override
	public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(WhatsappCommonUtils instance, boolean hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious,
                                                                                           String groupNameExceptIndv, Set<String> allPhoneContacts, int counter, int repeatCounter)
			throws InterruptedException {

		logger.fine( "overriden takeActionIfTheGroupIsEligibleToProceed: "+groupNameExceptIndv+" hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious: "+hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious+" groupNameExceptIndv: "+ groupNameExceptIndv +" allPhoneContacts size: "+allPhoneContacts.size()+" counter: "+ counter +" repeatCounter: "+repeatCounter);

		this.instance = instance;

		boolean isCurrGroupANewPersonalCreatedGroup=false;
		//for WA
		if(instance.getxPathInterface().getSocialType()==SocialType.WHATSAPP) {

		//Intention : Extract Contacts from Group by Mouse Hover
		Set<String> phContactsChunk = extractContactsByGroupMouseHover(3000);

		//Intention : Avoid accessing newly created Personal Group
		Predicate<Set<String>> isCurrGroupNotANewPersonalCreatedGroup = phContactsChunks -> !validateBroadcastRiskIfNewPersonalGroupFound(groupNameExceptIndv,
				phContactsChunks);

		//Intention : Avoid sending broadcast proxy message to created Personal Support Group having total 3 persons or proxy group for me by mediator
		Predicate<Set<String>> isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe = phContactsChunks-> phContactsChunks.size() > SUPPORT_GROUP_SIZE_CREATED_BY_ME_FOR_SUPPORT_OR_BY_MEDIATOR_FOR_PROXY;

		//Intention : Either the flag for successful groups check is not required or if required, then the group list must contain it
		Predicate<String> shouldBroadcastChecksAllSuccessful = groupNameExceptInd->!instance.isTraverseDependsOnAllSuccessGroupList() ||(instance.isTraverseDependsOnAllSuccessGroupList() && allSuccessfulSupportGroupsFromFile.contains(groupNameExceptInd));

		if(isCurrGroupNotANewPersonalCreatedGroup.test(phContactsChunk) && isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe.test(phContactsChunk) && shouldBroadcastChecksAllSuccessful.test(groupNameExceptIndv)){
			isCurrGroupANewPersonalCreatedGroup=false;
		}else {
			isCurrGroupANewPersonalCreatedGroup=true;

		}
		logger.fine( "allPhoneContacts updated size: in overriden takeActionIfTheGroupIsEligibleToProceed:  "+allPhoneContacts.size() +" isCurrGroup_NOT_ANewPersonalCreatedGroup : "+isCurrGroupNotANewPersonalCreatedGroup.test(phContactsChunk)+" isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe : "+isNotA3PersonSupportGroupByMEOrProxyGroupCreatedForMe.test(phContactsChunk)+" shouldBroadcastChecksAllSuccessful : "+shouldBroadcastChecksAllSuccessful.test(groupNameExceptIndv));
		}

		if(!isCurrGroupANewPersonalCreatedGroup) {
			try {
				// Broadcast to Group
				broadcastMessageToSupportGroups();
				hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = true;
			} catch (Exception ex) {
				logger.fine( "Exception in broadcastMessageToSupportGroups : " + ex.getLocalizedMessage());
				ex.printStackTrace();
			}
		}


		Map<ActionIfTheGroupIsEligible, Object> actionMap = Map. ofEntries(Map.entry(ActionIfTheGroupIsEligible.FLAG_TRUE, hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious), Map.entry(ActionIfTheGroupIsEligible.GROUP_NAME, groupNameExceptIndv), Map.entry(ActionIfTheGroupIsEligible.SET_OF_DATA, allPhoneContacts), Map.entry(ActionIfTheGroupIsEligible.COUNTER, counter++), Map.entry(ActionIfTheGroupIsEligible.REPEAT_COUNTER, repeatCounter));
		return actionMap;
	}

	@Override
	public void broadcastMessageToSupportGroups() throws InterruptedException {
		WebElement webElementTypeMsg = febx(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
		Thread.sleep(200);//NA

		/* Human Like behavior added in WATG following FB*/

		String message = getxPathInterface().getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT();
		String greet = getRandomGreeting();
		message = greet + ", " + message;

		//webElementTypeMsg.sendKeys(message);
		gradualTypeMessageOnlyLastLine(webElementTypeMsg, message);
		//gradualTypeMessageByWords(webElementTypeMsg, message);

		logger.fine( "Message is ready to be posted with greet" + message);
		scrollAndSleepRandomly(2000, 3500);

		Thread.sleep(1000);
		febx(getxPathInterface().getSPAN_DATA_TESTID_SEND()).click();
		Thread.sleep(1000);
		logger.fine( "Message Broadcasted Successfully.............");
	}

	@Override
	public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate(){
		logger.info("getAlreadyTraversedGroupsOnlyAndUpdateDate : ClearUpdateCurrDateAndReloadFile : this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() : "+this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG()+ " groupsAlreadyMessageSentForTheDaySetWA: "+groupsAlreadyMessageSentForTheDaySetWA.size());
		return clearUpdateCurrDateAndReloadFile(this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG(), GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG, groupsAlreadyMessageSentForTheDaySetWA, true);
	}



	@Override
	public boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping) {
		logger.info("exportDataToFile : exportAndMergeToExisingSingleFile : this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() : "+fileName+ " SUCCESSFUL_GROUP_NAMES: "+setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
		WhatsappContactUtils.exportAndMergeToExistingSingleFile(fileName,setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
	return true;

	}
	@Override
	public int getExpectedSizeToModuloToInsertExportToFile() {
		return 1;
	}

	@Override
	public String getFilePathToExportContactsData() {
		return this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG();
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
	public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored, String lastVisitedGroup) {
			searchAndClickOnGroup(lastVisitedGroup);
			hasNewGroupJoinedButNotRestored = false;
		return hasNewGroupJoinedButNotRestored;
	}

	//@Override
	/*public void broadcastMessageToSupportGroups(String fileNameToReadAllGroupNames, boolean needToSendMessageToEveryGroup, String broadcastMessageToSend) throws InterruptedException, AWTException, IOException {
		// Iterating not to miss the groups receiving messages
		//IntStream.range(0, 5).forEach(i -> {

			try {
				driver.get(driver.getCurrentUrl());
				logger.fine( "driver page refresh");
				Thread.sleep(2000);
				broadcastToAllSupportGroup();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		//});

		driver.quit();
	}*/

//NOT APPLICABLE
	/*public void broadcastToAllSupportGroup() throws InterruptedException, AWTException, IOException {

	Set<String> groupNames = new TreeSet<>();
	Set<String> groupNamesInvalid = new TreeSet<>();

	int counter = 0;
	String lastVisitedGroup = "";
	boolean hasNewGroupJoinedButNotRestored = false;

	boolean hasMoreElementsToTraverse = true;
	int countAlreadyExists = 0;

	while (hasMoreElementsToTraverse) {

		if (counter >= 10 && hasNewGroupJoinedButNotRestored) {
			CommonImpl.searchAndClickOnGroup(lastVisitedGroup);
			hasNewGroupJoinedButNotRestored = false;
		}

		runRobotClassForNextChat();

		try {
			String groupInfo = null;
			String groupNameExceptIndv = febx(FIND_NEXT_CHAT_IND_AFTER_CLICK_GROUP_NAME_RIGHT_TOP)).getText()
					.trim();

			logger.fine( "\n\n\n" + groupNameExceptIndv);

			if (alreadyTraversedAndMessageSentGroups.contains(groupNameExceptIndv)) {

				if (countAlreadyExists >= 5) {
					hasMoreElementsToTraverse = false;
				} else {
					countAlreadyExists = +1;
				}
				logger.fine( "groupNameExceptIndv :"+groupNameExceptIndv+" countAlreadyExists "+countAlreadyExists+ " hasMoreElementsToTraverse: "+hasMoreElementsToTraverse);
				continue;
			} else {
				alreadyTraversedAndMessageSentGroups.add(groupNameExceptIndv);
				lastVisitedGroup = groupNameExceptIndv;
				counter++;
			}


			List<WebElement> elemGroupInfoOrPhOrAnnouncements = getDriver()
					.findElements(By.xpath(SUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT));

			if (elemGroupInfoOrPhOrAnnouncements.isEmpty()) {
				logger.fine( "webElement.isEmpty::>> " + groupNameExceptIndv);
				continue;
			} else {
				groupInfo = elemGroupInfoOrPhOrAnnouncements.get(0).getText();
				logger.fine( "groupInfo: " + groupInfo);

				if (groupInfo.contains("group info") || groupInfo.contains(",")) {
							if (allSuccessfulSupportGroupsFromFile.stream().anyMatch(group -> group.trim().contains(groupNameExceptIndv)
									|| groupNameExceptIndv.contains(group.trim()))
							&& alreadyTraversedAndMessageSentGroups.stream().noneMatch(sentGroup -> sentGroup.trim().contains(groupNameExceptIndv)
									|| groupNameExceptIndv.trim().contains(sentGroup.trim()))
							&& excludePersonalGroupNamesSet.stream().noneMatch(sentGroup -> sentGroup.trim().contains(groupNameExceptIndv)
									|| groupNameExceptIndv.trim().contains(sentGroup.trim()))
							&& excludeSupportGroupNamesSet.stream().noneMatch(sentGroup -> sentGroup.trim().contains(groupNameExceptIndv)
									|| groupNameExceptIndv.trim().contains(sentGroup.trim()))
							&& neglectToJoinGroupsWithTextSetLowerCase.stream().noneMatch(sentGroup -> sentGroup.trim().contains(groupNameExceptIndv)
									|| groupNameExceptIndv.trim().contains(sentGroup.trim())))
					{
						logger.fine( "Match found :current group inside Support groups but not in alreadyBroadcastedGroups : group is elible to recieve the Message Broadcast");

						logger.fine( "Not Present in Exclusion List : " + groupNameExceptIndv);

						if (groupNames.add(groupNameExceptIndv) ) {
							broadcastToGroups(groupNameExceptIndv, null);
						} else {
							logger.fine( "Duplicate Present : " + groupNameExceptIndv);
							groupNamesInvalid.add(groupNameExceptIndv);

							if (countAlreadyExists == 10) {
								hasMoreElementsToTraverse = false;
							} else {
								countAlreadyExists = +1;
							}
						}

					if (groupNames.size() % 10 == 0) {
						logger.fine( groupNames.toString());
						WhatsappContactUtils.exportToSingleFile("GroupsAlreadyRecievedBroadcast.txt", groupNames);
						// System.exit(1);
					}
				} else if (groupInfo.contains("Announcements")) {
					//broadcastSupportGrpOrExitAnnouncement(groupNameExceptIndv,EnumActionOnRightClickPopUp.EXIT_ANNOUNCEMENTS_GROUP);
				}
			}

			}} catch (Exception ex) {
			logger.fine( ex.getLocalizedMessage());
		}

	}

	logger.fine( groupNames.toString());
	String localFormat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

	groupNames.add("Execution Completed..........." + localFormat);

	WhatsappContactUtils.exportToSingleFile("GroupsAlreadyRecievedBroadcast.txt", groupNames);

	driver.close();
}

	*/
	void broadcastToGroups(String groupNameExceptIndv,String BROADCAST_MESSAGE_TO_SEND) {

		if(allSuccessfulSupportGroupsFromFile.contains(groupNameExceptIndv)) {

		//Broadcast to Group
		WebElement webElementTypeMsg = febx(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
		webElementTypeMsg.sendKeys(BROADCAST_MESSAGE_TO_SEND);
		febx(getxPathInterface().getSPAN_DATA_TESTID_SEND()).click();
		}
	}

	@Override
	public FlowTypeEnum getEnumFlowType() {
		return enumFlowType;
	}

	@Override
	public boolean isSuccessfulSupportGroupContainsCheckRequired() {
		return isSuccessfulSupportGroupContainsCheckRequired;
	}
}
