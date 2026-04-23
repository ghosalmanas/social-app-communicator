package com.wtf.app.app.com.parent;

import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.enums.ActionIfTheGroupIsEligible;
import com.wtf.app.app.enums.EnumStringToExport;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.enums.SocialType;
import org.apache.tika.Tika;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.List;

public abstract class WhatsappParent extends WhatsappCommonUtils {

	public WhatsappParent(String baseURL, SocialType socialType) {
		super(baseURL,socialType);
		if(isRunningTestSuite()) {
			//update 3 file name values with runningTestSuiteApenderFunction
			}
	}


	@Override
	public void traverseGroupsTemplate(WhatsappCommonUtils instance)
			throws InterruptedException, AWTException, IOException {

		if(instance.getEnumFlowType() == FlowTypeEnum.SYNC_GROUP_NAMES_WITH_SENT_FOR_THE_DAY_FILE) {
			instance.takeActionIfTheGroupIsEligibleToProceed(instance, false, null, null, 0, 0);
			return;
		}

		if (instance.getEnumFlowType() == FlowTypeEnum.UN_ARCHIVE_SUPPORT_GROUP) {
			instance.takeActionIfTheGroupIsEligibleToProceed(instance, false, null, null, 0, 0);
			return;
		}

		logger.fine("Files to be Impacted : "+this.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA() +" , "+this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() +" , "+this.getALREADY_JOINED_GROUP_NAMES_STR());
		logger.fine("Files to be Impacted for Contants calls : "+Constants.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA() +" , "+Constants.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() +" , "+Constants.getALREADY_JOINED_GROUP_NAMES_STR());
		logger.fine("Files to be Impacted for Super: "+super.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA() +" , "+super.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() +" , "+super.getALREADY_JOINED_GROUP_NAMES_STR());
		logger.fine("Files to be Impacted for instance: "+instance.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA() +" , "+instance.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() +" , "+instance.getALREADY_JOINED_GROUP_NAMES_STR());

		//Always try to log with ENUM -EVENT and REASON as below
		if(instance.isRunningTestSuite() && (!instance.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA().contains(Constants._TEST_SUITE) && !instance.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG().contains(Constants._TEST_SUITE))) {
			logger.info( "TEST_SUITE_BREAKING  REASON__NO_TEST_SUITE present in file extension..."+this.getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA());
			return;
		}


		String pinnedGroup = null;

		if (instance.getEnumFlowType() == FlowTypeEnum.UN_ARCHIVE_SUPPORT_GROUP) {
			//Lets wait for the pinned element to kick off
			pinnedGroup = waitIdentifyAndClickOnThePinnedElement(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE());
		}else {
			pinnedGroup = waitIdentifyAndClickOnThePinnedElement(getxPathInterface().getWA_PINNED_SPAN_TITLE()); //MG Communications
		}
		pinnedGroup ="MG Communications";//Hard-coded
		logger.info("PINNED_GROUP_RETRIVED :"+pinnedGroup);


		//Mainly useful for traversal and message posting
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        WebElement leftChatPanelContainerWithScroll = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN())));


        /* NOTE: URGENT search with //* and Move all the hardcoded xpath into xpaths files, else TG works and WA fails or vice versa*/

		// Traversal with Robot class each and every groups and chats

		Properties properties = getConfigProperties();

		Set<String> allPhoneContacts = new TreeSet<>();
		Set<String> alreadyTraversedGroupsOnly = instance.getAlreadyTraversedGroupsOnlyAndUpdateDate();

		Set<String> alreadyTraversedGroupsAndIndividuals = new TreeSet<>();
		Set<String> invalidGroupNames = new TreeSet<>();
		Set<String> setOfDataToExport = new TreeSet<>();


		boolean waitForLoadingAllChatsForGroupJoiningDuringTraversal = Boolean.parseBoolean(properties.getProperty("wa.waitForLoadingAllChatsForGroupJoiningDuringTraversal", "false"));
		String filePathToExportContactsData = instance.getFilePathToExportContactsData();
		Tika tika = new Tika();

		int robotExecutionCounter = 0;
		int prevCounter = 0;
		int counter = 0;
		int repeatCounter = 0;
		int continuousAlreadyTraversedStoredCounter = 0;
		int continuousAlreadyPresentDiffGroupAndIndCounter = 0;
		int continuousSameGroupRepeatCounter = 0;


		String lastVisitedGroup = "";
		String lastVisitedButNonSentGroupPrevToSuccessToMoveBack = "";
		String lastPerfect1stTimeVisitedGroup = "";
		//Stack<String> stackOfAlreadyVisitedGroups= new Stack<String>();

		boolean hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = false;
		boolean success_takeActionIfTheGroupIsEligibleToProceed = false;

		boolean hasMoreElementsToTraverse = true;
		int countAlreadyExists = 0;
		String groupNameExceptIndvForLastVisitedTracker = "";
		boolean robotFasteningOccurred = false;
		int processCompletedSuccessfullyCounter = 0;
		int howManyFreshGroupTraversedCounter=0;
		String localDateTime = getLocalTimeInFormat.get();
		boolean clearAlreadyTraversedSetInRuntime =false;

		/*Predicate<String> predicateCollectGroupNamesOrGroupMustBeInSupportList = groupNameExceptIndvid -> ifSuccessfulGroupsContainsCheckRequired(
				instance, groupNameExceptIndvid);*/
		// instance.conditionValidationForGroupContains(groupNameExceptIndvid); //delete whenever cleaning

		logger.fine( "Before Traversal: alreadyTraversedGroupsOnly : "
				+ String.join(",", alreadyTraversedGroupsOnly));

		logger.fine(
				"Before Traversal: isAlreadyUpdatedThePersonalGroupAndContactsForTheDay : "
						+ isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA + " instance.getEnumFlowType() : "
						+ instance.getEnumFlowType().name());

		logger.info("WaitForLoadingAllChatsForGroupJoiningDuringTraversal : "+waitForLoadingAllChatsForGroupJoiningDuringTraversal +" instance.getSocialType() : "+instance.getxPathInterface().getSocialType().name() +" instance.getEnumFlowType() : "+instance.getEnumFlowType().name());
		//Wait for the chats to load
		if (waitForLoadingAllChatsForGroupJoiningDuringTraversal && instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP && instance.getEnumFlowType() == FlowTypeEnum.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS) {
			int chat_download_millis = 60000 * 10;
			logger.severe("SLEEPING for "+chat_download_millis+"_MILLI_SECONDS before traversal so that the chats are DOWNLOADED to make the GROUP LINKS to JOIN instead of incomplete work...");
			Thread.sleep(chat_download_millis);//Adjust as per the situation
			logger.severe("Ignoring & clearing alreadyTraversedGroupsOnly from file for fresh traversal each time...");
			alreadyTraversedGroupsOnly.clear();
		}


		while (hasMoreElementsToTraverse) {

			localDateTime = getLocalTimeInFormat.get(); //update time for each execution

			logger.info("\n\n");
			logger.severe("Iteration Started...Date-Time: " + localDateTime + " FreshGroupTraversed : " + howManyFreshGroupTraversedCounter + " counter:" + counter + " lastVisitedGroup:" + lastVisitedGroup + " lastVisitedButNonSentGroupPrevToSuccessToMoveBack :" + lastVisitedButNonSentGroupPrevToSuccessToMoveBack);

			/* Test Suite Specific */
			if (instance.isRunningTestSuite() && howManyFreshGroupTraversedCounter == instance.getNumOfGroupsLimitTestSuite()) {
				logger.info("BREAKING AS TEST_SUITE limit reached..." + instance.getNumOfGroupsLimitTestSuite());
				return;
			}

			if (howManyFreshGroupTraversedCounter != 0 && howManyFreshGroupTraversedCounter % 10 == 0) {
				//Clear the alreadyTraversedGroups in runtime by property injection
				if(!clearAlreadyTraversedSetInRuntime) {
					properties = getConfigProperties();
					clearAlreadyTraversedSetInRuntime = Boolean.parseBoolean(properties.getProperty("wa.clear_the_already_traversed_set", "false"));
					if(clearAlreadyTraversedSetInRuntime){
						alreadyTraversedGroupsOnly.clear();
						logger.info("AlreadyTraversedGroupsOnly is cleared by injection in GetConfigProperties clearAlreadyTraversedSetInRuntime :  " + clearAlreadyTraversedSetInRuntime);
					}

				}
				//Scroll down after certain number of groups traversal
				jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];", leftChatPanelContainerWithScroll, 200);
			}
			//Restore to previous group if needed
			if (hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious && (counter >=10 || !driver.findElements(By.xpath("//*[text()='"+pinnedGroup+"']")).isEmpty()) /*checking if the group pushed at the top*/){
				String lastVisitedEligibleRestoreGroup = (!lastVisitedButNonSentGroupPrevToSuccessToMoveBack.isEmpty() && !lastVisitedButNonSentGroupPrevToSuccessToMoveBack.equalsIgnoreCase(pinnedGroup)) ? lastVisitedButNonSentGroupPrevToSuccessToMoveBack : lastVisitedGroup;
				logger.fine("searchAndClickOnLastVisitedGroup ? counter : " + counter + " hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious : " + hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious+" lastVisitedEligibleRestoreGroup : "+lastVisitedEligibleRestoreGroup);
				hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = instance.searchAndClickOnLastVisitedGroup(counter, hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious, lastVisitedEligibleRestoreGroup);
			}

			try {
				// ROBOT CLASS
				if (instance.getEnumFlowType() != FlowTypeEnum.ARCHIVE_SUPPORT_GROUP) {
					if (continuousAlreadyPresentDiffGroupAndIndCounter == 40) {
						// Achieve basic blockers and try to go to-fro to refresh the page
						removeBlockersStuckAndRefreshToMoveToNextChat(lastVisitedGroup);

					} else {
						runRobotClassForNextChat();
					}
				}

				// ROBOT CLASS
				if (instance.getEnumFlowType() != FlowTypeEnum.ARCHIVE_SUPPORT_GROUP) {
					/*
					 * if (getxPathInterface().isSocialTypeWhatsApp()) { if (continuousNextCounter %
					 * 3 == 0) { robotFasteningOccurred = true; runRobotClassForNextChat(500,
					 * robotFasteningOccurred);// fastening causing not loading the chat for //
					 * telegram } } else if (robotFasteningOccurred && continuousNextCounter == 1)
					 */
					if (true/*getxPathInterface().getSocialType() == SocialType.WHATSAPP*/) {
						if (continuousSameGroupRepeatCounter > 0 && continuousSameGroupRepeatCounter % 5 == 0) {
							Thread.sleep(1000);
							logger.fine( "continuousSameGroupRepeatCounter :::: "+continuousSameGroupRepeatCounter);
							doSomeAwaking(lastVisitedGroup, continuousSameGroupRepeatCounter/5);
							continuousSameGroupRepeatCounter=0;//
							Thread.sleep(1000);
						}

						if ((continuousAlreadyPresentDiffGroupAndIndCounter % 10==0 || continuousAlreadyTraversedStoredCounter% 10==0) && continuousSameGroupRepeatCounter<2 && getSLEEP_TIME_MS()>400) {
							robotFasteningOccurred = true;
							setSLEEP_TIME_MS(getSLEEP_TIME_MS()-200);
						}
						/*
						 * if(continuousNextCounter< 10) { setSLEEP_TIME_MS(getSLEEP_TIME_MS_RESET());
						 * robotFasteningOccurred = false; }
						 */

						logger.info( "*------ ContinuousSameGroupNextCounter::"+continuousSameGroupRepeatCounter+" ContinuousNextCounter :: "+continuousAlreadyPresentDiffGroupAndIndCounter+" robotFasteningOccurred::"+robotFasteningOccurred);
					}

					//robotFasteningOccurred = false;
					//runRobotClassForNextChat(0, robotFasteningOccurred); //enable

				}

				String findRightTopGroupName = decideGroupOrIndvUserAnnouncement_and_findRightTopWebElementForGroupOnly(lastVisitedGroup, instance.getEnumFlowType());
				//Individual User or invalid cases for Admins
				if(findRightTopGroupName==null) {
					continue;
				}
				findRightTopGroupName = getTopGroupNameInUtf8(tika, findRightTopGroupName);

				String currentGroupNameExceptIndv = findRightTopGroupName;

				if (currentGroupNameExceptIndv.equals(lastVisitedGroup)) {
					continuousSameGroupRepeatCounter = continuousSameGroupRepeatCounter + 1;
					logger.info("SAME_GROUP_REPEATED_"+currentGroupNameExceptIndv+" pinnedGroup: "+pinnedGroup);
				}else if(!lastVisitedGroup.isEmpty() && currentGroupNameExceptIndv.trim().equals(pinnedGroup.trim())) {
					logger.info("PINNED_GROUP_TRAVERSE_FOUND_"+pinnedGroup+" MOVING_TO PREVIOUS_1ST_TIME_VISITED_GROUP_"+lastVisitedGroup);
					searchAndClickOnGroup(lastPerfect1stTimeVisitedGroup);
					continuousSameGroupRepeatCounter = continuousSameGroupRepeatCounter + 1;
				}else {
					logger.info("PROPER_NEXT_EXECUTION_FOUND_"+currentGroupNameExceptIndv+" PREVIOUS_GROUP_WAS_"+lastVisitedGroup +" pinnedGroup: "+pinnedGroup);
					continuousSameGroupRepeatCounter = 0;
				}

				// ROBOT CLASS for ARCHIVE_SUPPORT_GROUP
				if (instance.getEnumFlowType() == FlowTypeEnum.ARCHIVE_SUPPORT_GROUP) {

					if (!success_takeActionIfTheGroupIsEligibleToProceed
							|| /* robotExecutionCounter%2==0 || */ currentGroupNameExceptIndv
									.equals(groupNameExceptIndvForLastVisitedTracker)) {
						runRobotClassForNextChat();
						Thread.sleep(2000);
					}

					robotExecutionCounter = robotExecutionCounter + 1;
					alreadyTraversedGroupsOnly.clear();
					alreadyTraversedGroupsAndIndividuals.clear();

					if (!neglectToJoinGroupsWithTextSetLowerCase.isEmpty()) {
						neglectToJoinGroupsWithTextSetLowerCase.clear();
					}

				}


				logger.fine(
						"Start inside While : lastVisitedGroup :" + lastVisitedGroup + " countAlreadyExists :"
								+ countAlreadyExists + " has/MoreElementsToTraverse: " + hasMoreElementsToTraverse
								+ " alreadyTraversedGroupsOnly Size : " + alreadyTraversedGroupsOnly.size());

				String groupInfo = null;
				lastVisitedGroup = groupNameExceptIndvForLastVisitedTracker;

				groupNameExceptIndvForLastVisitedTracker = currentGroupNameExceptIndv;
				logger.fine(
						"lastVisitedGroup: " + lastVisitedGroup + " groupNameExceptIndv :" + currentGroupNameExceptIndv);

				if(alreadyTraversedGroupsOnly.contains(currentGroupNameExceptIndv)) {
					continuousAlreadyTraversedStoredCounter=continuousAlreadyTraversedStoredCounter+1;
				}else {continuousAlreadyTraversedStoredCounter=0;
				}

				// Handle continuous next try on last element : start
				if (alreadyTraversedGroupsAndIndividuals.contains(currentGroupNameExceptIndv)) {

					Map<String, Object> prevValRepeatLimit = checkIfPrevCurrGroupSameOrRepeatExhausted(instance,
							currentGroupNameExceptIndv, lastVisitedGroup, hasMoreElementsToTraverse, countAlreadyExists);
					hasMoreElementsToTraverse = (boolean) prevValRepeatLimit.get("hasMoreElementsToTraverse");
					countAlreadyExists = (Integer) prevValRepeatLimit.get("countAlreadyExists");
					continuousAlreadyPresentDiffGroupAndIndCounter = continuousAlreadyPresentDiffGroupAndIndCounter + 1;

					logger.fine(
							"Check Already Exists:: Continuous next targetted for first/last group : groupNameExceptIndv :"
									+ currentGroupNameExceptIndv + " countAlreadyExists: " + countAlreadyExists
									+ " hasMoreElementsToTraverse: " + hasMoreElementsToTraverse);
					String reason = "ALREDY_TRAVERSED_FOR_THE_DAY";
					logger.info("Eligibility Failed......Reason : ***** "+reason+" *****");
					logger.fine("Current Group "+currentGroupNameExceptIndv+ " has been "+reason+" ::Present in the TraversedList: \n"+alreadyTraversedGroupsOnly);
					lastVisitedButNonSentGroupPrevToSuccessToMoveBack = currentGroupNameExceptIndv;

					continue;

				} else {
					alreadyTraversedGroupsAndIndividuals.add(currentGroupNameExceptIndv);
					counter++;
					continuousAlreadyPresentDiffGroupAndIndCounter = 0;
					setSLEEP_TIME_MS(getSLEEP_TIME_MS_RESET());
					robotFasteningOccurred = false;
				}


				// Handle continuous next try on last element : end
				List<WebElement> elemGroupInfoOrPhOrAnnouncements = driver
						.findElements(By.xpath(getxPathInterface()
								.getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT()));

				if (elemGroupInfoOrPhOrAnnouncements.isEmpty()) {
					logger.fine( "webElement.isEmpty::: " + currentGroupNameExceptIndv);
					continue;
				} else {

					groupInfo = elemGroupInfoOrPhOrAnnouncements.getFirst().getText();

					logger.fine(
							"is groupInfo a group?: " + instance.getxPathInterface().getGroupInfo().test(groupInfo)
									+ " alreadyTraversedGroupsOnly :" + alreadyTraversedGroupsOnly);

					if (instance.getxPathInterface().getGroupInfo().test(groupInfo)) {
												// Start: Add functionality to avoid newly added/created personal groups or
						// personal contact
						//if (isRequiredToUpdatePersonalGroupsAndContacts(instance)) {
							// updateExludePersonalContactListFile(groupNameExceptIndv);//todo:
							// uncomment/work
						//}
						// End

						//printLogEligibilityValidationResult(alreadyTraversedGroupsOnly,predicateCollectGroupNamesOrGroupMustBeInSupportList, groupNameExceptIndv);

						if (validateTheEligibility(alreadyTraversedGroupsOnly, currentGroupNameExceptIndv)) {
							logger.info("============================Start=======================================");
							logger.info(
									"-----*--- ELIGIBLE ---*---- Group present in SupportGroup and Not Present in Exclusion/neglect group list : "
											+ currentGroupNameExceptIndv + " not present in alreadyTraversedGroupsOnly:"
											+ alreadyTraversedGroupsOnly);

							// if (!alreadyTraversedGroupsOnly.contains(groupNameExceptIndv))

								/*----------Actual Task for each group for FB, WA, TG--------------------*/
								Map<ActionIfTheGroupIsEligible, Object> result = instance
										.takeActionIfTheGroupIsEligibleToProceed(instance,
												hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious, currentGroupNameExceptIndv,
												allPhoneContacts, counter, repeatCounter);

								success_takeActionIfTheGroupIsEligibleToProceed = (boolean) result
										.get(ActionIfTheGroupIsEligible.FLAG_TRUE);
								hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious = success_takeActionIfTheGroupIsEligibleToProceed;
								// groupNameExceptIndv = (String)
								// result.get(ActionIfTheGroupIsEligible.GROUP_NAME);
								allPhoneContacts = (Set<String>) result.get(ActionIfTheGroupIsEligible.SET_OF_DATA);
								counter = (int) result.get(ActionIfTheGroupIsEligible.COUNTER);
								repeatCounter = (int) result.get(ActionIfTheGroupIsEligible.REPEAT_COUNTER);
								logger.fine(
										"After Retrival :: hasNewGroupJoinedButNotRestored : "
												+ hasNewGroupJoinedOrMsgSentButNotRestoredToPrevious + " allPhoneContacts size: "
												+ allPhoneContacts.size() + " counter: " + counter + " repeatCounter: "
												+ repeatCounter);
								logger.fine(
										"Meet criteria checks : " + currentGroupNameExceptIndv + " counter: " + counter
												+ " prevCounter: " + prevCounter + " alreadyTraversedGroupsOnly.size "
												+ alreadyTraversedGroupsOnly.size());
								alreadyTraversedGroupsOnly.add(currentGroupNameExceptIndv);
								logger.fine( "Eligible Group : " + currentGroupNameExceptIndv
										+ " added in alreadyTraversedGroupsOnly");
								logger.info("========================End===========================================");

								countAlreadyExists = 0;
								howManyFreshGroupTraversedCounter++;
								lastPerfect1stTimeVisitedGroup = currentGroupNameExceptIndv;

						} else {
							lastVisitedButNonSentGroupPrevToSuccessToMoveBack = currentGroupNameExceptIndv;
							logger.severe(
									"Oh No! this Group does not meet eligibility criteria checks : " + currentGroupNameExceptIndv + " counter: " + counter
											+ " prevCounter: " + prevCounter + " alreadyTraversedGroupsOnly.size "
											+ alreadyTraversedGroupsOnly.size());
						}

						logger.info("Data Export Eligible? alreadyTraversedGroupsOnly has data: "+ !alreadyTraversedGroupsOnly.isEmpty()+" filePathToExportContactsData: "+filePathToExportContactsData +" ExpectedSizeToExport: "+ instance.getExpectedSizeToModuloToInsertExportToFile());

						if (!alreadyTraversedGroupsOnly.isEmpty() && filePathToExportContactsData!=null && !filePathToExportContactsData.isEmpty() && alreadyTraversedGroupsOnly.size()
								% instance.getExpectedSizeToModuloToInsertExportToFile() == 0) {

							logger.fine("alreadyTraversedGroups after "+instance.getExpectedSizeToModuloToInsertExportToFile()+" occurances : " + alreadyTraversedGroupsOnly.toString());

							Map<EnumStringToExport, Set<String>> exportData = getHashTableToExportData(allPhoneContacts,
									alreadyTraversedGroupsOnly, invalidGroupNames, setOfDataToExport);
							logger.fine( "fileNameToExportData : " + filePathToExportContactsData+ " exportDataToFile : " + alreadyTraversedGroupsOnly.toString());

							instance.exportDataToFile(filePathToExportContactsData, exportData);
						}
					}
				}

			} catch (Exception ex) {
				logger.fine( "Exception occured in catch :" + ex.getLocalizedMessage());
				ex.printStackTrace();
				continue;
			}
		}


		if ((alreadyTraversedGroupsOnly == null || alreadyTraversedGroupsOnly.isEmpty())
				&& (allPhoneContacts == null || allPhoneContacts.isEmpty())) {
			logger.fine( "Fail: No Data Found alreadyTraversedGroups : " + alreadyTraversedGroupsOnly
					+ "allPhoneContacts : " + allPhoneContacts);
			return;
		}

		allPhoneContacts.add("Execution Completed..........." + localDateTime);
		alreadyTraversedGroupsOnly.add("Execution Completed..........." + localDateTime);
		logger.fine( "Success: alreadyTraversedGroups : " + alreadyTraversedGroupsOnly
				+ " allPhoneContacts : " + allPhoneContacts.size());

		// Template4
		// WhatsappContactUtils.exportToFile(SUPPORT_EXTRACTED_CONTACTS,
		// allPhoneContacts, groupNames, groupNamesInvalid);
		Map<EnumStringToExport, Set<String>> exportData = getHashTableToExportData(allPhoneContacts,
				alreadyTraversedGroupsOnly, invalidGroupNames, setOfDataToExport);

		instance.exportDataToFile(filePathToExportContactsData, exportData);

		logger.fine(
				"Success: isAlreadyUpdatedThePersonalGroupAndContactsForTheDay : "
						+ isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA + " instance.getEnumFlowType() : "
						+ instance.getEnumFlowType().name());

		// todo:instance.updateCurrDateAndReloadFile with impl and empty for others
		/*
		 * if(isRequiredToUpdatePersonalGroupsAndContacts(instance)) {
		 * clearUpdateCurrDateAndReloadFile(excludePersonalGroupContactNamesFromCsv,
		 * "group_traverse_already_date_",excludePersonalGroupContactNamesOrNumbersSet,
		 * false); isAlreadyUpdatedThePersonalGroupAndContactsForTheDay=true; }
		 */
		logger.fine(
				"Process Completed Successfully instance.getEnumFlowType() : " + instance.getEnumFlowType().name());

		if (processCompletedSuccessfullyCounter < 5) {
			traverseGroupsTemplate(instance);
		}

		if(!instance.isRunningTestSuite()) {
			driver.close();
		}

	}

	private static String getTopGroupNameInUtf8(Tika tika, String findRightTopGroupName) {
		// Use Tika to detect encoding
		String encoding = tika.detect(findRightTopGroupName);
		if(!"UTF-8".equalsIgnoreCase(encoding) && !"application/octet-stream".equalsIgnoreCase(encoding)) {
			logger.severe("Detected Encoding: " + encoding +"\n GroupName :"+ findRightTopGroupName); //
			findRightTopGroupName = new String(findRightTopGroupName.getBytes(StandardCharsets.ISO_8859_1)/*, StandardCharsets.UTF_8*/);//uncomment if any issue
			logger.severe("ISO_8859_1 Decoded Text: " + findRightTopGroupName);
			//findRightTopGroupName = removeJunkCharactersIfExists(findRightTopGroupName);
		}
		return findRightTopGroupName;
	}

	public String decideGroupOrIndvUserAnnouncement_and_findRightTopWebElementForGroupOnly(String lastVisitedGroup, FlowTypeEnum flowTypeEnum) throws InterruptedException {

		boolean isAnyExceptionOccurrred = false;
		String perfectGroupName=null;
		String decideAGroup = null;
		try {
			/* Remember that code change is better than xPath change*/
			/* Allow admins for WA for Contact extractions.. Write all Use cases for clarity and avoid blocking any use case*/
			/*
			 * Use Cases:
			 * 1.Message Posting: Allow Only Groups. Not admins, Subscribers and individual users
			 * 2.Traverse and contacts extractions and join new groups: Allow Groups, admins and subscribers as well. Not user.
			 * 3.Archive: Allow Groups and Admins for WA
			 * 4.UnArchieve: All
			 * 5.Sync Group Names: Possible/applicable only for Message posting case 1. so admins not required
			 *   --- So we must need to have Groups and Admins. No use of individual user in ay case
			 */

			//Thread.sleep(1000);//safe guard to populate the final sub-header like ph Nos but NA

			//Retrieve only group case and Decide if a Group or an individual user or an admin group for WA
			List<WebElement> subHeaderToDecideAGroups = febxs(
					getxPathInterface().getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT());

			//Includes Perfect group and Admins as well
			List<WebElement> groupNames = febxs(
					getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP());//All for WA but only members and Subscribers for TG, not user

			//It it an admin group
			boolean isItsAnAdminGroup = !febxs(
					getxPathInterface().getFIND_ADMIN_GROUP_NAME_RIGHT_TOP()).isEmpty();//All for WA but only members and Subscribers for TG, not user

			//check if a group with subscribers in TG
			if(!subHeaderToDecideAGroups.isEmpty()){
						perfectGroupName = subHeaderToDecideAGroups.getFirst().getText();
					}


			//Cover the explicit cases first like user and then admin and return.

			//individual users. May be for announcement as well
			if(subHeaderToDecideAGroups.isEmpty() || subHeaderToDecideAGroups.getFirst().getText().contains("contact info") || subHeaderToDecideAGroups.getFirst().getText().contains("last seen") || subHeaderToDecideAGroups.getFirst().getText().equals("online")) {
				List<WebElement> individualUsers = febxs(
						getxPathInterface().getFIND_INDV_USER_NAME_RIGHT_TOP());
				if(individualUsers.isEmpty()) {
					logger.severe("URGENT_INTERVENTION_REQUIRED: ERROR in PAGE_FOCUS OR PAGE_LOAD");//null for TG.NA
					doSomeAwaking(lastVisitedGroup, 4);//Simply search and reload
					return null;
				}
				String individualGroupName = individualUsers.isEmpty()? null : individualUsers.getFirst().getText();//Optional. Not required
				logger.info("Skip Individual Users :: "+individualGroupName);//null for TG.NA
				return null;
			}

			//Traverse and contacts extractions: Allows admins
			if( isItsAnAdminGroup && !groupNames.isEmpty()) {
				 String adminGroupName = groupNames.getFirst().getText();
				if(flowTypeEnum == FlowTypeEnum.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS || flowTypeEnum == FlowTypeEnum.ARCHIVE_SUPPORT_GROUP || flowTypeEnum == FlowTypeEnum.UN_ARCHIVE_SUPPORT_GROUP) {
					logger.info("Allowing Admin Group for Archive and Traversal :: "+adminGroupName);//null for TG.NA
					return adminGroupName;
				}else {
					//Skip the Admin for rest of the cases
					logger.info("Skipping Admin Group for Posting Msg, SyncGroupNames :: "+adminGroupName);//null for TG.NA
					return null;
				}
			}

			//in immediate time, individual users also has a value like 'Business User' or 'Click Here to see contact info' and after a second its empty means
			//whereas Group should have either 'Click here to see Group info' or comma separated contacts

			//Now Finally its turn for the Actual and Perfect Group name
			if(!subHeaderToDecideAGroups.isEmpty() && !groupNames.isEmpty()) {
				 decideAGroup = subHeaderToDecideAGroups.iterator().next().getText();
					if(decideAGroup!=null && decideAGroup.contains("group info") || decideAGroup.contains(",") || decideAGroup.contains("members")) {
						perfectGroupName = groupNames.getFirst().getText();
						logger.info("Perfect eligible group name retrived :: "+perfectGroupName);
						return perfectGroupName;
					}
			}

			logger.severe("Something is wrong retrieving the group name :: "+perfectGroupName +" subHeaderToDecideAGroups is empty? :"+subHeaderToDecideAGroups.isEmpty()+" isItsAnAdminGroup : "+isItsAnAdminGroup+" groupnames is empty? "+groupNames.isEmpty());

			//Worst case if a group but some discrepancy.So to invoke the exceptions, print traces and handling
			 perfectGroupName = febx(
					getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP()).getText();

			 return perfectGroupName;


		} catch (MoveTargetOutOfBoundsException ex) {
			logger.severe( "Exception retrieving top right header MoveTargetOutOfBoundsException : "
					+ ex.getLocalizedMessage());
			isAnyExceptionOccurrred=true;
		}catch (StaleElementReferenceException ex) {
			logger.severe( "Exception retrieving top right header StaleElementReferenceException : "
					+ ex.getLocalizedMessage());
			isAnyExceptionOccurrred=true;
		}catch(NoSuchElementException nse) {
			logger.severe( "Exception retrieving top right header NSE : "
					+ nse.getLocalizedMessage());
			isAnyExceptionOccurrred=true;
		}
		catch(NoSuchSessionException nss) {
			logger.severe( "Exception retrieving top right header NoSuchSessionException: " + nss.getLocalizedMessage());
			isAnyExceptionOccurrred=true;
			doSomeAwaking(null, 5);
		}
		catch (Exception ex) {
			logger.severe( "Exception retrieving top right header: " + ex.getLocalizedMessage());
			isAnyExceptionOccurrred=true;
		}
		finally {
			if(isAnyExceptionOccurrred) {
				doSomeAwaking("",3);
				List<WebElement> groupOrIndvEls = febxs(getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP());
				if(!groupOrIndvEls.isEmpty()) {
					return groupOrIndvEls.getFirst().getText();
				}
			}
		}
		return null;
	}



	boolean validateTheEligibility(Set<String> alreadyTraversedGroupsOnly, String groupNameExceptIndv) {
		boolean neverTraversed = alreadyTraversedGroupsOnly.stream()
						.noneMatch(sentGroup -> sentGroup.trim().toLowerCase()
								.contains(groupNameExceptIndv.toLowerCase())
								|| groupNameExceptIndv.trim().toLowerCase()
										.contains(sentGroup.trim().toLowerCase()));
		boolean notPresentInPersonalGroupList = excludePersonalGroupNamesSet.stream()
				.noneMatch(sentGroup -> sentGroup.trim().toLowerCase()
						.contains(groupNameExceptIndv.toLowerCase())
						|| groupNameExceptIndv.trim().toLowerCase()
								.contains(sentGroup.trim().toLowerCase()));
		boolean notPresentInExclusionGroupList = excludeSupportGroupNamesSet.stream()
				.noneMatch(sentGroup -> sentGroup.trim().toLowerCase()
						.contains(groupNameExceptIndv.toLowerCase())
						|| groupNameExceptIndv.trim().toLowerCase()
								.contains(sentGroup.trim().toLowerCase()));
		boolean notPresentInNeglectToJoinGroupList = neglectToJoinGroupsWithTextSetLowerCase.stream()
				.noneMatch(sentGroup -> groupNameExceptIndv.trim().toLowerCase()
						.contains(sentGroup.trim().toLowerCase()));
		logger.fine("Eligibility to Process started... All must be true :::: neverTraversed: "+neverTraversed+" notPresentInPersonalGroupList: "+notPresentInPersonalGroupList+" notPresentInExclusionGroupList: "+notPresentInExclusionGroupList+ " notPresentInNeglectToJoinGroupList: "+notPresentInNeglectToJoinGroupList);



		boolean isEligibleToShootToEachGroup = /*predicateCollectGroupNamesOrGroupMustBeInSupportList.test(groupNameExceptIndv)*///fixed false
				neverTraversed
				&& notPresentInPersonalGroupList
				&& notPresentInExclusionGroupList
				&& notPresentInNeglectToJoinGroupList;
		String reason ="";
		if(!isEligibleToShootToEachGroup) {
			if(!neverTraversed) {
				reason = "ALREDY_TRAVERSED_FOR_THE_DAY";
				logger.fine("Current Group "+groupNameExceptIndv+ " has been "+reason+" ::Present in the TraversedList: \n"+alreadyTraversedGroupsOnly);
			}
			if(!notPresentInPersonalGroupList) {
				reason = "ALREDY_PRESENT_IN_PERSONAL_GROUP_LIST";
			}
			if(!notPresentInExclusionGroupList) {
				reason = "ALREDY_PRESENT_IN_EXCLUSION_GROUP_LIST";
			}
			if(!notPresentInNeglectToJoinGroupList) {
				reason = "ALREDY_PRESENT_IN_NEGLECT_TO_JOIN_GROUP_LIST";
			}

			logger.info("Eligibility Failed......Reason : ***** "+reason+" *****");
		}

		return isEligibleToShootToEachGroup;
	}

	protected void removeBlockersStuckAndRefreshToMoveToNextChat(String lastVisitedGroup) {
		logger.fine( "removeBlockersStuckAndRefreshToMoveToNextChat invoked....................");
		CustomFunction func = () -> febx(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());
		if (runTC(func).isPresent()) {
			((WebElement) runTC(func).get()).click();
		} else {
			searchAndClickOnGroup(lastVisitedGroup);
		}
	}

	void printLogEligibilityValidationResult(Set<String> alreadyTraversedGroupsOnly, String groupNameExceptIndv) {

		logger.fine( "Eligibility Checks : "
				//+ predicateCollectGroupNamesOrGroupMustBeInSupportList.test(groupNameExceptIndv)
				+ " alreadyTraversedGroupsOnly value: "
				+ alreadyTraversedGroupsOnly.stream().noneMatch(
						sentGroup -> sentGroup.trim().toLowerCase().contains(groupNameExceptIndv.toLowerCase())
								|| groupNameExceptIndv.trim().toLowerCase().contains(sentGroup.trim().toLowerCase()))
				+ " excludePersonalGroupNamesSet value: "
				+ excludePersonalGroupNamesSet.stream().noneMatch(
						sentGroup -> sentGroup.trim().toLowerCase().contains(groupNameExceptIndv.toLowerCase())
								|| groupNameExceptIndv.trim().toLowerCase().contains(sentGroup.trim().toLowerCase()))
				+ " neglectToJoinGroupsWithTextSetLowerCase value "
				+ neglectToJoinGroupsWithTextSetLowerCase.stream().noneMatch(sentGroup -> groupNameExceptIndv.trim()
						.toLowerCase().contains(sentGroup.trim().toLowerCase())));

		logger.fine( "Eligibility Checks : final "
				+ validateTheEligibility(alreadyTraversedGroupsOnly,
						groupNameExceptIndv));
	}

	void executeWindowFunction() throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("document.body.style.zoom='50%'"); // working
		Thread.sleep(3000);

		logger.fine( " down 50% executed");

	}

}
