package com.wtf.app.app.commons;

import com.wtf.app.app.enums.ActionIfTheGroupIsEligible;
import com.wtf.app.app.enums.EnumStringToExport;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public abstract class WhatsappCommonUtils extends SocialCommonUtils {

	private static int SLEEP_TIME_MS = 2000;
	private static int SLEEP_TIME_MS_RESET = SLEEP_TIME_MS;
	private static final String BACK_OR_TITLE_BACK_IF_DUPLICATES = "//*[@aria-label='Back' or @title='Back']";
	protected static final String EXACT_BACK_BUTTON = "//div[@class='back-button']/button[@title='Back']";

	private XPathInterfaceWATG xPathInterface;
	private String baseUrl;
	//private SocialType socialType;
	private String messgeToSearch;
	/* Test Suite Information*/
	private boolean runningTestSuite;
	private int numOfGroupsLimitTestSuite;
	private boolean traverseDependsOnAllSuccessGroupList;
	private FlowTypeEnum enumFlowType;
	protected boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = groupsAlreadyMessageSentForTheDaySetWA
			.contains("group_traverse_already_date_" + LocalDate.now())
			|| groupsAlreadyMessageSentForTheDaySetWA
					.contains("group_traverse_already_date_" + LocalDate.now().minusDays(1l)) || groupsAlreadyMessageSentForTheDaySetWA
							.contains("group_traverse_already_date_" + LocalDate.now().minusDays(2l));
	// group_traverse_already_date_2023-12-29
	private boolean isRequiredToUpdatePersonalGroupsAndContacts = false;

	protected WhatsappCommonUtils(String baseURL, SocialType socialType) {
		super(baseURL, socialType);
	}

	protected boolean ifSuccessfulGroupsContainsCheckRequired(WhatsappCommonUtils instance,
			String groupNameExceptIndvid) {
		return !instance.isSuccessfulSupportGroupContainsCheckRequired()
				|| (instance.isSuccessfulSupportGroupContainsCheckRequired()
						&& allSuccessfulSupportGroupsFromFile.stream().anyMatch(
								group -> (group.trim().toLowerCase().contains(groupNameExceptIndvid.toLowerCase())
										|| groupNameExceptIndvid.toLowerCase().contains(group.trim().toLowerCase()))));
	}

	protected abstract boolean isSuccessfulSupportGroupContainsCheckRequired();

	protected Map<String, Object> checkIfPrevCurrGroupSameOrRepeatExhausted(WhatsappCommonUtils instance,
			String groupNameExceptIndv, String lastVisitedGroup, boolean hasMoreElementsToTraverse,
			int countAlreadyExists) {

		if (countAlreadyExists >= instance.getMaxAllowedRepeatOfSameGroup()) {
			hasMoreElementsToTraverse = false;
			logger.fine( "countAlreadyExists exceeded : " + instance.getMaxAllowedRepeatOfSameGroup()
					+ " groupNameExceptIndv : " + groupNameExceptIndv);

		} else {
			if (lastVisitedGroup.equalsIgnoreCase(groupNameExceptIndv)) {
				logger.fine( "countAlreadyExists increased : " + countAlreadyExists
						+ " groupNameExceptIndv : " + groupNameExceptIndv);
				countAlreadyExists = countAlreadyExists + 1;
			} else {
				countAlreadyExists = 0;
			}
		}

		return Map.of("hasMoreElementsToTraverse", hasMoreElementsToTraverse, "countAlreadyExists", countAlreadyExists);
	}

	protected boolean isRequiredToUpdatePersonalGroupsAndContacts(WhatsappCommonUtils instance) {
		return !instance.isRequiredToUpdatePersonalGroupsAndContacts()
				|| (instance.isRequiredToUpdatePersonalGroupsAndContacts()
						&& !isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA
						&& instance.getEnumFlowType().equals(FlowTypeEnum.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS));
	}

	public boolean isRequiredToUpdatePersonalGroupsAndContacts() {
		return isRequiredToUpdatePersonalGroupsAndContacts;
	}

	protected void updateExludePersonalContactListFile(String groupNameExceptIndv) throws InterruptedException {
		if (excludePersonalGroupNamesSet.stream()
				.anyMatch(sentGroup -> sentGroup.trim().toLowerCase().contains(groupNameExceptIndv.toLowerCase())
						|| groupNameExceptIndv.trim().toLowerCase().contains(sentGroup.trim().toLowerCase()))) {

			Set<String> excludeGroupContactNamesSetRetrived = extractContactsByGroupMouseHover(3000);
			excludePersonalGroupContactNamesOrNumbersSet = addMergePhChunksIntoAllPhoneContacts(
					excludePersonalGroupContactNamesOrNumbersSet, excludeGroupContactNamesSetRetrived);
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV,
					excludeGroupContactNamesSetRetrived);
		}
	}

	@Override
	public String waitIdentifyAndClickOnThePinnedElement(String waPinnedSpanTitle) throws InterruptedException {

			String pinnedGroup=null;
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(240l));
			WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(waPinnedSpanTitle)));
			// new WebDriverWait(getWebDriver(),
			// 10).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@class='footer']")));

			// interact with your element
			pinnedGroup = element.findElement(By.xpath(waPinnedSpanTitle.replace("/..",""))).getText();
			element.click();
			Thread.sleep(1000);
			logger.info("Pinned Group : "+pinnedGroup);
			return pinnedGroup;

		} catch (Exception ex) {
			System.out
					.println("Exception occurred in waitIdentifyAndClickOnThePinnedElement" + ex.getLocalizedMessage());
			Thread.sleep(1000);
			WebElement findElement = febx(waPinnedSpanTitle);
			pinnedGroup = findElement.findElement(By.xpath(waPinnedSpanTitle.replace("/..",""))).getText();
			logger.info("Pinned Group :: "+pinnedGroup);
			findElement.click();
			return pinnedGroup;
		}
	}

	protected void runRobotClassForNextChat() {
		runRobotMoveToChat(false, SLEEP_TIME_MS);
		logger.fine( "ROBOT Executed SLEEP_TIME_MS :" + SLEEP_TIME_MS);

	}

	protected void runRobotClassForPreviousChat() {
		runRobotMoveToChat(true, SLEEP_TIME_MS);
	}

	protected void runRobotClassForNextChat(int Substract, boolean isSubstractForFastening) {
		if (isSubstractForFastening) {
			SLEEP_TIME_MS = SLEEP_TIME_MS > Substract ? SLEEP_TIME_MS - Substract : SLEEP_TIME_MS;
		} else {
			SLEEP_TIME_MS = SLEEP_TIME_MS_RESET;
		}
		runRobotMoveToChat(false, SLEEP_TIME_MS);
	}

	protected void runRobotClassForNextChat(int SLEEP_TIME_MS) {
			SLEEP_TIME_MS = SLEEP_TIME_MS_RESET;
		runRobotMoveToChat(false, SLEEP_TIME_MS);
	}

	protected void runRobotClassForPreviousChat(int dividendToFasting) {
		SLEEP_TIME_MS = SLEEP_TIME_MS / dividendToFasting;
		runRobotMoveToChat(true, SLEEP_TIME_MS);
	}

	void runRobotMoveToChat(boolean moveToPrevOnly, int SLEEP_TIME_MS) {
		try {
			Robot robot = new Robot();
			// Robot class throws AWT Exception
			robot.keyPress(xPathInterface.getMoveToChatVkAlt());
			Thread.sleep(200);
			boolean isExecutionForWhatsApp = xPathInterface.getMoveToChatVkControl() != 0
					&& xPathInterface.getMoveToChatVkShift() != 0;

			if (isExecutionForWhatsApp) {
				robot.keyPress(xPathInterface.getMoveToChatVkControl());
				Thread.sleep(200);
				robot.keyPress(xPathInterface.getMoveToChatVkShift());
				Thread.sleep(200);
			}

			if (moveToPrevOnly) {
				robot.keyPress(xPathInterface.getMoveToChatVkPrevious());
			} else {
				robot.keyPress(xPathInterface.getMoveToChatVkNext());
			}
			Thread.sleep(SLEEP_TIME_MS);
			robot.keyRelease(xPathInterface.getMoveToChatVkAlt());
			Thread.sleep(300);//1200
			if (isExecutionForWhatsApp) {
				robot.keyRelease(xPathInterface.getMoveToChatVkControl());
				Thread.sleep(200);
				robot.keyRelease(xPathInterface.getMoveToChatVkShift());
				Thread.sleep(200);
			}

			if (moveToPrevOnly) {
				robot.keyRelease(xPathInterface.getMoveToChatVkPrevious());
			} else {
				robot.keyRelease(xPathInterface.getMoveToChatVkNext());
			}
			Thread.sleep(200);//200
			logger.fine( "ROBOT Executed Successfully for :" + (moveToPrevOnly ? "Prev" : "Next")
					+ " Chat: ALT+CTRL+]");
		} catch (Exception exception) {
			logger.fine( "Exception executing robot for " + (moveToPrevOnly ? "Prev" : "Next")
					+ " Chat: " + exception.getLocalizedMessage());
		}
	}

	public abstract boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored,
			String lastVisitedGroup);

	public void searchAndClickOnLastVisitedGroup(String lastGroup) {
		searchAndClickOnGroup(lastGroup);
	}
	// Template1 for archive
	/*
	 * if (counter >= 10 && hasNewGroupJoinedButNotRestored) {
	 * CommonImpl.searchAndClickOnGroup(lastVisitedGroup);
	 * hasNewGroupJoinedButNotRestored = false; }
	 *
	 * }
	 */


	protected boolean clickOnCancelOrClose() {
		logger.fine( "join Group click: Cancel if any error or long hold");
		List<WebElement> webElements = febxs(xPathInterface.getXPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP());

		if (!webElements.isEmpty()) {
			webElements.getFirst().click();
			return true;
		} else {
			actionsKeyPerform(Keys.ESCAPE);
		}
		return false;
	}

	public boolean popUp_joinGroupOrCancelForOtherCases_all(String xpathToJoinAppender, String xpathToCancelAppender) {

		try {
			//Thread.sleep(1000) is already added as implicitWait in InitialSetup class for each webElement calls
			List<WebElement> joinGroupButtonInPopUp = febxs(getxPathInterface().getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN()+(xpathToJoinAppender==null? "":xpathToJoinAppender));//<div class="ripple-container">

			if(!joinGroupButtonInPopUp.isEmpty()) {
				logger.fine("JOIN_GROUP button _FOUND_ "+joinGroupButtonInPopUp.size()+ " and clicking in the POP_UP: "+joinGroupButtonInPopUp.getFirst().getText());
				//click all if found multiples so that unnecessary popups will added or also be closed/cancelled
				try {
					joinGroupButtonInPopUp.forEach(i -> {
						i.click();
						logger.severe("JOIN_GROUP button CLICKED_Successfully: " + i.getText());
					});
				}catch (Exception e) {
						logger.severe("JOIN_GROUP button _NOT_CLICKED: ");
						e.printStackTrace();
					}
				logger.fine("Good News! Join Group button _CLICKED_ Successfully");
				return true;

			}else {
				//Cancel Close or Back
				logger.fine("JOIN_GROUP option _NOT_PRESENT_ in POP_UP. Trying to close the POP_UP");
				List<WebElement> cancelCloseOrBackArrows = febxs(getxPathInterface().getBUTTON_CANCEL_CLICK_BACK()+(xpathToCancelAppender==null? "" : xpathToCancelAppender));
				Thread.sleep(2000);
				if(!cancelCloseOrBackArrows.isEmpty()) {
					logger.fine("Join Group button NOT_FOUND and clicking in CANCEL_CLOSE_BUTTON in the POP_UP: "+cancelCloseOrBackArrows.getFirst().getText());
					cancelCloseOrBackArrows.stream().forEach(cancelJoinGroup-> cancelJoinGroup.click());
					logger.fine("CANCEL_CLOSE button Clicked Successfully");
					return true;
				}else{
					logger.fine("ERROR: Neither JOIN_GROUP nor CANCEL_CLOSE button FOUND. Trying Back button in GroupChat[Not Chat-List] area..");
					//Back button from newly added group is already handled in cancel/close
					List<WebElement> backButtons = febxs(BACK_OR_TITLE_BACK_IF_DUPLICATES+(xpathToCancelAppender==null? "" : xpathToCancelAppender));
					if(!backButtons.isEmpty() && backButtons.size()>1) {
						backButtons = febxs(EXACT_BACK_BUTTON+(xpathToCancelAppender==null? "" : xpathToCancelAppender));
					}
					if(!backButtons.isEmpty()) {
						logger.fine("Clicking on BACK_BUTTON in ALREADY_JOINED Added group: "+backButtons.getFirst().getText());
						backButtons.stream().forEach(backFromJoinedGroup-> backFromJoinedGroup.click());
						logger.fine("BACK_BUTTON button Clicked Successfully");
						return true;//Should continue the same loop
					}else{
						logger.fine("ERROR: _NONE_ of JOIN_GROUP or CANCEL_CLOSE or BACK button FOUND. Trying ESC button");
						actionsKeyPerform(Keys.ESCAPE);//In case it helps to remove unnecessary pop-up. not sure
					}
				}
			}
		}catch(Exception ex) {
			logger.severe("EXCEPTION in popUp_joinGroupOrCancelForOtherCases : "+ex.getLocalizedMessage());
			ex.printStackTrace();
		}

		return false;
	}


	public void doSomeAwaking(String groupName, int step) throws InterruptedException {
		logger.info(" doSomeAwaking invoked GroupName..:"+groupName+"..................Step :"+step);
		try {
			if(step==1) {
				String chatList = getxPathInterface().getLEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS();
				logger.info("..........SELF HEALING MECHANISM in doSomeAwaking....Waiting till visible :"+chatList);

				waitTillVisible(chatList);
				List<WebElement> febxs = febxs(chatList);
				logger.info("febxs(chatList) size: "+febxs.size());
				febxs.get(febxs.size()>10? febxs.size()-9: febxs.size()-1).click();
				return;
			}

			if(step==2) {
				logger.info("..........SELF HEALING in doSomeAwaking....Waiting till visible and clickable"+getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
					waitTillVisible(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
					waitTillClickable(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());

					WebElement webElementTypeMsg = febx(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE());
					if (webElementTypeMsg != null && !webElementTypeMsg.getText().isEmpty()) {
						webElementTypeMsg.click();
						webElementTypeMsg.click();
						searchAndClickOnGroup(groupName);
					} else {
						Thread.sleep(500);
						if (webElementTypeMsg != null && !webElementTypeMsg.getText().isEmpty()) {
							webElementTypeMsg.click();
						} else {
							febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
							febx(getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
							Thread.sleep(300);
						}
						return;
					}
				return;
			}

			if(step==3) {
					//Move to PevChat to nextChat
					Thread.sleep(2000);
					runRobotClassForPreviousChat();
					Thread.sleep(2000);
					runRobotClassForNextChat();
					Thread.sleep(2000);
					return;
			}

			if(step==4) {
				//Just sleep and searchAndClickOnGroup
				Thread.sleep(2000);
				searchAndClickOnGroup(groupName);
				Thread.sleep(2000);
				return;
		}

			if(step >=5) {
				driver.navigate().refresh();
				logger.info("..........SELF HEALING in doSomeAwaking.......driver.navigate().refresh() invoked to reload the browser.....................");
				Thread.sleep(10000);
			}


		} catch (Exception ex) {
			try {
			//febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
			//febx(getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
			Thread.sleep(2000);
			searchAndClickOnGroup(groupName);
			//Rfebx(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()).click();
			/*Actions actions = new Actions(driver);
			actions.contextClick().perform();
			Thread.sleep(200);
			actions.sendKeys(Keys.ESCAPE).perform();
			actions.release();*/
			}catch(ExceptionInInitializerError exx) {
				//Will never come here for admins
				WebElement onlyAdminCanSendMessage = driver
						.findElement(By.xpath("//span[text()='Only ']//span[@role='button' and text()='admins']//parent::span[text()=' can send messages']"));
				if(onlyAdminCanSendMessage!=null) {
					logger.info("Add to Admin text file");
				}else {
					exx.printStackTrace();
					actionsKeyPerform(Keys.ESCAPE);
				}
			}
		}

	}


	public abstract String getFilePathToExportContactsData();

	public abstract Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(
			WhatsappCommonUtils instance, boolean hasNewGroupJoinedButNotRestored, String groupNameExceptIndv,
			Set<String> allPhoneContacts, int counter, int repeatCounter) throws InterruptedException;

	public abstract boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping);

	public abstract int getExpectedSizeToModuloToInsertExportToFile();

	protected abstract int getMaxAllowedRepeatOfSameGroup();

	protected abstract int processCompletedSuccessfullyCounter();

	public abstract Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate();

	protected Map<EnumStringToExport, Set<String>> getHashTableToExportData(Set<String> allPhoneContacts, Set<String> groupNames,
			Set<String> groupNamesInvalid, Set<String> setOfDataToExport) {
		Map<EnumStringToExport, Set<String>> exportData = new Hashtable<>();

		exportData.put(EnumStringToExport.SET_OF_DATA, setOfDataToExport);
		exportData.put(EnumStringToExport.NEW_PH_CONTACTS, allPhoneContacts);
		exportData.put(EnumStringToExport.SUCCESSFUL_GROUP_NAMES, groupNames);
		exportData.put(EnumStringToExport.ERROR_GROUP_NAMES, groupNamesInvalid);

		logger.fine(
				"exportData: EnumStringToExport.SET_OF_DATA : " + exportData.get(EnumStringToExport.SET_OF_DATA));
		logger.fine( "exportData: EnumStringToExport.NEW_PH_CONTACTS Size: "
				+ exportData.get(EnumStringToExport.NEW_PH_CONTACTS).size());
		logger.info( "exportData: EnumStringToExport.SUCCESSFUL_GROUP_NAMES : "
				+ exportData.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
		logger.fine(
				"exportData: EnumStringToExport.ERROR_GROUP_NAMES : " + exportData.get(EnumStringToExport.SET_OF_DATA));

		return exportData;
	}

	protected boolean extractContacts_ValidateIfPresentInPersonalGroup_mergeAllContacts(String groupNameExceptIndv,
			Set<String> allPhoneContacts) throws InterruptedException {
		// 1. extractContacts
		Set<String> phContactsChunk = extractContactsByGroupMouseHover(3000);
		// Start : Avoid accessing newly created Personal Group
		boolean isCurrGroupNewPersonalCreatedGroup = validateBroadcastRiskIfNewPersonalGroupFound(groupNameExceptIndv,
				phContactsChunk);
		// End : Avoid accessing newly created Personal Group
		return isCurrGroupNewPersonalCreatedGroup;
	}

	protected Set<String> extractContactsByGroupMouseHover(int SLEEP_TIME) throws InterruptedException {
		Thread.sleep(SLEEP_TIME);
		String textOfContacts = febx(
				xPathInterface.getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT())
						.getText();
		Thread.sleep(SLEEP_TIME / 2);
		if(textOfContacts != null && !textOfContacts.isEmpty() && textOfContacts.split(" ").length >5) {
			logger.severe( "Suspicious contacts found. Returning emptySet. Contacts extracted : " + textOfContacts);
			return Collections.emptySet();
		}
		textOfContacts = removeJunkCharactersIfExists(textOfContacts);
		// Add contactNumbers to contact list
		Set<String> phContactsChunk = new HashSet<>(Arrays.asList(textOfContacts.split(",")));
		return phContactsChunk;
	}

	protected Set<String> addMergePhChunksIntoAllPhoneContacts(Set<String> allPhoneContacts,
			Set<String> phContactsChunk) {
		if (phContactsChunk != null && !phContactsChunk.isEmpty()) {
			logger.fine( "Extracted contacts chunk size : " + phContactsChunk.size());
			allPhoneContacts.addAll(phContactsChunk);
		} else {
			logger.fine( "Extracted mouseover contacts chunk is null or empty : " + phContactsChunk);
		}

		return allPhoneContacts;
	}

	public Set<String> clearUpdateCurrDateAndReloadFile(String fileNameWithExtension, String messageFormatInsideFile,
			Set<String> groupsAlreadyMessageSentForTheDaySet, boolean clearExistingContent) {

		if (groupsAlreadyMessageSentForTheDaySet.contains(messageFormatInsideFile + LocalDate.now())
				|| groupsAlreadyMessageSentForTheDaySet
						.contains(messageFormatInsideFile + LocalDate.now().minusDays(1l))|| groupsAlreadyMessageSentForTheDaySet
						.contains(messageFormatInsideFile + LocalDate.now().minusDays(2l))) {
			logger.fine("ClearUpdateCurrDateAndReloadFile : Message Already Posted within 2 Days : "+groupsAlreadyMessageSentForTheDaySet+ " fileNameWithExtension: "+fileNameWithExtension);
			return groupsAlreadyMessageSentForTheDaySet;
		} else {
			if (clearExistingContent) {
				logger.fine("ClearUpdateCurrDateAndReloadFile : clearExistingContent is true means message Posted long back.So clearning the file : clearExistingContent : "+clearExistingContent+ " fileNameWithExtension: "+fileNameWithExtension);
				WhatsappContactUtils.clearContentInFile(fileNameWithExtension);
			}

			logger.fine("ClearUpdateCurrDateAndReloadFile : cleared and then exportAndMergeToExisingSingleFile : "+clearExistingContent+ " fileNameWithExtension: "+messageFormatInsideFile + LocalDate.now());
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(fileNameWithExtension,
					Set.of(messageFormatInsideFile + LocalDate.now()));
		}
		logger.fine("ClearUpdateCurrDateAndReloadFile : Finally the groupsAlreadyMessageSentForTheDaySet retrived size:"+groupsAlreadyMessageSentForTheDaySet.size());
		groupsAlreadyMessageSentForTheDaySet = extractListOfDataFromFile(fileNameWithExtension);
		return groupsAlreadyMessageSentForTheDaySet;
	}

	public void updateWAAndTGSpecificDifferentSleepTime(SocialType onlyApplicableSocialTypeOutOfWAOrTG, WhatsappCommonUtils instance, long waSleepDuration, long tgSleepDuration) throws InterruptedException {

		//onlyApplicableSocialTypeOutOfWAOrTG : if we want to add a sleep time only for WA or only for TG exclusively/specifically.

		if((onlyApplicableSocialTypeOutOfWAOrTG==null || onlyApplicableSocialTypeOutOfWAOrTG == SocialType.WHATSAPP) && instance.getxPathInterface().getSocialType() == SocialType.WHATSAPP) {
			Thread.sleep(waSleepDuration);
			return;
		}
		if((onlyApplicableSocialTypeOutOfWAOrTG==null || onlyApplicableSocialTypeOutOfWAOrTG == SocialType.TELEGRAM) && instance.getxPathInterface().getSocialType() == SocialType.TELEGRAM) {
			Thread.sleep(tgSleepDuration);//7k to allow to load the chat for each group in WA but not applicable for TG as it clicks clickToButton
			return;
		}
	}


	public Properties getConfigProperties(){
		Properties prop = new Properties();

		// try-with-resources automatically closes the InputStream
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				System.out.println("Sorry, unable to find config.properties");
				return null;
			}
			prop.load(input);
			prop.forEach((key, value) -> System.out.println("getConfigProperties: "+key + " : " + value));

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return prop;
	}

	protected boolean validateBroadcastRiskIfNewPersonalGroupFound(String groupNameExceptIndv,
			Set<String> phContactsChunkRetrievedFromSingleGroup) {
		boolean isCurrGroupNewlyCreatedPersonalGroup = false;
		// Avoid newly created Personal Group
		if (isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA && phContactsChunkRetrievedFromSingleGroup
				.parallelStream().anyMatch(contact -> excludePersonalGroupContactNamesOrNumbersSet.contains(contact))) {
			logger.fine(
					" !!!!! New Personal Group found but not present in excludePersonalGroupNames during traversal: "
							+ groupNameExceptIndv);
			phContactsChunkRetrievedFromSingleGroup.parallelStream()
					.filter(contact -> excludePersonalGroupContactNamesOrNumbersSet.contains(contact))
					.forEach(contact -> {
						logger.fine( " excludePersonalGroupContactNamesOrNumbersSet contact: " + contact);
					});
			// todo: include/export the newly created personal group in
			// excldePersonalGroupNames and add contact to personalContactFile
			WhatsappContactUtils.exportAndMergeToExistingSingleFile(EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV, Set.of(groupNameExceptIndv));
			isCurrGroupNewlyCreatedPersonalGroup = true;
		}
		return isCurrGroupNewlyCreatedPersonalGroup;
	}

	public boolean searchAndClickOnGroup(String oldGroupString) {

		try {
			logger.info( "searchAndClickOnGroup start : " + oldGroupString);
			Thread.sleep(3000);
			WebElement webElement = searchAGroupInSearchBar(oldGroupString);
			// driver.manage().timeouts().implicitlyWait(SLEEP_TIME, TimeUnit.MILLISECONDS);
			/*
			 * WebElement webElementGroupNameCheck = driver
			 * .findElement(By.xpath(String.format(getxPathInterface().
			 * getSELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP(),oldGroupString)));
			 */
			webElement.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
			Thread.sleep(3000);

			waitTillVisible(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());

			boolean isClickedSuccessfulForClear = febxsAndClick(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION(),"//*[@aria-label='Cancel search']",false,"CLEAR_BUTTON_IN_SEARCH");
			logger.severe("searchAndClickOnGroup success ? CLEAR_IN_SEARCH: " + isClickedSuccessfulForClear);

			/*
			try {

				List<WebElement> clearOrBacks = febxs(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());
				if (!clearOrBacks.isEmpty()) {
					clearOrBacks.getFirst().click();
					Thread.sleep(1000);
				} else {
					logger.severe("CLEAR_BUTTON_NOT_FOUND for : " + getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());
					clearOrBacks = febxs("//*[@aria-label='Cancel search']");
					if (!clearOrBacks.isEmpty()) {
						clearOrBacks.getFirst().click();
						Thread.sleep(1000);
						logger.info("CLEAR_BUTTON_FOUND");
					}
				}
				Thread.sleep(3000);

			} catch (Exception e) {
				e.printStackTrace();
			}*/

			try {
				/*
				 * febx(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION())
				 * .click(); Thread.sleep(500);
				 */

				//waitTillVisible(getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());//Must be available to click on back

				boolean isClickedSuccessful = febxsAndClick("//span[@data-icon='back']",getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH(),false,"BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH");
				 if(!isClickedSuccessful) isClickedSuccessful = febxsAndClick("//span[@data-icon='back']/parent::div", "//span[@data-icon='search']", false, "BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_TRYING_OTHERS");

				logger.severe("searchAndClickOnGroup success ? BACK_ARROW: " + isClickedSuccessful);

				 /*List<WebElement> wes = febxs(getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());
				//create a chain of clicks within try-catch using varrargs
				//or use febxsAndClick
				if (!wes.isEmpty()) {
					try {
						wes.getFirst().click();
					}catch(Exception eex) {
						logger.severe("BACK_ARROW_WITH_ChatList_NOT_FOUND : " + getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());
						wes = febxs("//span[@aria-label='Back']");
						if (!wes.isEmpty()) {
							wes.getFirst().click();
							logger.info("BACK_ARROW_FOUND");
						} else {
							logger.severe("BACK_ARROW_Back_NOT_FOUND : " + getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());
						}
					}
				} else *//*{
						wes = febxs("//span[@aria-label='Back']/parent::div");
						if (!wes.isEmpty()) {
							wes.getFirst().click();
							logger.info("BACK_ARROW_NOW_FOUND");
						} else {
							wes = febxs("//span[@aria-label='Back']/parent::button");
							if (!wes.isEmpty()) {
								wes.getFirst().click();
								logger.info("BACK_ARROW_NOW_FINALLY_FOUND");
							}
						}
					}*/
				Thread.sleep(300);
			} catch (Exception ex) {
				logger.severe("CLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION for : " + oldGroupString);
				ex.printStackTrace();
			}
			logger.fine( "Old Group restored from Search box: " + oldGroupString);
			return true;

		} catch (Exception exception) {
			exception.printStackTrace();
			logger.severe( "exception searchAndClickOnGroup: " + exception.getLocalizedMessage());
		}
		return true;
	}

	public WebElement searchAGroupInSearchBar(String oldGroupString) throws InterruptedException {
		febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
		WebElement webElement = febx(getxPathInterface().getXPATH_SEARCH());

		// 1.Search groupName in SearchBox
		Thread.sleep(2000);
		logger.info( "Old Group typing in Search box: " + oldGroupString);
		webElement.sendKeys(oldGroupString + "\n");
		Thread.sleep(2000);
		return webElement;
	}

	public void removeArchievedOrExitedClosurePopups() {
		try {
			driver.findElements(By.xpath(getxPathInterface().getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT())).stream()
					.forEach(crossArchievedPopup -> {
						if (crossArchievedPopup != null) {
							crossArchievedPopup.click();
						}
					});
			Thread.sleep(200);
			logger.fine( "ClosurePopups executed..... ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void removeSingleArchievedOrExitedClosurePopup() {
		try {
			WebElement webElement = febx(getxPathInterface().getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT());
			if (webElement != null && !webElement.getText().isEmpty()) {
				webElement.click();
			}
			logger.fine( "ClosurePopups executed..... ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isTraverseDependsOnAllSuccessGroupList() {
		return traverseDependsOnAllSuccessGroupList;
	}

	public void setTraverseDependsOnAllSuccessGroupList(boolean traverseDependsOnAllSuccessGroupList) {
		this.traverseDependsOnAllSuccessGroupList = traverseDependsOnAllSuccessGroupList;
	}

	public FlowTypeEnum getEnumFlowType() {
		return enumFlowType;
	}

	public XPathInterfaceWATG getxPathInterface() {
		return xPathInterface;
	}

	public void setxPathInterface(XPathInterfaceWATG xPathInterface) {
		this.xPathInterface = xPathInterface;
	}

	public abstract void traverseGroupsTemplate(WhatsappCommonUtils instance)
			throws InterruptedException, AWTException, IOException;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public static int getSLEEP_TIME_MS() {
		return SLEEP_TIME_MS;
	}

	public static void setSLEEP_TIME_MS(int sLEEP_TIME_MS) {
		SLEEP_TIME_MS = sLEEP_TIME_MS;
	}

	public static int getSLEEP_TIME_MS_RESET() {
		return SLEEP_TIME_MS_RESET;
	}

	public static void setSLEEP_TIME_MS_RESET(int sLEEP_TIME_MS_RESET) {
		SLEEP_TIME_MS_RESET = sLEEP_TIME_MS_RESET;
	}

	public String getMessgeToSearch() {
		return messgeToSearch;
	}

	public void setMessgeToSearch(String messgeToSearch) {
		this.messgeToSearch = messgeToSearch;
	}

	/*public SocialType getSocialType() {
		return socialType;
	}

	public void setSocialType(SocialType socialType) {
		this.socialType = socialType;
	}*/

	public boolean isRunningTestSuite() {
		return runningTestSuite;
	}

	public void setRunningTestSuite(boolean runningTestSuite) {
		this.runningTestSuite = runningTestSuite;
	}

	public int getNumOfGroupsLimitTestSuite() {
		return numOfGroupsLimitTestSuite;
	}

	public void setNumOfGroupsLimitTestSuite(int numOfGroupsLimitTestSuite) {
		this.numOfGroupsLimitTestSuite = numOfGroupsLimitTestSuite;
	}


}