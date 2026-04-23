package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.enums.*;
import com.wtf.app.app.interfaces.IUnArchiveAllSupportGroups;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.*;

public class WA_A_UnArchiveAllSupportGroups extends WhatsappParent implements IUnArchiveAllSupportGroups {

	public WA_A_UnArchiveAllSupportGroups(String baseURL, SocialType socialType) {
		super(baseURL,socialType);
	}


	private FlowTypeEnum enumFlowType = FlowTypeEnum.UN_ARCHIVE_SUPPORT_GROUP;
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
		System.out.println("About to unarchive all archived groups matches with Successful groups");

		boolean isUnArchivedSuccessful = unAarchiveSupportGroups(groupNameExceptIndv,
				EnumActionOnRightClickPopUp.UNARCHIVE_SUPPORT_GROUP);

		System.out.println(
				"Unarchived done for all archived groups matches with Successful groups : " + isUnArchivedSuccessful);

		Map<ActionIfTheGroupIsEligible, Object> map = new HashMap<>();

		return map;

	}

	boolean unAarchiveSupportGroups(String groupNameExceptIndv, EnumActionOnRightClickPopUp actionOnPopUp)
			throws InterruptedException {

		loadArchiveAndRefreshInitializeWait();

		WebElement clickOnEachArchivedElement = null;
		String previousGroup = "";
		Actions actions = new Actions(driver);
		try {
			int counter = 1;
			int iWithoutException = 0;
			int exceptionOccuredCounter = 0;
			int sameGroupOccurredCont = 0;

			int size = allSuccessfulSupportGroupsFromFile.size();
			System.out.println("Size: Archive Started allSuccessfulSupportGroupsFromFile: " + size);

			for (int i = 0; i <= 4000; i++) {

				try {
					Thread.sleep(200);

					try {
						// WebDriverWait wait = new WebDriverWait(getDriver(), 160);
						// clickOnEachArchivedElement =
						// wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT+"["+counter+"]")));
						clickOnEachArchivedElement = driver.findElement(
								By.xpath(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT() + "[" + counter + "]"));
						iWithoutException = iWithoutException + 1;
						i = iWithoutException;
						exceptionOccuredCounter = 0;
						System.out.println("Archive Started allSuccessfulSupportGroupsFromFile: " + i+" iWithoutException: "+iWithoutException+" allSuccessGroup :"+size+" exceptionOccuredCounter : "+exceptionOccuredCounter);

					} catch (Exception ex) {
						System.out.println("Exception while unarchving a group :" + counter);
						exceptionOccuredCounter = exceptionOccuredCounter + 1;
						ex.printStackTrace(System.out);
						if (counter >= 20) {
							counter = 1;
							Thread.sleep(200);
							System.out.println("Exception while unarchving a group :counter 20 : " + counter);
							continue;
						}
						counter = counter + 1;
						Thread.sleep(200);
						System.out.println("Exception while unarchving a group :counter !=20: " + counter);
						continue;
					}
					// if(allSuccessfulSupportGroupsFromFile.contains(clickOnEachArchivedElement.getText())){
					try {
						String archivedGroupElementName = clickOnEachArchivedElement.getText();
						actions.contextClick(clickOnEachArchivedElement).perform();

						System.out.println(
								"Archive Folder Opened : actions click on archive Performed to open list of archived chats to unarchive");
						Thread.sleep(6000);//to avoid to skip sleep on webElement during exception like for unarchieve

						// febx(ARRORW_FROM_ARCHIVED_TO_CHATS));// just to
						// make sure to keep in archived folder and do unarchive instead of archiving

						// Add some crucial validation
						if (actionOnPopUp == EnumActionOnRightClickPopUp.UNARCHIVE_SUPPORT_GROUP) {

							actions.sendKeys(Keys.ARROW_DOWN, Keys.ENTER).perform();
							System.out.println("UN_ARCHIVE_SUPPORT_GROUP : ARROW_DOWN and ENTER Performed : PreviousGroup: "+ previousGroup + " CurrentGroup: " + archivedGroupElementName);
							 Thread.sleep(2000);
							// actions.sendKeys(Keys.ESCAPE).perform();--its returning the flow back to main
							// page
						}
						removeArchievedOrExitedClosurePopups();
						Thread.sleep(1200);
						removeArchievedOrExitedClosurePopups();

						if (previousGroup.equals(archivedGroupElementName)) {
							counter = counter + 1;
							sameGroupOccurredCont=sameGroupOccurredCont+1;
							System.out.println("SameGroupOccurred : "+sameGroupOccurredCont+" archivedGroupElementName: "+archivedGroupElementName+" exceptionOccuredCounter:"+exceptionOccuredCounter);
							//Avoid infinity inside
							if(exceptionOccuredCounter>=100 || sameGroupOccurredCont>=100) {
								break;
							}
							continue;
						}
						counter = 1;
						Thread.sleep(1000);
						previousGroup = archivedGroupElementName;
						sameGroupOccurredCont=0;
						System.out.println("Success. Not SameGroupOccurred so counter,group,sameGroupOccurredCont are reset : "+sameGroupOccurredCont+" counter: "+counter);

					} catch (StaleElementReferenceException elementReferenceException) {
						elementReferenceException.printStackTrace();
						exceptionOccuredCounter = exceptionOccuredCounter + 1;
						Thread.sleep(1200);
						counter = 1;
						// actions.sendKeys(Keys.ESCAPE).perform();
						loadArchiveAndRefresh();
						//System.out.println("StaleElementReferenceException. So not Unarchived : "+ clickOnEachArchivedElement.getText());

						continue;
					} catch (Exception ex) {
						/*System.out.println("Exception :Group is not present in successful group. So not Unarchived : "
								+ clickOnEachArchivedElement.getText());*/
						counter = counter + 1;
						exceptionOccuredCounter = exceptionOccuredCounter + 1;
						loadArchiveAndRefresh();
						Thread.sleep(200);
						continue;
					}
					// return true;
				} catch (NoSuchElementException noSuchElementException) {
					System.out.println("exception noSuchElementException :" + noSuchElementException.getMessage());
					loadArchiveAndRefresh();
					exceptionOccuredCounter = exceptionOccuredCounter + 1;
					Thread.sleep(200);
				}

				if (i % 5 == 0) {
					loadArchiveAndRefresh();// refresh forcefully to bring the cursor at top as its archiving randomly
				}

				//Avoid infininte loop while no more elements but searching continuously
				if(exceptionOccuredCounter>=100) {
					System.out.println("All Archived Chats are Unarchived successfully. Size : "+iWithoutException);
					break;
				}

			}

			// outer Catch
		} catch (NoSuchElementException noSuchElementException) {
			System.out.println("exception noSuchElementException outer :" + noSuchElementException.getMessage());
			loadArchiveAndRefresh();
			Thread.sleep(200);
		} catch (Exception ex) {
			System.out.println("exception noSuchElementException outer");
			ex.printStackTrace();
			loadArchiveAndRefresh();
			// return false;
		}

		System.out.println("actions.release process ended...");
		actions.release();
		return true;
	}

	void loadArchiveAndRefreshInitializeWait() throws InterruptedException {
		try {
			waitIdentifyAndClickOnThePinnedElement(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE());

		} catch (Exception ex) {
			System.out.println("exception loadArchiveAndRefreshInitializeWait");
			febx(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE()).click();
			// Thread.sleep(100);
			febx(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE()).click();
			ex.printStackTrace();

		}
	}

	void loadArchiveAndRefresh() throws InterruptedException {
		try {
			febx(getxPathInterface().getARRORW_FROM_ARCHIVED_TO_CHATS()).click();
			Thread.sleep(400);
			febx(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE()).click();

		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			System.out.println("exception loadArchiveAndRefresh");
			febx(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE()).click();
			// Thread.sleep(100);
			febx(getxPathInterface().getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE()).click();

		}
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

	public Boolean conditionValidationForGroupContains(String groupNameExceptIndv) {
		return allSuccessfulSupportGroupsFromFile.stream()
				.anyMatch(group -> (group.trim().toLowerCase().contains(groupNameExceptIndv.toLowerCase())
						|| groupNameExceptIndv.toLowerCase().contains(group.trim().toLowerCase())));

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
