package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.ActionIfTheGroupIsEligible;
import com.wtf.app.app.enums.EnumStringToExport;
import com.wtf.app.app.enums.FlowTypeEnum;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.interfaces.IUnArchiveAllSupportGroups;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class WA_CC_SyncGroupNamesWithSentForTheDayFile extends WhatsappParent implements IUnArchiveAllSupportGroups {

	WhatsappCommonUtils instance;

	public WA_CC_SyncGroupNamesWithSentForTheDayFile(String baseURL, SocialType socialType) {
		super(baseURL,socialType);
	}


	private FlowTypeEnum enumFlowType = FlowTypeEnum.SYNC_GROUP_NAMES_WITH_SENT_FOR_THE_DAY_FILE;
	private boolean isSuccessfulSupportGroupContainsCheckRequired = false;


	@Override
	public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored,
			String lastVisitedGroup) {
		return hasNewGroupJoinedButNotRestored;
	}

	@Override
	public void moveGroupsToUnArchive() {

	}

	@Override
	public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(
			WhatsappCommonUtils instance, boolean isPreviousArchived, String groupNameExceptIndv,
			Set<String> allPhoneContacts, int counter, int repeatCounter) throws InterruptedException {

		logger.info("About to sync all groups matches with Successful groups in Sent for the Day file");
		this.instance = instance;
		String message_SELF_SKILLSET = instance.getxPathInterface().getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT();
		String messageToSearchInGroups= message_SELF_SKILLSET.replace("*", "").substring(0, message_SELF_SKILLSET.indexOf(" Years of ")+15);

		boolean isSyncGroupsPostedSuccessful = syncSentForTheDayFileAgainstActuallySentMessages(messageToSearchInGroups);

		logger.info(
				"Syncing done for all groups matches with Successful groups : " + isSyncGroupsPostedSuccessful);

		Map<ActionIfTheGroupIsEligible, Object> map = new HashMap<>();

		return map;

	}
	  public boolean syncSentForTheDayFileAgainstActuallySentMessages(String messagePosted) {
		  //consider the admin groups as well

		  //String messagePosted="Hi, I am a Fullstack and Backend Proxy and Support Person";

	            // Loop to scroll and load more chats until no new chats are loaded

	            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
	            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

	            try {

	                // 1. Locate the search bar and enter the search term
	                //WebElement searchBar = febx("//input[contains(@placeholder, 'Search')]");
	            	searchAGroupInSearchBar(messagePosted);

	                logger.info("Entered search term: " + messagePosted);

	                // 2. Wait for search results to load (adjust XPath for the results container)
	                WebElement searchResultsContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'LeftSearch--content') and contains(@class, 'custom-scroll')]")));
	                logger.info("Search results container found.");

	                Set<String> actualGroupNamesMessagePosted = new HashSet<>();
	                int previousResultCount = 0;
	                int currentResultCount = 0;
	                int sameResultCount=0;

	                logger.info("Starting endless scroll through search results...");

	                // 3. Scroll through search results until no more are loaded
	                while (true) {
	                    //List<WebElement> searchResultItems = driver.findElements(By.xpath("//div[contains(@class, 'ListItem ChatMessage')]"));
	                    List<WebElement> searchResultItems = driver.findElements(By.xpath("//div[contains(@class, 'ListItem ChatMessage')]/descendant::h3[contains(@class,'fullName')]"));

	                    previousResultCount = searchResultItems.size();
	                    //logger.info("Previous search result count: " + currentResultCount);

	                    jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];", searchResultsContainer, 1000);
	                    Thread.sleep(1000);


	                    jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];", searchResultsContainer, 2000);
	                    Thread.sleep(1000);


	                    jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];", searchResultsContainer, 3000);
	                    Thread.sleep(1000);

	                    jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];", searchResultsContainer, 4000);
	                    Thread.sleep(1000);

	                    // Scroll down by a smaller increment (e.g., a fraction of the scrollHeight)
	                    //jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + (arguments[0].scrollHeight * 0.5);", searchResultsContainer);
	                    //Thread.sleep(2000); // Adjust as needed

	                    //jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + (arguments[0].scrollHeight * 0.5);", searchResultsContainer);
	                    //Thread.sleep(2000); // Adjust as needed


	                    // Scroll to the bottom of the search results container
	                    //jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", searchResultsContainer);

	                    // Wait for a short period for more results to load
	                    //Thread.sleep(5000); // Adjust as needed

	                    //searchResultItems = driver.findElements(By.xpath("//div[contains(@class, 'ListItem ChatMessage')]"));
	                    searchResultItems = driver.findElements(By.xpath("//div[contains(@class, 'ListItem ChatMessage')]/descendant::h3[contains(@class,'fullName')]"));

	                    //Add them into a set
	                    actualGroupNamesMessagePosted.addAll(searchResultItems.stream().map(we->we.getText().trim()).collect(Collectors.toSet()));

	                    currentResultCount = searchResultItems.size();
	                    logger.info("Current search result count: " + currentResultCount);
	                    Thread.sleep(5000);
	                    if(currentResultCount == previousResultCount) {
	                    	sameResultCount++;
	                    }

	                    if (sameResultCount == 15) {
	                        logger.info("Reached the end of the search results.");
	                        break;
	                    }

	                    // Optional timeout
	                    // ...
	                }

	                // 4. Extract contact/group names from the loaded search results
	                List<WebElement> allSearchResults = driver.findElements(By.xpath("//div[contains(@class, 'ListItem ChatMessage')]"));
	                logger.info("Total search results loaded: " + allSearchResults.size());

	                // 5. Print the extracted names
	                if (!actualGroupNamesMessagePosted.isEmpty()) {

	                    logger.info("\nContacts/Groups where the message '" + messagePosted + "' was found:");
	                    for (String name : actualGroupNamesMessagePosted) {
	                        logger.info("- " + name);
	                    }
	                    logger.info("\nPrevious Groups extra present in AlreadySent incl admin with message '" + messagePosted + "' was found total :"+groupsAlreadyMessageSentForTheDaySetWA.size());

	                    groupsAlreadyMessageSentForTheDaySetWA.removeAll(actualGroupNamesMessagePosted);

	                    logger.info("\nMessage Actually posted found in :"+actualGroupNamesMessagePosted.size()+" groups");
	                    logger.info("\nExtra Groups present in AlreadySent but not recieved the message : '" + messagePosted + "' was found in :"+groupsAlreadyMessageSentForTheDaySetWA.size()+" groups");

	                    WhatsappContactUtils.writeGroupsWithNewLine(Paths.get(this.getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG()), actualGroupNamesMessagePosted);

	                } else {
	                    logger.info("\nNo contacts or groups found with the message '" + messagePosted + "'.");
	                }

	            } catch (Exception e) {
	                System.err.println("An error occurred: " + e.getMessage());
	            } finally {
	                driver.quit();
	            }
	            return true;
	        }


	@Override
	public Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
		return new TreeSet<>();
	}

	@Override
	public boolean exportDataToFile(String fileName, Map<EnumStringToExport, Set<String>> setOfDataMapping) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int getExpectedSizeToModuloToInsertExportToFile() {
		return 3;
	}

	@Override
	protected int getMaxAllowedRepeatOfSameGroup() {
		return 10;
	}
	@Override
	protected int processCompletedSuccessfullyCounter() {
		return 5;
	}


	@Override
	public String getFilePathToExportContactsData() {
		return "";
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
