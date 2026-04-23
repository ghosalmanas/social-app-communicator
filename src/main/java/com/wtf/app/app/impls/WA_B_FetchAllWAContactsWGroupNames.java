package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.ActionIfTheGroupIsEligible;
import com.wtf.app.app.enums.EnumStringToExport;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.enums.SocialType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WA_B_FetchAllWAContactsWGroupNames
		extends WhatsappParent/* implements IAllWAGroupNamesRetrival_JoinNewGroups */{

	private static final String YOU_CANT_ACCESS_THIS_CHAT_NOT_A_MEMBER = "//*[contains(text(), 'Unfortunately, you can')]";
	String previousGroupLink="";
	String previousGroup="";
	Set<String> alreadyJoinedGroupLinks = new HashSet<>();

	public WA_B_FetchAllWAContactsWGroupNames(String baseURL, SocialType socialType) {
		super(baseURL,socialType);
	}

	private FlowTypeEnum enumFlowType = FlowTypeEnum.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS;
	private boolean isSuccessfulSupportGroupContainsCheckRequired = false;


	@Override
	public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored, String lastVisitedGroup) {
			searchAndClickOnGroup(lastVisitedGroup);
			hasNewGroupJoinedButNotRestored = false;
		return hasNewGroupJoinedButNotRestored;
	}

	@Override
	public boolean exportDataToFile(String fileName, Map<EnumStringToExport,Set<String>> setOfDataMapping) {
			WhatsappContactUtils.exportToFile(fileName,setOfDataMapping.get(EnumStringToExport.NEW_PH_CONTACTS),setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES),setOfDataMapping.get(EnumStringToExport.ERROR_GROUP_NAMES));
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(this.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA(),setOfDataMapping.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
		return true;
	}

	@Override
	public String getFilePathToExportContactsData() {
		return getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS();
	}

	@Override
	public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate(){
		return clearUpdateCurrDateAndReloadFile(this.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA(), GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG, groupsAlreadyMessageSentForTheDaySetWA, true);
	}

	@Override
	public int getExpectedSizeToModuloToInsertExportToFile() {
		if(this.isRunningTestSuite()) {
			return 2;
		}
		return 10;
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
	public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(WhatsappCommonUtils instance, boolean hasNewGroupJoinedButNotRestored, String groupNameExceptIndv, Set<String> allPhoneContacts, int counter, int repeatCounter) throws InterruptedException {

		logger.fine( "overriden takeActionIfTheGroupIsEligibleToProceed > hasNewGroupJoinedButNotRestored: "+hasNewGroupJoinedButNotRestored+" groupNameExceptIndv: "+ groupNameExceptIndv +" allPhoneContacts size: "+allPhoneContacts.size()+" counter: "+ counter +" repeatCounter: "+repeatCounter);

		updateWAAndTGSpecificDifferentSleepTime(null, instance,2000,2000);

		executeIfSyncingRequired(instance);

		boolean chatsStillLoading = !febxs("//*[contains(@title,'Loading messages')]").isEmpty()
				||!febxs("//*[text()='Waiting for this message. This may take a while.']").isEmpty()
				||!febxs("//*[text()='Syncing paused. Open WhatsApp on your phone to continue syncing.']").isEmpty();

		if(chatsStillLoading) {
			logger.info( "ChatsStillLoading..So delaying for longer periods");
			updateWAAndTGSpecificDifferentSleepTime(null, instance, 5000/*10000*/,2000);
			doSomeAwaking(groupNameExceptIndv, 3);
		}

		boolean isCurrGroupANewPersonalCreatedGroup=false;
	//UseCases to handle
		//handle TELANGANA US IT STAFFING  REMOTE No Hotlist

		//for WA
		if(instance.getxPathInterface().getSocialType()==SocialType.WHATSAPP) {

		//1. extractContacts
		Set<String> phContactsChunk = extractContactsByGroupMouseHover(3000);

		//Start : Avoid accessing newly created Personal Group
		 isCurrGroupANewPersonalCreatedGroup = validateBroadcastRiskIfNewPersonalGroupFound(groupNameExceptIndv,
				phContactsChunk);

		//Merge with allContacts if newExtractcted Contacts are valid
		if(!isCurrGroupANewPersonalCreatedGroup) {
			allPhoneContacts = addMergePhChunksIntoAllPhoneContacts(allPhoneContacts, phContactsChunk);
		}
		//End : Avoid accessing newly created Personal Group

		logger.fine( "allPhoneContacts updated size: in overriden takeActionIfTheGroupIsEligibleToProceed:  "+allPhoneContacts.size() +" isCurrGroupANewPersonalCreatedGroup : "+isCurrGroupANewPersonalCreatedGroup);
		}

		if(!isCurrGroupANewPersonalCreatedGroup) {
		//2. Join group inside a group
			try {
				if(instance.getxPathInterface().getSocialType()==SocialType.TELEGRAM) {
					febxsAndClick("//*[@title='Go to bottom']", null, false,"The_Go_To_Button_For_Unread_Chats ");
				}
				hasNewGroupJoinedButNotRestored = joinAllGroups(instance, groupNameExceptIndv, alreadyJoinedGroupsSet, neglectToJoinGroupsWithTextSetLowerCase, 3000);

			} catch (Exception exception) {
				exception.printStackTrace();
				logger.log(Level.SEVERE,"Exception in joinAllGroups : " + exception.getLocalizedMessage());
				hasNewGroupJoinedButNotRestored = true;
			}
		}

		Map<ActionIfTheGroupIsEligible, Object> actionMap = Map. ofEntries(Map.entry(ActionIfTheGroupIsEligible.FLAG_TRUE, hasNewGroupJoinedButNotRestored), Map.entry(ActionIfTheGroupIsEligible.GROUP_NAME, groupNameExceptIndv), Map.entry(ActionIfTheGroupIsEligible.SET_OF_DATA, allPhoneContacts), Map.entry(ActionIfTheGroupIsEligible.COUNTER, counter++), Map.entry(ActionIfTheGroupIsEligible.REPEAT_COUNTER, repeatCounter));
		return actionMap;
	}

	private void executeIfSyncingRequired(WhatsappCommonUtils instance) throws InterruptedException {
		List<WebElement> syncing = febxs("//*[text()='Syncing older messages. Click to see progress.']");
		if(!syncing.isEmpty()) {
			logger.info( "Syncing older messages clicked..So delaying for longer periods");
			syncing.getFirst().click();
			updateWAAndTGSpecificDifferentSleepTime(null, instance, 5000/*10000*/,2000);
			executeIfSyncingRequired(instance);
		}
	}


	private boolean joinAllGroups(WhatsappCommonUtils instance, String currentGroup, Set<String> alreadyJoinedGroupLinksFromFileDB,
			Set<String> neglectToJoinGroupsWithTextSetLowerCase, long sleep) throws InterruptedException {

		alreadyJoinedGroupLinks.addAll(alreadyJoinedGroupLinksFromFileDB);
		boolean[] hasNewGroupJoinedButNotRestored = new boolean[] { false };

		int whileCounter=1;
		int joinGroupLinksSize = febxs(instance.getxPathInterface().getJOIN_NEW_GROUP_LINK()).size();
		boolean isAnyPendingGroupLinksRemainingToBeCovered = true;
		int sameGroupLinkInSameGroupCounter=0;

		//Ideally loop not required as we are clicking a single group at a time
			while(isAnyPendingGroupLinksRemainingToBeCovered) {

				if(whileCounter % 50==0) {//To avoid infinity flow. Considering 50 links cant be in a group
					logger.fine( "Sorry !! BREAKING the while loop as the counter modulo of 20 currentGroup: "+currentGroup+ " whileCounter :"+whileCounter);
					isAnyPendingGroupLinksRemainingToBeCovered =false;
					return false;
					//break;
				}
				whileCounter++;
				List<WebElement> webElements = driver.findElements(By.xpath(instance.getxPathInterface().getJOIN_NEW_GROUP_LINK()));

				//Thread.sleep(1000);// SafeSide
				if(webElements.isEmpty()) {
					logger.severe( "**** Sorry !! No Join Group Links Present in this group: "+currentGroup);
					return false;
				}

				try {
					Set<String> groupsLinks = webElements.stream().map(WebElement::getText).collect(Collectors.toSet());//convert to for loop if further stream recieved
					logger.info( "joinAllGroupsLinks found links: counts" +groupsLinks.size()+" groupsLinks: "+ groupsLinks);
					logger.fine( "joinLinkCurrentCount_webElements_size: " + webElements.size()+ " whileCounter : "+whileCounter+ " joinGroupLinksPreviousCount :"+joinGroupLinksSize);
				}catch(Exception ex) {
					logger.info("Exception in grouNamesToJoin link extractions: NOT SEVERE");
				}

				Optional<WebElement> joinGroupWEOptional = webElements.stream().filter(joinGroup1 -> !joinGroup1.getText().isEmpty() && !alreadyJoinedGroupLinks.contains(joinGroup1.getText())).findFirst();

				/* Break The Loop*/
				if(joinGroupWEOptional.isEmpty()) {
					logger.severe( "**** WOW  GOOD NO_GROUP_LINK_LEFT GROUP_LINK_ALREADY_CLICKED_EARLIER. BREAKING the while loop as groupLinks joinGroupWEOptional value not present. **   "+currentGroup);
					isAnyPendingGroupLinksRemainingToBeCovered = false;
		           return false;
				}

				//Click on single groupLink at a time
				WebElement joinGroupWE = joinGroupWEOptional.get();
				//.forEach(joinGroupWE -> {

							try {
								//The Group is about to be clicked And This group is a fresh group to join. Not present in alreadyJoinedGroups file
							String groupLinkToJoin = joinGroupWE.getText();
							logger.info("Trying to click to join "+groupLinkToJoin);

							// Validating if checking consecutive SAME_LINK in SAME_GROUP again and again
							if(previousGroupLink.equals(groupLinkToJoin) && previousGroup.equals(currentGroup) && sameGroupLinkInSameGroupCounter >=4) {
								logger.severe( "BREAKING as SAME_GROUP and SAME_LINK the while loop consecutively used inside same group: "+currentGroup +" groupLinkToJoin: "+groupLinkToJoin +" sameGroupLinkInSameGroupCounter :"+sameGroupLinkInSameGroupCounter);
								isAnyPendingGroupLinksRemainingToBeCovered = false;
								return true;//break;
							}else if(previousGroupLink.equals(groupLinkToJoin) && previousGroup.equals(currentGroup)){
								logger.info( "SAME_GROUP and SAME_LINK consecutively inside the while loop "+previousGroupLink +" sameGroupLinkInSameGroupCounter"+sameGroupLinkInSameGroupCounter);
								sameGroupLinkInSameGroupCounter++;
								continue;
							}else if(previousGroupLink.equals(groupLinkToJoin) && !previousGroup.equals(currentGroup)){
								logger.info( "SAME_LINK but not SAME_GROUP and consecutively inside the while loop "+previousGroupLink +" sameGroupLinkInSameGroupCounter"+sameGroupLinkInSameGroupCounter);
								continue;
							}else {
								logger.severe( "Perfect Case: NOT_SAME_GROUP and NOT_SAME_LINK. Update previousGroupLink by new to verify consecutive SAME_LINK "+previousGroupLink);
								previousGroupLink = groupLinkToJoin;
								previousGroup = currentGroup;
							}

							logger.fine( "JOIN_GROUP_LINK **joinGroupWE** fresh link : ***" + groupLinkToJoin);
							//Thread.sleep(1000);// remove them if slowing
							if(joinGroupClickAndHandleExcs(currentGroup, joinGroupWE) ==null) {//Click and verify
								alreadyJoinedGroupLinks.add(groupLinkToJoin);// add into the alreadyJoinedGroup and continue the parent while loop
								continue;
							}

							/*start----Group name or info validation for eligibility against neglect groups to join*/
							List<WebElement> groupJoinNameEls = driver.findElements(By.xpath(instance.getxPathInterface().getXPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON())).stream().filter(i->!i.getText().isEmpty()).collect(Collectors.toList());
							//"//span[@data-testid='group-join-modal-group-name']"
							groupJoinNameEls.stream().map(i -> i.getText().toString()).forEach(str->{
								logger.info( "JOIN_GROUP_NAME eligibility validation text if eligible : "+str);
							});

							boolean isGroupNameValidToJoin = groupJoinNameEls.stream().noneMatch(neglectGroupWL ->
								neglectToJoinGroupsWithTextSetLowerCase.stream().anyMatch(neglectStr->neglectGroupWL.getText().trim().toLowerCase().contains(neglectStr)));

							logger.info( "JOIN_GROUP_NAME text content valid to join avoiding neglectWords/AlreadyExistWords ?  ..."+isGroupNameValidToJoin);

							if (isGroupNameValidToJoin) {
							/*end --- Group name or Group info validation for eligibility against neglect groups to join*/
								logger.fine( "Hurray !! valid Group Name Found in the link to join : "+groupLinkToJoin);
									try {
										if(popUp_joinGroupOrCancelForOtherCases_all("","")){
											alreadyJoinedGroupLinks.add(groupLinkToJoin);
										 logger.severe( "FINALLY !!Join Group final clicked...text: "+groupLinkToJoin +" for group: "+currentGroup);
										}

									}catch(Exception ex) {
										logger.fine( String.format("Exception in joining new groupLink : ",getxPathInterface().getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN()+"//child::div"+" :::  "+alreadyJoinedGroupLinks));

										if(instance.getxPathInterface().getSocialType()==SocialType.TELEGRAM) {
											try {
												if(popUp_joinGroupOrCancelForOtherCases_all("//child::div","")) {
													alreadyJoinedGroupLinks.add(groupLinkToJoin);
													logger.fine( "join Group child div click...text: "+groupLinkToJoin);
												}

											}catch(Exception ex1) {
												logger.fine( String.format("Exception in joining new groupLink : ",getxPathInterface().getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN()+"//child::div"+" :::  "+alreadyJoinedGroupLinks));
												ex1.printStackTrace();
											}
										}else {
											throw ex;
										}
									}

									if(!alreadyJoinedGroupLinks.isEmpty()) {
										alreadyJoinedGroupLinks.add(groupLinkToJoin);
										logger.fine( "New Group added to alreadyJoinedGroups :"+alreadyJoinedGroupLinks);

										WhatsappContactUtils.exportAndMergeToExistingSingleFile(this.getALREADY_JOINED_GROUP_NAMES_STR(),alreadyJoinedGroupLinks);
										logger.fine( "New Group..exported: "+alreadyJoinedGroupLinks);
										Thread.sleep(2000);
									}else {
										logger.fine( "Sorry! New Group ..link was unable to be joined.So NOT_ADDED into the alreadyJoinedGroups");
									}
								try {
									if(instance.getxPathInterface().getSocialType()==SocialType.TELEGRAM) {
										febxsAndClick(EXACT_BACK_BUTTON,"",false,"the_exact_back_buton");//its same as back button line 295
										logger.fine( "Back Button Executed...");
									}
								}catch(Exception exc) {
									logger.fine( "exception : back-button...");
								}

								hasNewGroupJoinedButNotRestored[0] = true;
								try {
									if(instance.getxPathInterface().getSocialType()==SocialType.WHATSAPP) {
										clickOnCancelOrClose();
									}
								} catch (Exception ex) {
									logger.fine( "exception : Cancel if any error or long hold...");
									ex.printStackTrace();
								}
								logger.fine( "searchAndClickOnGroup to restore old group");
								hasNewGroupJoinedButNotRestored[0] = !searchAndClickOnGroup(currentGroup);

							} else {
								logger.fine( "JOIN_GROUP_NAME is INVALID to join So click: Close/Cancel/back");
								try {
									alreadyJoinedGroupLinks.add(groupLinkToJoin);
									febxsAndClick(instance.getxPathInterface().getBUTTON_CANCEL_CLICK_BACK(),"",false,"the_cancel_close_buton");
								}catch(Exception ex) {
									logger.fine( "Exception join Group final click: Cancel/back : "+ex.getMessage());
									hasNewGroupJoinedButNotRestored[0] =!searchAndClickOnGroup(currentGroup);
								}
							}
							}catch(Exception ex) {
								logger.fine( "Exception at the beginning of link join: : "+ex.getMessage());
								ex.printStackTrace();
								hasNewGroupJoinedButNotRestored[0] =!searchAndClickOnGroup(currentGroup);
							}
				}

				//});
			logger.info( "joinAllGroupsLinks whileCounter : "+whileCounter+ " joinGroupLinksPreviousCount :"+joinGroupLinksSize +" Difference groupLinks not eligible count: "+(joinGroupLinksSize-whileCounter));
			hasNewGroupJoinedButNotRestored[0] =!searchAndClickOnGroup(currentGroup);

		return hasNewGroupJoinedButNotRestored[0];
	}

	public Boolean joinGroupClickAndHandleExcs(String currentGroup, WebElement joinGroupWE) throws InterruptedException {
		try {
			joinGroupWE.click();
			if(!febxs(YOU_CANT_ACCESS_THIS_CHAT_NOT_A_MEMBER).isEmpty()) {
				return null;// add into the alreadyJoinedGroup and continue the parent while loop
			}
			Thread.sleep(1000);
			return true;
		}catch(Exception e) {
			try{
				logger.fine( "Exception in joinGroupWE.click() : "+e);
				Thread.sleep(2000);
				joinGroupWE.click();
				Thread.sleep(2000);
			}catch(Exception ex) {
				try {
				logger.fine( "Exception 2 layers in joinGroupWE.click() and search and clicking: "+ex);
				doSomeAwaking(currentGroup,3);
				}catch(ExceptionInInitializerError exx) {
					logger.fine( "Exception 3 layers in joinGroupWE.click() and search and clicking: "+ex);
					searchAndClickOnGroup(currentGroup);
				}
			}
		}
		return false;
	}


	private Set<String> extractGroupNamesForCurrView(Set<String> groupNames) {
		List<WebElement> webElements = driver.findElements(By.xpath(
				"//span[starts-with(text(),':')]/parent::span[1]//parent::div[1]//parent::div[1]/preceding-sibling::div[1]/child::div[1]/child::span[1]"));
		groupNames.addAll(webElements.stream().map(i -> i.getText()).collect(Collectors.toSet()));
		return groupNames;
	}

	private void joinGroup() {
		driver.findElements(By.xpath("//div[@data-testid='group-invite-link-action']")).parallelStream()
				.forEach(element -> element.click());
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
