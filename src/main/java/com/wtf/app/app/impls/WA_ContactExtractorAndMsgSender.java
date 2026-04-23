package com.wtf.app.app.impls;

import com.wtf.app.app.com.parent.Constants;
import com.wtf.app.app.commons.InitialSetup;
import com.wtf.app.app.commons.WhatsappContactUtils;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.interfaces.IWhatsappContactExtractorAndMessage;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//Not in Use
public class WA_ContactExtractorAndMsgSender extends Constants
		implements IWhatsappContactExtractorAndMessage {

	private static final boolean isSpecialCharacterSplitRequired = false;
	private XPathInterfaceWATG xPathInterfaceWatg;
	public WA_ContactExtractorAndMsgSender(XPathInterfaceWATG xPathInterfaceWatg) {
		super(xPathInterfaceWatg.getBASE_URL(), SocialType.WHATSAPP);
		this.xPathInterfaceWatg=xPathInterfaceWatg;
	}

	Logger logger = InitialSetup.getLogger();

	@Override
	public void updateContactsOrBroadcastMessages(String FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT,
			Boolean doWeReallyNeedToSendMessageToEveryGroup, String BROADCAST_MESSAGE_TO_SEND)
			throws InterruptedException, IOException {



		InitialSetup initialSetup = InitialSetup.getInstance(xPathInterfaceWatg.getBASE_URL(), SocialType.WHATSAPP);
		initialSetup.getDriver();


		Set<String> allGroupsFromCsv = null;
		Set<String> errorGroup = new TreeSet<>();
		Set<String> allPhoneContacts = new TreeSet<>();
		Set<String> specialCharsToSplit = null;

		try {
			Set<String> allGroupsFromCsvSet = new TreeSet<>(WhatsappContactUtils
					.getAllGroupsFromCsv(Paths.get(FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT)));
			// logger.fine( "allGroupsFromCsv :: "+allGroupsFromCsvSet);

			logger.fine( "group_names size :" + allGroupsFromCsvSet.size());

			List<String> allGroupsFromCsvList = new CopyOnWriteArrayList<>(allGroupsFromCsvSet);

			if(isSpecialCharacterSplitRequired) {
				specialCharsToSplit = specialCharacterSplitting(allGroupsFromCsvList);
			}

			allGroupsFromCsv = new LinkedHashSet<>(allGroupsFromCsvList);// .stream().sorted().distinct().collect(Collectors.toSet());
			logger.fine( "Modified allGroupsFromCsv :: " + allGroupsFromCsv);
			// allGroupsFromCsv =
			// allGroupsFromCsv.stream().skip(60).collect(Collectors.toSet());

			// Create a backup file and Update existing with sorted list
			// Create/update New File with current Date
			// WhatsappContactUtils.readAndUpdateExistingContactsPresentInFileToMerge(Paths.get("group_names_sorted_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"))+".txt"),allGroupsFromCsv);

			Thread.sleep(DURATION_DRIVER_SETUP_AND_LOADING_AFTER_SCANNED);

			// search, click, Extract and process the contacts From Groups
			searchClickAndExtractContactsFromGroups(allGroupsFromCsv, errorGroup, allPhoneContacts, specialCharsToSplit,
					GROUP_EXECUTION_INTERVAL_MS, doWeReallyNeedToSendMessageToEveryGroup, BROADCAST_MESSAGE_TO_SEND);

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * // Enter data using BufferReader BufferedReader reader = new
		 * BufferedReader(new InputStreamReader(System.in));
		 *
		 * int x = 5; // wait 5 seconds at most long startTime =
		 * System.currentTimeMillis(); while ((System.currentTimeMillis() - startTime) <
		 * x * 2000 && !reader.ready()) { }
		 *
		 * if (reader.ready()) { logger.fine( "You entered: " +
		 * reader.readLine()); // Reading data using readLine
		 * yesOrNoToReporocessErrorList = reader.readLine(); }
		 */

		if (yesReporocessErrorList) {
			logger.fine( "Reprocessing error list" + errorGroup);

			// Re-process and retry the error group
			Set<String> errorGroupToReProcess = new HashSet<>(errorGroup);
			searchClickAndExtractContactsFromGroups(errorGroupToReProcess, errorGroup, allPhoneContacts,
					specialCharsToSplit, _ERROR_GROUP_PROCESS_SLEEP_TIME, doWeReallyNeedToSendMessageToEveryGroup,
					BROADCAST_MESSAGE_TO_SEND);
		} else {
			logger.fine( "Skipping error list from reprocessing " + errorGroup);

		}

		logger.fine( "ErrorGroup \n" + errorGroup);
		logger.fine( "allPhoneContacts \n" + allPhoneContacts);
		Set<String> successfulGroups = new HashSet<>();
		successfulGroups.addAll(allGroupsFromCsv);

		successfulGroups.removeAll(errorGroup);
		if (!doWeReallyNeedToSendMessageToEveryGroup) {
			// 4. Export Contacts to a file
			WhatsappContactUtils.exportToFile(getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS(), allPhoneContacts, successfulGroups,
					errorGroup);
		}

	}

	Set<String> specialCharacterSplitting(List<String> allGroupsFromCsvList) throws FileNotFoundException, IOException {
		Set<String> specialCharsToSplit;
		specialCharsToSplit = WhatsappContactUtils
				.getAllGroupsFromCsv(Paths.get(GROUP_NAMES_SPECIAL_CHAR_TO_SPLIT_EXTRACTOR_CLASS_REF_TXT));

		String specialCharsToSplitFormat = String.join("|\\", specialCharsToSplit);


		for (String eachGroup : allGroupsFromCsvList) {
			for (String eachSpecialChar : specialCharsToSplit) {
				if (eachGroup.contains(eachSpecialChar)) {
					List<String> splittedList = Arrays.asList(eachGroup.split(specialCharsToSplitFormat));
					splittedList = splittedList.stream().map(i -> i.replace("", "")).map(i -> i.trim())
							.filter(j -> j.length() > 2).collect(Collectors.toList());
					allGroupsFromCsvList.addAll(splittedList);
					allGroupsFromCsvList.remove(eachGroup);
				}
			}
		}
		return specialCharsToSplit;
	}

	private void searchClickAndExtractContactsFromGroups(Set<String> allGroupsFromCsv, Set<String> errorGroup,
			Set<String> allPhoneContacts, Set<String> specialCharsToSplit, int SLEEP_TIME,
			Boolean doWeReallyNeedToSendMessageToEveryGroup, String BROADCAST_MESSAGE_TO_SEND) {

		logger.fine( "allGroupsFromCsv.size() Before Processing :: " + allGroupsFromCsv.size());
		logger.fine( "errorGroup" + errorGroup + " Before Processing with sleep time: " + SLEEP_TIME);

		Collections.synchronizedSet(allGroupsFromCsv);

		int[] counter = { 0 };
		errorGroup.clear();

		try {
			logger.fine( "allGroupsFromCsv :: " + allGroupsFromCsv);
			allGroupsFromCsv.stream().filter(admin-> !admin.contains("Study") && !admin.contains("Python_training")).skip(3).limit(600).forEach(eachGroupString -> {
				sendMessageOrExtractContactsFromEachGroup(errorGroup, allGroupsFromCsv, allPhoneContacts,
						specialCharsToSplit, SLEEP_TIME, counter, eachGroupString,
						doWeReallyNeedToSendMessageToEveryGroup, BROADCAST_MESSAGE_TO_SEND);

			});
		} catch (Exception e) {
			logger.fine( "error external group : ");
			e.printStackTrace();
		}

		logger.fine( "allPhoneContacts.size() Now :: " + allPhoneContacts.size());
	}

	private void sendMessageOrExtractContactsFromEachGroup(Set<String> errorGroup, Set<String> allGroupsFromCsv,
			Set<String> allPhoneContacts, Set<String> specialCharsToSplit, int SLEEP_TIME, int[] counter,
			String eachGroupString, Boolean doWeReallyNeedToSendMessageToEveryGroup, String BROADCAST_MESSAGE_TO_SEND) {
		counter[0] = counter[0] + 1;
		logger.fine( "group procssesing " + counter[0] + " Out of total groups : " + allGroupsFromCsv.size());

		try {
			Thread.sleep(SLEEP_TIME);
		if (!doWeReallyNeedToSendMessageToEveryGroup) {
			if (counter[0] % MINIMUM_EXPORT_COUNT_INTERVAL == 0) {

				// Start Exporting Contacts to a file
				WhatsappContactUtils.exportToFile(getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS(), allPhoneContacts, allGroupsFromCsv,
						errorGroup);
			}

		}}catch(Exception ex) {
			logger.fine( "exception exporting loop : " + ex.getMessage());
		}
		WebElement webElement=null;
		try {
			driver.findElement(By.xpath(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH())).click();
			 webElement = driver.findElement(By.xpath(getxPathInterface().getXPATH_SEARCH()));
			logger.fine( "LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH : " +eachGroupString);

			try {// 1.Search groupName in SearchBox

				webElement.sendKeys(eachGroupString + "\n");
				Thread.sleep(SLEEP_TIME);
				// driver.manage().timeouts().implicitlyWait(SLEEP_TIME, TimeUnit.MILLISECONDS);

				// Send a Message
				if (doWeReallyNeedToSendMessageToEveryGroup) {
					logger.fine( "webElementGroupNameCheck driver : " +eachGroupString);

					// logger.fine( "Do We Really Need To Send Message To EveryGroup
					// ????????????????? STOP IT");
					WebElement webElementGroupNameCheck = driver
							.findElement(By.xpath("//span[contains(@title,'" + eachGroupString.trim() + "')]"));
					if (!webElementGroupNameCheck.getText().isBlank()) {
						logger.fine( "webElementGroupNameCheck.get : " +eachGroupString);

						WebElement webElementTypeMsg = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
						webElementTypeMsg.sendKeys(BROADCAST_MESSAGE_TO_SEND);
						driver.findElement(By.xpath(getxPathInterface().getSPAN_DATA_TESTID_SEND())).click();
						Thread.sleep(SLEEP_TIME/2);
					}
				}

				Thread.sleep(SLEEP_TIME);
				if (!doWeReallyNeedToSendMessageToEveryGroup) {//changed
					logger.fine( "Error processAfterSearchBoxAndSelectionAndMosehoverContacts : " +eachGroupString);
					processAfterSearchBoxAndSelectionAndMousehoverContacts(errorGroup, SLEEP_TIME, eachGroupString,
							specialCharsToSplit, webElement);
				}

			} catch (Exception e) {
				logger.fine( "exception out of allGroupsFromCsv loop : " + e.getMessage());
				errorGroup.add(eachGroupString);
			}

			// 3.Extract Message with ContctNumbers using xpath in
			// developer
			// tools

			if (!doWeReallyNeedToSendMessageToEveryGroup) {
				logger.fine( "Error extractContactsByGroupMouseHover : " +eachGroupString);
				//extractContactsByGroupMouseHover(allPhoneContacts, SLEEP_TIME);:todo
			}
			webElement.clear();
		} catch (Exception e) {
			try {
			Thread.sleep(3000);
			if(webElement!=null) {
			webElement.clear();
			}
			driver.findElement(By.xpath("/html/body/div[1]/div/div/div[4]/div/div[1]/div/div/button/div[2]/span")).click();
			}catch(Exception ex) {
				 logger.fine( "Error removeArchievedOrExitedClosurePopup : " +ex.getLocalizedMessage());
			}
			errorGroup.add(eachGroupString);
			 logger.fine( "Error LABEL_CLASS_XPATH or XPATH_SEARCH : " +eachGroupString);
		}
	}

	private void processAfterSearchBoxAndSelectionAndMousehoverContacts(Set<String> errorGroup, int SLEEP_TIME,
																		String eachGroupString, Set<String> specialCharsToSplit, WebElement webElement)
			throws InterruptedException {
		// 2.Click on searched result group
		try {
			// groupName exact Match without icon char
			driver.findElement(By.xpath("//span[@title='" + eachGroupString + "']")).click();
		} catch (Exception ex) {
			try {
				// logger.fine( "Start Searching with starts-with : "
				// +line);

				// span[starts-with(@title,'Azure Devops Training')]
				driver.findElement(By.xpath("//span[starts-with(@title, '" + eachGroupString + "')]")).click();
			} catch (Exception e) {
				// logger.fine( "Start Searching with contains : " +line);
				try {
					// for icon char.So contains check
					driver.findElement(By.xpath("//span[contains(@title, '" + eachGroupString.trim() + "')]")).click();
				} catch (Exception ee) {
					// logger.fine( "still in Searching with extraspaces :
					// " + line);
					StringBuilder xPathContains = new StringBuilder();

					try {

						xPathContains.append("//span[contains(@title,'");

						String[] words = eachGroupString.split("  ");


						for (int i = 0; i < words.length; i++) {
							xPathContains = xPathContains.append(words[i] + "') ");
							if (i != words.length - 1) {
								xPathContains.append("and contains(@title,'");
							}
						}
						xPathContains.append("]");

						// for extra spaces char. So multiple contains check
						driver.findElement(By.xpath(xPathContains.toString())).click();

						// driver.findElement(By.xpath("//*[text()="+ line +
						// "')]").click();
					} catch (Exception eee) {
						try {
							// logger.fine( "final try with 2 dropdown
							// error group : " + line);
							webElement.clear();
							webElement.sendKeys(eachGroupString);
							Thread.sleep(SLEEP_TIME);

							/*
							 * Select dropdown = new Select(webElement); dropdown.selectByIndex(0);
							 * logger.fine( dropdown.getOptions()+"::"+ dropdown.isMultiple());
							 */

							webElement.sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER);
							Thread.sleep(SLEEP_TIME);

							// WebElement element =
							// driver.findElement(By.name("text"));
							/*
							 * String text = webElement.getAttribute("value"); logger.fine( text);
							 */

							/* Search Group and click on group in search result is done */

							counter++;
							if (counter > 1) {
								throw new Exception();
							}
							processAfterSearchBoxAndSelectionAndMousehoverContacts(errorGroup, SLEEP_TIME,
									eachGroupString, specialCharsToSplit, webElement);

						} catch (Exception exx) {
							try {
								// logger.fine( "final try with 3 dropdown
								// error group : " + line);
								webElement.clear();
								webElement.sendKeys(eachGroupString);
								Thread.sleep(SLEEP_TIME);
								webElement.sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER);
								Thread.sleep(SLEEP_TIME);

								counter++;
								if (counter > 2) {
									counter = 0;
									throw new Exception();
								}
								processAfterSearchBoxAndSelectionAndMousehoverContacts(errorGroup, SLEEP_TIME,
										eachGroupString, specialCharsToSplit, webElement);

							} catch (Exception exxx) {
								try {
									// Checks on single space due to img to text
									// conversion issue
									xPathContains.append("//span[contains(@title,'");
									String[] words = null;

									for (int i = 0; i < words.length; i++) {
										xPathContains = xPathContains.append(words[i] + "') ");
										if (i != words.length - 1) {
											xPathContains.append("and contains(@title,'");
										}
									}
									xPathContains.append("]");

									// for extra spaces char. So multiple
									// contains check
									driver.findElement(By.xpath(xPathContains.toString())).click();
								} catch (Exception exxxx) {
									/*
									 * logger.fine("Error in Adding after Searching : " + line + " exxx : "
									 * + exxx.getMessage());
									 */
									logger.fine( "Adding into errorgroup after Searching groupName: "
											+ eachGroupString + " XPATH : " + xPathContains.toString());
									errorGroup.add(eachGroupString);
									webElement.clear();
									return;
								}
							}
						}
						Thread.sleep(SLEEP_TIME);
					}
				}
			}
		}
	}






	{

		// Temp://*[@aria-label='Profile picture, disappearing messages on']

		//// *[@aria-label='Profile picture, disappearing messages
		//// on']/parent::div[1]/following::span[@dir='auto' and @title and @class]
		// *[@aria-label='Profile picture, disappearing messages
		//// on']/parent::div[1]/parent::div[1]/following-sibling::div[1]/child::div[1]/child::div[1]/child::span[1]
		// span[@data-testid="default-group" and @data-icon="default-group" ]

		// *[@aria-label='Profile picture, disappearing messages
		// on']/parent::div[1]/parent::div[1]/following-sibling::div[1]/child::div[1]/child::div[1]/child::span[1]
		// | //span[@data-testid="default-group" and @data-icon="default-group" ]

		// https://www.youtube.com/watch?v=MjtJeDbSeSc

		// https://www.youtube.com/watch?v=9N7ERYWbjuw
		/*
		 * Use CTRL + F to open xpath search string window
		 *
		 * Xpath=//tagname[@attribute='value']
		 *
		 * 1. //*[@id='Manas'] 2. //input[@id='Manas'] or //input[@type='Text']
		 * 3.//input[@id='Manas'] | //input[@type='Text'] 4. //input[@id='Manas'] and
		 * //input[@type='Text'] 5. (//*[Text()='Click here to search']) [1]
		 * 6.//*[contains(@title,' JOB SUPPORTS')]
		 * 7.//div[contains(@class,'aui-column-content-last')]
		 * 8.//div[contains(@class,'atag') and contains(@class ,'btag')]
		 *
		 * Contains(),Using OR & AND,Starts-with(), Text(),
		 * Following,Ancestor,Child,Preceding,Following-sibling,Parent, Self,descendant
		 * Xpath=//*[@type='text']//following::input[1] Xpath=//*[text()='Enterprise
		 * Testing']//ancestor::div[1] Xpath=//*[@id='java_technologies']//child::li
		 * Xpath=//*[@type='submit']//preceding::input
		 * xpath=//*[@type='submit']//following/preceding-sibling::input
		 * Xpath=//*[@id='rt-feature']//parent::div Xpath
		 * =//*[@type='password']//self::input
		 * Xpath=//*[@id='rt-feature']//descendant::a //span[starts-with(text(),':')]
		 *
		 * https://www.guru99.com/xpath-selenium.html
		 *
		 * //span[@dir='auto' and @title]//self::text() //span[@dir='auto' and @title
		 * and @class]//self::span
		 *
		 *
		 * https://testsigma.com/blog/scrolling-in-selenium/
		 *
		 * driver.findElement(By.
		 * xpath("//div[@class='seg2_formBox']//span[@class='mbs2_formError' and not(@class='ng-hide')]//preceding::label[1]"
		 * )).sendKeys("Manoj Soundarrajan");
		 *
		 *
		 *
		 *
		 * https://www.youtube.com/watch?v=aAWvwGFkySI
		 *
		 * Examples ----- //label[text()='Email']/following-sibling::input[1]
		 * //td[text()='Maria Anders']/preceding-sibling::td/child::input
		 * //label[text()='Email']/following-sibling::input[1]/parent::div
		 * //div[@class='container']/child::input[@type='text']
		 * //div[@class='container']/descendant::button
		 * //div[@class='buttons']/ancestor-or-self::div
		 * //label[text()='Password']/following::input[1]
		 *
		 * //production[not(contains(category,'Business'))]
		 *
		 *
		 * //new WebDriverWait(driver,
		 * 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
		 * "//span[text()='index.html']")));
		 * //je.executeScript("arguments[0].scrollIntoView(true);",element);
		 *
		 */

		/* <Element attribute1="abc" attribute2="xyz">Data</Element> */
		// Element[@attribute1="abc" and @attribute2="xyz" and text()="Data"]

	}
	// public static void main(String[] args) throws InterruptedException,
	// IOException {


	// To Get the updated group names

	/*
	 * 1. capture scrolling element in whatsapp group names using portable
	 * faststone. D:\SupportDetails\Support_ExtractSupportProxyContacts\FSCapture94
	 * https://www.faststone.org/FSCapturerDownload.htm
	 */

	/*
	 * 2. and then convert the img to text/pd using ocr online tool
	 * https://www.ocr2edit.com/result
	 */

	// 3.
	// WhatsappCollectTheGroupNamesFromTextFromManualScreenshot.getAllTheGroupNamesTruncatingInitialContacts();

	// updateContacts();

	// }
	// button[@data-testid='popup-controls-ok']//child::div[@data-testid='content'
	// and text()='Join group']
	// a[contains(@href,'https://chat.whatsapp.com/')]

	// Copy paste with Actions

	/*
	 * Keys cmdCtrl = Platform.getCurrent().is(Platform.MAC) ? Keys.COMMAND :
	 * Keys.CONTROL;
	 *
	 * WebElement textField = driver.findElement(By.id("textInput")); new
	 * Actions(driver) .sendKeys(textField, "Selenium!") .sendKeys(Keys.ARROW_LEFT)
	 * .keyDown(Keys.SHIFT) .sendKeys(Keys.ARROW_UP) .keyUp(Keys.SHIFT)
	 * .keyDown(cmdCtrl) .sendKeys("xvv") .keyUp(cmdCtrl) .perform();
	 *
	 * Assertions.assertEquals("SeleniumSelenium!",
	 * textField.getAttribute("value"));
	 */



	public XPathInterfaceWATG getxPathInterface() {
		return xPathInterfaceWatg;
	}


	public void setxPathInterface(XPathInterfaceWATG xPathInterface) {
	}


}
