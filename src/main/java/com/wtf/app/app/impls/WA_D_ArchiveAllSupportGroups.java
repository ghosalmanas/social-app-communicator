package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.*;
import com.wtf.app.app.interfaces.IArchiveAllSupportGroups;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class WA_D_ArchiveAllSupportGroups extends WhatsappParent implements IArchiveAllSupportGroups {

	public WA_D_ArchiveAllSupportGroups(String baseURL, SocialType socialType) {
		super(baseURL,socialType);
	}

	private FlowTypeEnum enumFlowType = FlowTypeEnum.ARCHIVE_SUPPORT_GROUP;

	private boolean isSuccessfulSupportGroupContainsCheckRequired = false;

	private static final String LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART = "//span[contains(@title,'";
	private static final String LEFT_PANEL_XREF_TO_ARCHIVE_2ND_PART = "') and contains(@title,'";
	private static final String LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART = "')]";

	// .findElement(By.xpath("//div[@data-testid='cell-frame-title']//child::span[contains(@title,'"+
	// groupNameExceptIndv.substring(0,5) + "') and
	// contains(@title,'"+groupNameExceptIndv.substring(groupNameExceptIndv.length()-6)+"')]"));

	@Override
	public boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored,
			String lastVisitedGroup) {
		return hasNewGroupJoinedButNotRestored;
	}

	@Override
	public void moveGroupsToArchive() throws InterruptedException, AWTException, IOException {
		// Iterating not to miss the groups receiving messages
		IntStream.range(0, 5).forEach(i -> {

			try {
				driver.get(driver.getCurrentUrl());
				logger.fine( "driver page refresh");
				Thread.sleep(2000);
				moveAllSupportGroupToArchived();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		});

		driver.quit();
	}

	@Override
	public Map<ActionIfTheGroupIsEligible, Object> takeActionIfTheGroupIsEligibleToProceed(WhatsappCommonUtils instance,
                                                                                           boolean isPreviousArchived, String groupNameExceptIndv, Set<String> allPhoneContacts, int counter,
                                                                                           int repeatCounter) throws InterruptedException {
		logger.fine( "Match found :current group inside successful groups");

		boolean isArchivedSuccessful = archiveSupportGrpOrExitAnnouncement(groupNameExceptIndv,
				EnumActionOnRightClickPopUp.ARCHIVE_SUPPORT_GROUP);

		if (isArchivedSuccessful) {
			isPreviousArchived = true;
			counter++;
		} else {
			isPreviousArchived = false;
			repeatCounter++;
		}

		Map<ActionIfTheGroupIsEligible, Object> map = Map.ofEntries(
				Map.entry(ActionIfTheGroupIsEligible.FLAG_TRUE, isPreviousArchived),
				Map.entry(ActionIfTheGroupIsEligible.SET_OF_DATA, allPhoneContacts),
				Map.entry(ActionIfTheGroupIsEligible.GROUP_NAME, groupNameExceptIndv),
				Map.entry(ActionIfTheGroupIsEligible.COUNTER, counter),
				Map.entry(ActionIfTheGroupIsEligible.REPEAT_COUNTER, repeatCounter));

		return map;

	}

	void moveAllSupportGroupToArchived() throws IOException, InterruptedException {

		Set<String> allNewSupportGroups = new TreeSet<>();

		int allExistingSupportGroupSize = allSuccessfulSupportGroupsFromFile.size();
		int bufferGroupLength = allExistingSupportGroupSize + 10;
		int counter = 0;
		int repeatCounter = 0;
		String previousGroupOrIndRepeatedForBreak = null;
		boolean isPreviousArchived = false;

		logger.fine( "before execution of robot while bufferGroupLength : " + bufferGroupLength);
		while (counter <= (bufferGroupLength + allNewSupportGroups.size())) {
			try {
				if (!isPreviousArchived) {
					runRobotClassForNextChat();
				}
				Thread.sleep(2000);
				String groupNameExceptIndv = driver
						.findElement(
								By.xpath(getxPathInterface().getFIND_GROUP_NAME_RIGHT_TOP()))
						.getText();

				logger.fine( "\n\n\nGroupName or Indv after robot : " + groupNameExceptIndv
						+ " isPreviousArchived :" + isPreviousArchived);

				if ((allSuccessfulSupportGroupsFromFile.stream()
						.anyMatch(group -> group.trim().contains(groupNameExceptIndv)
								|| groupNameExceptIndv.contains(group.trim())))
						|| (allNewSupportGroups.stream().anyMatch(group -> group.trim().contains(groupNameExceptIndv)
								|| groupNameExceptIndv.trim().contains(group.trim())))) {
					logger.fine( "Match found :current group inside successful groups");

					boolean isArchivedSuccessful = archiveSupportGrpOrExitAnnouncement(groupNameExceptIndv,
							EnumActionOnRightClickPopUp.ARCHIVE_SUPPORT_GROUP);

					if (isArchivedSuccessful) {
						isPreviousArchived = true;
						counter++;
					} else {
						isPreviousArchived = false;
						repeatCounter++;
					}

					if (!isArchivedSuccessful) {
						continue;
					}

				} else {
					isPreviousArchived = false;
					// Break Condition
					logger.fine( "GroupNameExceptIndv :" + groupNameExceptIndv
							+ " previousGroupOrIndRepeatedForBreak: " + previousGroupOrIndRepeatedForBreak);
					if (groupNameExceptIndv.equalsIgnoreCase(previousGroupOrIndRepeatedForBreak)) {
						if (repeatCounter == 10) {
							break;
						} else {
							previousGroupOrIndRepeatedForBreak = groupNameExceptIndv;
							repeatCounter++;
							logger.fine( "repeatCounter :::: " + repeatCounter);
						}
					}

					logger.fine(
							"Not mached :current group inside successful groups :" + groupNameExceptIndv);

					List<WebElement> elemGroupInfoOrPhOrAnnouncements = driver
							.findElements(By.xpath(getxPathInterface()
									.getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT()));

					if (elemGroupInfoOrPhOrAnnouncements.isEmpty()) {
						logger.fine( "webElement.isEmpty: " + groupNameExceptIndv);
						continue;
					} else {
						String groupInfo = elemGroupInfoOrPhOrAnnouncements.get(0).getText();
						logger.fine( "groupInfo: " + groupInfo);

						if (groupInfo.contains("group info") || groupInfo.contains(",")) {
							if (excludePersonalGroupNamesSet.stream()
									.noneMatch(personalGroup -> (personalGroup.contains(groupNameExceptIndv)
											|| groupNameExceptIndv.contains(personalGroup)))
									&& excludeSupportGroupNamesSet.stream()
											.noneMatch(personalGroup -> (personalGroup.contains(groupNameExceptIndv)
													|| groupNameExceptIndv.contains(personalGroup)))) {
								logger.fine(
										"Not Present in existingSuportGroups and ExclusionList but its a group, So Adding: "
												+ groupNameExceptIndv);

								allNewSupportGroups.add(groupNameExceptIndv);

								if (allNewSupportGroups.size() % 20 == 0) {
									allSuccessfulSupportGroupsFromFile.addAll(allNewSupportGroups);
									WhatsappContactUtils.exportAndMergeToExistingSingleFile("successfulGroups.txt",
											allSuccessfulSupportGroupsFromFile);
								}
							}
						} else if (groupInfo.contains("Announcements")) {
							archiveSupportGrpOrExitAnnouncement(groupNameExceptIndv,
									EnumActionOnRightClickPopUp.EXIT_ANNOUNCEMENTS_GROUP);
						}
					}

				}

			} catch (Exception ex) {
				ex.printStackTrace();
				isPreviousArchived = false;
				Thread.sleep(1000);
				continue;
			}
		}
		System.out
				.println("All Support Gtroup Length limit reached : " + allExistingSupportGroupSize + " : " + counter);
		allSuccessfulSupportGroupsFromFile.addAll(allNewSupportGroups);
		WhatsappContactUtils.exportAndMergeToExistingSingleFile("successfulGroups.txt", allSuccessfulSupportGroupsFromFile);
		return;
	}

	boolean archiveSupportGrpOrExitAnnouncement(String groupNameExceptIndv, EnumActionOnRightClickPopUp actionOnPopUp)
			throws InterruptedException {
		try {
			// removeArchievedOrExitedClosurePopup();//
			Actions actions = new Actions(driver);
			Thread.sleep(200);

			WebElement groupAtLeftPanelTray = null;
			String archiveXpath = "";
			boolean isNSEExceptionOccurred = false;
			boolean isQuestionMarksAfterDecoded = false;

			try {
				if (groupNameExceptIndv.contains("???")) {
					isQuestionMarksAfterDecoded = true;
				} else if (groupNameExceptIndv.contains("  ")) {
					archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART
							+ groupNameExceptIndv.substring(0, groupNameExceptIndv.indexOf("  "))
							+ LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
					groupAtLeftPanelTray = febx(archiveXpath);

				} else if (groupNameExceptIndv.contains("'")) {
					archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART
							+ groupNameExceptIndv.substring(0, groupNameExceptIndv.indexOf("'"))
							+ LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
					groupAtLeftPanelTray = febx(archiveXpath);

				} else if (groupNameExceptIndv.length() > 40) {
					archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART + groupNameExceptIndv.substring(0, 5)
							+ LEFT_PANEL_XREF_TO_ARCHIVE_2ND_PART
							+ groupNameExceptIndv.substring(groupNameExceptIndv.length() - 6)
							+ LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
					groupAtLeftPanelTray = febx(archiveXpath);
				} else {
					archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART + groupNameExceptIndv
							+ LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
					groupAtLeftPanelTray = febx(archiveXpath);
				}
			} catch (NoSuchElementException ex) {
				// archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART+
				// groupNameExceptIndv.substring(0,5) + LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
				// archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART+ groupNameExceptIndv +
				// LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
				// groupAtLeftPanelTray = febx(archiveXpath));
				logger.fine( "NoSuchElementException Occurred in archival : " + ex.getMessage());
				isNSEExceptionOccurred = true;

			} catch (Exception ex) {
				// archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART+
				// groupNameExceptIndv.substring(0,5) + LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
				// archiveXpath = LEFT_PANEL_XREF_TO_ARCHIVE_1ST_PART+ groupNameExceptIndv +
				// LEFT_PANEL_XREF_TO_ARCHIVE_3RD_PART;
				// groupAtLeftPanelTray = febx(archiveXpath));
				logger.fine( "Exception Occurred in archival : " + ex.getMessage());
				isNSEExceptionOccurred = false;

			}
			logger.fine( "Archive xpath " + archiveXpath);
			removeArchievedOrExitedClosurePopups();
			Thread.sleep(100);
			removeArchievedOrExitedClosurePopups();

			/*
			 * if(isQuestionMarksAfterDecoded) { actions.contextClick().perform();//use
			 * selected or highlighted }else {
			 * actions.contextClick(groupAtLeftPanelTray).perform(); }
			 */

			logger.fine( "actions right click Performed");
			Thread.sleep(200);
			if (actionOnPopUp == EnumActionOnRightClickPopUp.ARCHIVE_SUPPORT_GROUP) {
				// actions.sendKeys(Keys.ARROW_DOWN, Keys.ENTER).perform();
				// actions.sendKeys(Keys.CONTROL,Keys.ALT, "E").perform();

				Action keydown = actions.keyDown(Keys.CONTROL).keyDown(Keys.ALT).keyDown(Keys.SHIFT).sendKeys("e")
						.build();
				keydown.perform();

				// groupAtLeftPanelTray.sendKeys(Keys.LEFT_SHIFT,
				// Keys.LEFT_CONTROL,Keys.LEFT_ALT, "e");

				logger.fine( "ARCHIVE_SUPPORT_GROUP : ARROW_DOWN and ENTER Performed");
				Thread.sleep(2000);

			} else if (actionOnPopUp == EnumActionOnRightClickPopUp.EXIT_ANNOUNCEMENTS_GROUP) {
				actions.contextClick(groupAtLeftPanelTray).perform();
				actions.sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER).perform();
				logger.fine( "EXIT_ANNOUNCEMENTS_GROUP : ARROW_DOWN, DOWN, DOWN and ENTER Performed");
			}

			Thread.sleep(200);
			if (actionOnPopUp == EnumActionOnRightClickPopUp.EXIT_ANNOUNCEMENTS_GROUP) {
				febx(getxPathInterface().getTEXT_REQUEST_TO_EXIT()).click();
				Thread.sleep(100);
			}
			removeArchievedOrExitedClosurePopups();
			actions.release();
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
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

	@Override
	public int getExpectedSizeToModuloToInsertExportToFile() {
		return 3;
	}

	@Override
	protected int getMaxAllowedRepeatOfSameGroup() {
		return 30;
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
