package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.enums.*;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.util.RetryUtils;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Component(value = "watgCommonUtils")
public abstract class WATGCommonUtils extends SocialParentCommonUtils {
    private static final Logger logger = LogManager.getLogger(WATGCommonUtils.class);

    private static int SLEEP_TIME_MS = 2000;
    private static int SLEEP_TIME_MS_RESET = SLEEP_TIME_MS;
    private static final String BACK_OR_TITLE_BACK_IF_DUPLICATES = "//*[@aria-label='Back' or @title='Back']";
    protected static final String EXACT_BACK_BUTTON = "//div[@class='back-button']/button[@title='Back']";

	@Value("${daysToSubtract:2}")
	private int daysToSubtract;

	protected XPathInterfaceWATG xPathInterfaceWATG;
    protected String messageToSearch;

    protected FlowType flowType;
    protected boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA;
    protected boolean isRequiredToUpdatePersonalGroupsAndContacts = false;

	protected ParallelWebDriverManager parallelWebDriverManager;

	@Autowired
    public WATGCommonUtils( 
                         InitialSetup initialSetup,
						   ParallelWebDriverManager parallelWebDriverManager) {
        super( initialSetup, parallelWebDriverManager);
		this.parallelWebDriverManager = parallelWebDriverManager;
        //initializeRequestScopedFields();
        logger.info("WATGCommonUtils initialized with  InitialSetup and ParallelWebDriverManager");
    }
    
    private void initializeRequestScopedFields() {
        // Initialize request-scoped fields
        this.xPathInterfaceWATG = null;
        this.baseUrl = null;
        this.messageToSearch = null;
        ((SocialParentCommonUtils)this).setTraverseDependsOnAllSuccessGroupList(false);
        this.flowType = null;

		//this.isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = TASK_SOCIAL_DATA.get().isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days();
        /*this.isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA =
            whatsappBroadcastGroupsAlreadyMessageSentForTheDaySet.contains("group_traverse_already_date_" + LocalDate.now()) ||
					whatsappBroadcastGroupsAlreadyMessageSentForTheDaySet.contains("group_traverse_already_date_" + LocalDate.now().minusDays(1L)) ||
					whatsappBroadcastGroupsAlreadyMessageSentForTheDaySet.contains("group_traverse_already_date_" + LocalDate.now().minusDays(2L));*/

    }

	protected boolean ifSuccessfulGroupsContainsCheckRequired(WATGCommonUtils instance,
															  String groupNameExceptIndividual) {
		return !instance.isSuccessfulSupportGroupContainsCheckRequired()
				|| (instance.isSuccessfulSupportGroupContainsCheckRequired()
						&& allSuccessfulSupportGroupsFromFile.stream().anyMatch(
								group -> (group.trim().toLowerCase().contains(groupNameExceptIndividual.toLowerCase())
										|| groupNameExceptIndividual.toLowerCase().contains(group.trim().toLowerCase()))));
	}

	protected abstract boolean isSuccessfulSupportGroupContainsCheckRequired();

	protected Map<String, Object> checkIfPrevCurrGroupSameOrRepeatExhausted(WATGCommonUtils instance,
																			String groupNameExceptIndv, String lastVisitedGroup, boolean hasMoreElementsToTraverse,
																			int countAlreadyExists) {

		if (countAlreadyExists >= instance.getMaxAllowedRepeatOfSameGroup()) {
			hasMoreElementsToTraverse = false;
			logger.info( "countAlreadyExists exceeded : " + instance.getMaxAllowedRepeatOfSameGroup()
					+ " groupNameExceptIndv : " + groupNameExceptIndv);

		} else {
			if (lastVisitedGroup.equalsIgnoreCase(groupNameExceptIndv)) {
				logger.info( "countAlreadyExists increased : " + countAlreadyExists
						+ " groupNameExceptIndv : " + groupNameExceptIndv);
				countAlreadyExists = countAlreadyExists + 1;
			} else {
				countAlreadyExists = 0;
			}
		}

		return Map.of("hasMoreElementsToTraverse", hasMoreElementsToTraverse, "countAlreadyExists", countAlreadyExists);
	}

	protected boolean isRequiredToUpdatePersonalGroupsAndContacts(WATGCommonUtils instance) {
		isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = TASK_SOCIAL_DATA.get(ThreadLocalAutomationContext.getContext().getTaskType())
				.isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days();
		return !instance.isRequiredToUpdatePersonalGroupsAndContacts()
				|| (instance.isRequiredToUpdatePersonalGroupsAndContacts()
						&& !isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA
						&& instance.getEnumFlowType().equals(FlowType.TRAVERSE_EXPORT_CONTACTS_AND_JOIN_NEW_GROUPS));
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
			WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV,
					excludeGroupContactNamesSetRetrived);
		}
	}

@Override
public String waitIdentifyAndClickOnThePinnedElement(String waPinnedSpanTitle) throws InterruptedException {

	logger.info("waitIdentifyAndClickOnThePinnedElement: {}", waPinnedSpanTitle);
    String pinnedGroup = null;
    final int maxRetries = 1;
    int retryCount = 0;
	boolean isPinnedElementFound = false;

	Thread.sleep(getSLEEP_TIME_MS());

	logger.info("Current driver: {} DriverMap: {}", parallelWebDriverManager.getCurrentDriver().getCurrentUrl(), parallelWebDriverManager.getProfileDriverMap().entrySet().stream().map(e->e.getKey()+"-"+e.getValue().getCurrentUrl()+"-"+e.getValue().getTitle()).collect(Collectors.joining(",")));

	while (retryCount < maxRetries) {
		String replaced = waPinnedSpanTitle.replace("/..", "");
		try {
			if (driver == null) {
				driver = getDriver();
			}


			final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(4200));
			String socialMediaScannerIdentifierAfterUrl = ThreadLocalAutomationContext.getContext().getSocialModel().getxPathInterface().getSOCIAL_MEDIA_SCANNER_IDENTIFIER_AFTER_URL();
			logger.info("Checking the Right Web Page for Social Media using ScannerIdentifier: {}", socialMediaScannerIdentifierAfterUrl);

			WebElement elementScanner = RetryUtils.withRetry(
					() -> wait.until(ExpectedConditions.presenceOfElementLocated(
							By.xpath(socialMediaScannerIdentifierAfterUrl)
					)),
					"Finding element by xpath: " + socialMediaScannerIdentifierAfterUrl,
					3,      // max retries
					5000     // delay between retries in ms
			);

			if (elementScanner == null) {
				logger.error("ElementScanner not found in ElementScanner: {} element is null", waPinnedSpanTitle);
				throw new NoSuchElementException("ElementScanner not found in pinnedTitle: " + waPinnedSpanTitle);
			}
			logger.info("Correct Match of ElementScanner found Successfully in ElementScanner: {}", socialMediaScannerIdentifierAfterUrl);

			logger.info("********** SCAN THE SCANNER AS SOON AS IT POSSIBLE***********");

			Thread.sleep(60000);


			// Wait for the element to be present in DOM first
			//final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
			// First wait for the element to be present in the DOM
			WebElement element = RetryUtils.withRetry(() -> wait.until(driver -> {
				try {
					WebElement el = driver.findElement(By.xpath(waPinnedSpanTitle));
					return el.isDisplayed() ? el : null;
				} catch (NoSuchElementException e) {
					logger.error("Element not found in pinnedTitle: {} with error {}", waPinnedSpanTitle, e.getMessage());
					return null;
				}
			}), "Finding element by xpath: " + waPinnedSpanTitle, 3, 60000);

			// Then wait for it to be clickable
			//wait = new WebDriverWait(driver, Duration.ofSeconds(100));
			WebElement clickablePinnedElement = RetryUtils.withRetry(() -> wait.until(ExpectedConditions.elementToBeClickable(element)), "Checking if element is clickable: " + waPinnedSpanTitle, 5, 60000);

			if (clickablePinnedElement == null) {
				logger.error("Element not found in pinnedTitle: {} element is null", waPinnedSpanTitle);
				throw new NoSuchElementException("Element not found in pinnedTitle: " + waPinnedSpanTitle);
			}

			// Scroll the element into view
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", clickablePinnedElement);
			Thread.sleep(getSLEEP_TIME_MS()/4); // Small pause for scroll to complete

			// Get the text before clicking
			logger.info("waPinnedSpanTitle.replaced after xpath: {}", replaced);
			WebElement pinnedGroupElement = RetryUtils.withRetry(() -> element.findElement(By.xpath(replaced)), "Finding replacedelement by xpath: " + replaced, 3, 60000);
			if (pinnedGroupElement == null) {
				logger.error("Element not found in pinnedTitle: {} element is null", waPinnedSpanTitle);
				throw new NoSuchElementException("Element not found in pinnedTitle: " + waPinnedSpanTitle);
			}
			pinnedGroup = pinnedGroupElement.getText();
			logger.info("Found pinned group: with no text associated: {}", pinnedGroup);

			// Click using JavaScript as a fallback
			try {
				RetryUtils.withRetry(() -> {
					element.click();
					return null;
				}, "Clicking element: " + waPinnedSpanTitle, 3, 60000);
			} catch (ElementClickInterceptedException e) {
				logger.warn("Element click intercepted, trying JavaScript click");
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
			}

			logger.info("Successfully clicked on pinned group: {}", pinnedGroup);
			Thread.sleep(getSLEEP_TIME_MS()* 10L); // Allow time for any post-click actions
			isPinnedElementFound = true;
			return pinnedGroup;

		} catch (TimeoutException e) {
			retryCount++;
			logger.warn("** Timeout waiting for element (attempt {}/{}): {}", retryCount, maxRetries, e.getMessage());
			if (retryCount >= maxRetries) {
				throw new RuntimeException("Failed to find or click element after " + maxRetries + " attempts: " + waPinnedSpanTitle, e);
			}
			// Wait before retry
			Thread.sleep(getSLEEP_TIME_MS()* 3L);

		} catch (StaleElementReferenceException e) {
			retryCount++;
			logger.warn("Stale element reference (attempt {}/{}), retrying...", retryCount, maxRetries);
			if (retryCount >= maxRetries) {
				throw new RuntimeException("Element became stale after " + maxRetries + " attempts", e);
			}

		} catch (Exception ex) {
			logger.error("Error in waitIdentifyAndClickOnThePinnedElement: {}", ex.getMessage(), ex);
			if (retryCount >= maxRetries - 1) {
				// Try one last time with direct find element as fallback
				try {
					WebElement findElement = febx(waPinnedSpanTitle);
					if (findElement != null) {
						pinnedGroup = findElement.findElement(By.xpath(replaced)).getText();
						logger.info("Using fallback method, found pinned group: {}", pinnedGroup);
						findElement.click();
						return pinnedGroup;
					}
				} catch (Exception e) {
					logger.error("Fallback method also failed", e);
				}
				throw new RuntimeException("Failed to identify and click pinned element after retries", ex);
			}
			retryCount++;
			Thread.sleep(getSLEEP_TIME_MS()/2);
		}
	}

	if (!isPinnedElementFound) {
		logger.error("Failed to find or click pinned element: {}", waPinnedSpanTitle);
		Thread.currentThread().interrupt();
		throw new RuntimeException("Failed to find or click pinned element: " + waPinnedSpanTitle);
	}
    
    return pinnedGroup;
}

	//Robot Class Implementation
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
			robot.keyPress(xPathInterfaceWATG.getMoveToChatVkAlt());
			Thread.sleep(200);
			boolean isExecutionForWhatsApp = xPathInterfaceWATG.getMoveToChatVkControl() != 0
					&& xPathInterfaceWATG.getMoveToChatVkShift() != 0;

			//Keypress
			if (isExecutionForWhatsApp) {
				robot.keyPress(xPathInterfaceWATG.getMoveToChatVkControl());
				Thread.sleep(200);
				robot.keyPress(xPathInterfaceWATG.getMoveToChatVkShift());
				Thread.sleep(200);
			}

			if (moveToPrevOnly) {
				robot.keyPress(xPathInterfaceWATG.getMoveToChatVkPrevious());
			} else {
				robot.keyPress(xPathInterfaceWATG.getMoveToChatVkNext());
			}
			Thread.sleep(SLEEP_TIME_MS);

			//Release keys
			robot.keyRelease(xPathInterfaceWATG.getMoveToChatVkAlt());
			Thread.sleep(300);//1200
			if (isExecutionForWhatsApp) {
				robot.keyRelease(xPathInterfaceWATG.getMoveToChatVkControl());
				Thread.sleep(200);
				robot.keyRelease(xPathInterfaceWATG.getMoveToChatVkShift());
				Thread.sleep(200);
			}

			if (moveToPrevOnly) {
				robot.keyRelease(xPathInterfaceWATG.getMoveToChatVkPrevious());
			} else {
				robot.keyRelease(xPathInterfaceWATG.getMoveToChatVkNext());
			}
			Thread.sleep(200);//200
			logger.info( "ROBOT Executed Successfully for :" + (moveToPrevOnly ? "Prev" : "Next")
					+ " Chat: ALT+CTRL+]");
		} catch (Exception exception) {
			logger.info( "Exception executing robot for " + (moveToPrevOnly ? "Prev" : "Next")
					+ " Chat: " + exception.getLocalizedMessage());
		}
	}

	//Action Class Implementation
	protected void runActionClassForPreviousChat() {
		runActionClassMoveToChat(true, SLEEP_TIME_MS);
	}

	protected void runActionClassForNextChat() {
		runActionClassMoveToChat(false, SLEEP_TIME_MS);
		logger.info( "Action class Executed SLEEP_TIME_MS :" + SLEEP_TIME_MS);

	}
	protected void runActionClassForNextChat(int Substract, boolean isSubstractForFastening) {
		if (isSubstractForFastening) {
			SLEEP_TIME_MS = SLEEP_TIME_MS > Substract ? SLEEP_TIME_MS - Substract : SLEEP_TIME_MS;
		} else {
			SLEEP_TIME_MS = SLEEP_TIME_MS_RESET;
		}
		runActionClassMoveToChat(false, SLEEP_TIME_MS);
	}

	void runActionClassMoveToChat(boolean moveToPrevOnly, int SLEEP_TIME_MS) {
		try {
			Actions actions = new Actions(driver);

				// Previous chat (Alt + Up)
				actions = actions.keyDown(xPathInterfaceWATG.getMoveToChatKeysAlt());
				boolean isExecutionForWhatsApp = xPathInterfaceWATG.getMoveToChatKeysControl().equals(Keys.CONTROL);

				//Key Down
				if (isExecutionForWhatsApp) {
					actions.keyDown(xPathInterfaceWATG.getMoveToChatKeysControl());
					Thread.sleep(200);
					actions.keyDown(xPathInterfaceWATG.getMoveToChatKeysShift());
					Thread.sleep(200);
				}
				actions.build().perform();

			if (moveToPrevOnly) {
				actions.sendKeys(xPathInterfaceWATG.getMoveToPreviousChatKeys());//send keys don't need perform
			} else {
				actions.sendKeys(xPathInterfaceWATG.getMoveToNextChatKeys());
			}

			Thread.sleep(SLEEP_TIME_MS);


			//Release keys
			actions = actions.keyUp(xPathInterfaceWATG.getMoveToChatKeysAlt());
			Thread.sleep(200);//1200
			if (isExecutionForWhatsApp) {
				actions.keyUp(xPathInterfaceWATG.getMoveToChatKeysControl());
				Thread.sleep(200);
				actions.keyUp(xPathInterfaceWATG.getMoveToChatKeysShift());
			}
			actions.build().perform();

			Thread.sleep(SLEEP_TIME_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Thread interrupted", e);
		} catch (Exception exception) {
			logger.info( "Exception executing action for " + (moveToPrevOnly ? "Prev" : "Next")
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
		logger.info( "join Group click: Cancel if any error or long hold for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
		List<WebElement> webElements = febxs(xPathInterfaceWATG.getXPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP());

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
				logger.info("JOIN_GROUP button _FOUND_ "+joinGroupButtonInPopUp.size()+ " and clicking in the POP_UP: "+joinGroupButtonInPopUp.getFirst().getText());
				//click all if found multiples so that unnecessary popups will added or also be closed/cancelled
				try {
					joinGroupButtonInPopUp.forEach(i -> {
						i.click();
						logger.error("JOIN_GROUP button CLICKED_Successfully: " + i.getText());
					});
				}catch (Exception e) {
						logger.error("JOIN_GROUP button _NOT_CLICKED:  for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
						e.printStackTrace();
					}
				logger.info("Good News! Join Group button _CLICKED_ Successfully for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
				return true;

			}else {
				//Cancel Close or Back
				logger.info("JOIN_GROUP option _NOT_PRESENT_ in POP_UP. Trying to close the POP_UP for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
				List<WebElement> cancelCloseOrBackArrows = febxs(getxPathInterface().getBUTTON_CANCEL_CLICK_BACK()+(xpathToCancelAppender==null? "" : xpathToCancelAppender));
				Thread.sleep(2000);
				if(!cancelCloseOrBackArrows.isEmpty()) {
					logger.info("Join Group button NOT_FOUND and clicking in CANCEL_CLOSE_BUTTON in the POP_UP: "+cancelCloseOrBackArrows.getFirst().getText());
					cancelCloseOrBackArrows.stream().forEach(cancelJoinGroup-> cancelJoinGroup.click());
					logger.info("CANCEL_CLOSE button Clicked Successfully");
					return true;
				}else{
					logger.info("ERROR: Neither JOIN_GROUP nor CANCEL_CLOSE button FOUND. Trying Back button in GroupChat[Not Chat-List] area..");
					//Back button from newly added group is already handled in cancel/close
					List<WebElement> backButtons = febxs(BACK_OR_TITLE_BACK_IF_DUPLICATES+(xpathToCancelAppender==null? "" : xpathToCancelAppender));
					if(!backButtons.isEmpty() && backButtons.size()>1) {
						backButtons = febxs(EXACT_BACK_BUTTON+(xpathToCancelAppender==null? "" : xpathToCancelAppender));
					}
					if(!backButtons.isEmpty()) {
						logger.info("Clicking on BACK_BUTTON in ALREADY_JOINED Added group: "+backButtons.getFirst().getText());
						backButtons.stream().forEach(backFromJoinedGroup-> backFromJoinedGroup.click());
						logger.info("BACK_BUTTON button Clicked Successfully for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
						return true;//Should continue the same loop
					}else{
						logger.info("ERROR: _NONE_ of JOIN_GROUP or CANCEL_CLOSE or BACK button FOUND. Trying ESC button for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
						actionsKeyPerform(Keys.ESCAPE);//In case it helps to remove unnecessary pop-up. not sure
					}
				}
			}
		}catch(Exception ex) {
			logger.error("EXCEPTION in popUp_joinGroupOrCancelForOtherCases : "+ex.getLocalizedMessage());
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
					Thread.sleep(getSLEEP_TIME_MS());
					//runRobotClassForPreviousChat();
					runActionClassForPreviousChat();
					Thread.sleep(getSLEEP_TIME_MS());
					//runRobotClassForNextChat();
					runActionClassForNextChat();
					Thread.sleep(getSLEEP_TIME_MS());
					return;
			}

			if(step==4) {
				//Just sleep and searchAndClickOnGroup
				Thread.sleep(getSLEEP_TIME_MS());
				searchAndClickOnGroup(groupName);
				Thread.sleep(getSLEEP_TIME_MS());
				return;
		}

			if(step >=5) {
				driver.navigate().refresh();
				logger.info("..........SELF HEALING in doSomeAwaking.......driver.navigate().refresh() invoked to reload the browser..................... for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
				Thread.sleep(getSLEEP_TIME_MS()*5L);
			}


		} catch (Exception ex) {
			try {
			//febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
			//febx(getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
			Thread.sleep(getSLEEP_TIME_MS());
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
					logger.info("Add to Admin text file for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
				}else {
					exx.printStackTrace();
					actionsKeyPerform(Keys.ESCAPE);
				}
			}
		}

	}


	public abstract String getFilePathToExportContactsData();

	public abstract Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(
			WATGCommonUtils instance, boolean hasNewGroupJoinedButNotRestored, String groupNameExceptIndv,
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

		logger.info(
				"exportData: EnumStringToExport.SET_OF_DATA : " + exportData.get(EnumStringToExport.SET_OF_DATA));
		logger.info( "exportData: EnumStringToExport.NEW_PH_CONTACTS Size: "
				+ exportData.get(EnumStringToExport.NEW_PH_CONTACTS).size());
		logger.info( "exportData: EnumStringToExport.SUCCESSFUL_GROUP_NAMES : "
				+ exportData.get(EnumStringToExport.SUCCESSFUL_GROUP_NAMES));
		logger.info(
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
				xPathInterfaceWATG.getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT())
						.getText();
		Thread.sleep(SLEEP_TIME / 2);
		if(textOfContacts != null && !textOfContacts.isEmpty() && textOfContacts.split(" ").length <5) {
			logger.error( "Suspicious contacts found. Returning emptySet. Contacts extracted : " + textOfContacts);
			return Collections.emptySet();
		}
		textOfContacts = removeJunkCharactersIfExists(textOfContacts);
		// Add contactNumbers to contact list
		Set<String> phContactsChunk = new HashSet<>(Arrays.asList(textOfContacts.split(",")));
		logger.info("Contacts extracted by mouse hover: {}",phContactsChunk.size());
		return phContactsChunk;
	}

	protected Set<String> addMergePhChunksIntoAllPhoneContacts(Set<String> allPhoneContacts,
			Set<String> phContactsChunk) {
		if (phContactsChunk != null && !phContactsChunk.isEmpty()) {
			logger.info( "Extracted contacts chunk size : " + phContactsChunk.size());
			allPhoneContacts.addAll(phContactsChunk);
		} else {
			logger.info( "Extracted mouseover contacts chunk is null or empty : " + phContactsChunk);
		}

		return allPhoneContacts;
	}

	public Set<String> clearUpdateCurrDateAndReloadFile(String fileNameWithExtension, String messageFormatInsideFile,
			 Set<String> groupsAlreadyMessageSentForTheDaySet, boolean clearExistingContent) {

		Set<String> finalGroupsAlreadyMessageSentForTheDaySet = groupsAlreadyMessageSentForTheDaySet;
		if (/*groupsAlreadyMessageSentForTheDaySet.contains(messageFormatInsideFile + LocalDate.now())
				||*/ IntStream.range(0,daysToSubtract+1).anyMatch(i-> finalGroupsAlreadyMessageSentForTheDaySet
						.contains(messageFormatInsideFile + LocalDate.now().minusDays(i)))/*|| groupsAlreadyMessageSentForTheDaySet
						.contains(messageFormatInsideFile + LocalDate.now().minusDays(2l))*/) {
			logger.info("ClearUpdateCurrDateAndReloadFile : Message Already Posted within 2 Days : "+groupsAlreadyMessageSentForTheDaySet+ " fileNameWithExtension: "+fileNameWithExtension);
			return groupsAlreadyMessageSentForTheDaySet;
		} else {
			if (clearExistingContent) {
				logger.info("ClearUpdateCurrDateAndReloadFile : clearExistingContent is true means message Posted long back.So clearning the file : clearExistingContent : "+clearExistingContent+ " fileNameWithExtension: "+fileNameWithExtension);
				WhatsappDBFileRepository.clearContentInFile(fileNameWithExtension);
			}

			logger.info("ClearUpdateCurrDateAndReloadFile : cleared and then exportAndMergeToExisingSingleFile : "+clearExistingContent+ " fileNameWithExtension: "+messageFormatInsideFile + LocalDate.now());
			WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(fileNameWithExtension,
					Set.of(messageFormatInsideFile + LocalDate.now()));
		}
		logger.info("ClearUpdateCurrDateAndReloadFile : Finally the groupsAlreadyMessageSentForTheDaySet retrived size:"+groupsAlreadyMessageSentForTheDaySet.size());
		groupsAlreadyMessageSentForTheDaySet = extractListOfDataFromFile(fileNameWithExtension);
		return groupsAlreadyMessageSentForTheDaySet;
	}

	public void updateWAAndTGSpecificDifferentSleepTime(SocialType onlyApplicableSocialTypeOutOfWAOrTG, WATGCommonUtils instance, long waSleepDuration, long tgSleepDuration) throws InterruptedException {

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
		isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = TASK_SOCIAL_DATA.get(ThreadLocalAutomationContext.getContext().getTaskType())
				.isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days();
		// Avoid newly created Personal Group
		if (isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA && phContactsChunkRetrievedFromSingleGroup
				.parallelStream().anyMatch(contact -> excludePersonalGroupContactNamesOrNumbersSet.contains(contact))) {
			logger.info(
					" !!!!! New Personal Group found but not present in excludePersonalGroupNames during traversal: "
							+ groupNameExceptIndv);
			phContactsChunkRetrievedFromSingleGroup.parallelStream()
					.filter(contact -> excludePersonalGroupContactNamesOrNumbersSet.contains(contact))
					.forEach(contact -> {
						logger.info( " excludePersonalGroupContactNamesOrNumbersSet contact: " + contact);
					});
			// todo: include/export the newly created personal group in
			// excldePersonalGroupNames and add contact to personalContactFile
			WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV, Set.of(groupNameExceptIndv));
			isCurrGroupNewlyCreatedPersonalGroup = true;
		}
		return isCurrGroupNewlyCreatedPersonalGroup;
	}

	public boolean searchAndClickOnGroup(String oldGroupString) {

		try {
			logger.info( "searchAndClickOnGroup start : " + oldGroupString+" : "+ThreadLocalAutomationContext.getContext().getTaskType());
			Thread.sleep(getSLEEP_TIME_MS()+1000L);
			WebElement webElement = searchAGroupInSearchBar(oldGroupString);
			// driver.manage().timeouts().implicitlyWait(SLEEP_TIME, TimeUnit.MILLISECONDS);
			/*
			 * WebElement webElementGroupNameCheck = driver
			 * .findElement(By.xpath(String.format(getxPathInterface().
			 * getSELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP(),oldGroupString)));
			 */
			webElement.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
			Thread.sleep(getSLEEP_TIME_MS()+1000L);

			waitTillVisible(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());

			try {
				boolean isClickedSuccessfulForClear = febxsAndClickWithRetry(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION(), "//span[@data-icon='close-refreshed']"/*"//button[@aria-label='Cancel search']"*/, false, "CLEAR_BUTTON_IN_SEARCH");
				logger.error("searchAndClickOnGroup success ? CLEAR_IN_SEARCH: " + isClickedSuccessfulForClear);
			}catch (Exception e) {
				logger.error("searchAndClickOnGroup failed CLEAR_IN_SEARCH: " + e.getMessage());
			}
			/*
			try {

				List<WebElement> clearOrBacks = febxs(getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());
				if (!clearOrBacks.isEmpty()) {
					clearOrBacks.getFirst().click();
					Thread.sleep(1000);
				} else {
					logger.error("CLEAR_BUTTON_NOT_FOUND for : " + getxPathInterface().getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION());
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

				boolean isClickedSuccessful = febxsAndClickWithRetry("//span[@data-icon='back']",getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH(),false,"BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH");
				 if(!isClickedSuccessful) isClickedSuccessful = febxsAndClickWithRetry("//span[@data-icon='back']/parent::div", "//span[@data-icon='search']", false, "BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_TRYING_OTHERS");

				logger.error("searchAndClickOnGroup success ? BACK_ARROW: " + isClickedSuccessful);

				 /*List<WebElement> wes = febxs(getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());
				//create a chain of clicks within try-catch using varrargs
				//or use febxsAndClick
				if (!wes.isEmpty()) {
					try {
						wes.getFirst().click();
					}catch(Exception eex) {
						logger.error("BACK_ARROW_WITH_ChatList_NOT_FOUND : " + getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());
						wes = febxs("//span[@aria-label='Back']");
						if (!wes.isEmpty()) {
							wes.getFirst().click();
							logger.info("BACK_ARROW_FOUND");
						} else {
							logger.error("BACK_ARROW_Back_NOT_FOUND : " + getxPathInterface().getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH());
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
				logger.error("CLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION for : " + oldGroupString);
				ex.printStackTrace();
			}
			logger.info( "Old Group restored from Search box: " + oldGroupString);
			return true;

		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error( "exception searchAndClickOnGroup: " + exception.getLocalizedMessage());
		}
		return true;
	}

	public WebElement searchAGroupInSearchBar(String oldGroupString) throws InterruptedException {
		febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
		WebElement webElement = febx(getxPathInterface().getXPATH_SEARCH());

		// 1.Search groupName in SearchBox
		Thread.sleep(getSLEEP_TIME_MS());
		logger.info( "Old Group typing in Search box: " + oldGroupString +" : "+ ThreadLocalAutomationContext.getContext().getTaskType());
		webElement.sendKeys(oldGroupString + "\n");
		Thread.sleep(getSLEEP_TIME_MS());
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
			logger.info( "ClosurePopups executed..... ");

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
			logger.info( "ClosurePopups executed..... ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public FlowType getEnumFlowType() {
		return flowType;
	}

	public XPathInterfaceWATG getxPathInterface() {
		return (XPathInterfaceWATG) getSocialModel().getxPathInterface();
	}

	public void setxPathInterface(XPathInterfaceWATG xPathInterface) {
		this.xPathInterfaceWATG = xPathInterface;
	}

	public abstract void traverseGroupsTemplate(WATGCommonUtils instance)
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

	public String getMessageToSearch() {
		return messageToSearch;
	}

	public void setMessageToSearch(String messageToSearch) {
		this.messageToSearch = messageToSearch;
	}

	public void setEnumFlowType(FlowType enumFlowType) {
		this.flowType = enumFlowType;
	}

	public XPathInterfaceWATG getxPathInterfaceWATG() {
		return xPathInterfaceWATG;
	}

	public void setxPathInterfaceWATG(XPathInterfaceWATG xPathInterfaceWATG) {
		this.xPathInterfaceWATG = xPathInterfaceWATG;
	}

	public WebDriver getDriver() {
		return driver;
	}

    protected void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    protected SocialType getSocialType() {
        return this.socialType;
    }

    protected void setFlowTypeEnum(FlowType flowType) {
        this.flowType = flowType;
    }

    public FlowType getFlowTypeEnum() {
        return this.flowType;
    }
}