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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//Search the phone number of individual consultant and click and send message
public class WA_MsgSenderByPhoneNumber extends Constants
		implements IWhatsappContactExtractorAndMessage {

	private static final boolean isSpecialCharacterSplitRequired = false;
	private XPathInterfaceWATG xPathInterfaceWatg;
	public WA_MsgSenderByPhoneNumber(XPathInterfaceWATG xPathInterfaceWatg) {
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


		Set<String> allPhNumsFromCsv = null;
		Set<String> errorPhoneNums= new TreeSet<>();
		Set<String> allPhoneContacts = new TreeSet<>();

		try {
			Set<String> allGroupsFromCsvSet = new TreeSet<>(WhatsappContactUtils
					.getAllGroupsFromCsv(Paths.get(FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT)));
			// logger.fine( "allGroupsFromCsv :: "+allGroupsFromCsvSet);

			logger.fine( "group_names size :" + allGroupsFromCsvSet.size());
			
			Function<String, String> phNumsSplitReplace = i->i.split("_____")[1].replaceAll("\\+", "").replaceAll("-", "").replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "");
			
			allPhNumsFromCsv = allGroupsFromCsvSet.stream().map(s->phNumsSplitReplace.apply(s)).collect(Collectors.toSet());
			
			logger.fine( "Modified allGroupsFromCsv :: " + allPhNumsFromCsv);

			Thread.sleep(DURATION_DRIVER_SETUP_AND_LOADING_AFTER_SCANNED);

			// search, click, and process the contacts From Groups
			searchClickAndExtractContactsFromGroups(allPhNumsFromCsv, errorPhoneNums,
					GROUP_EXECUTION_INTERVAL_MS, BROADCAST_MESSAGE_TO_SEND);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (yesReporocessErrorList) {
			logger.fine( "Reprocessing error list" + errorPhoneNums);

			// Re-process and retry the error group
			Set<String> errorGroupToReProcess = new HashSet<>(errorPhoneNums);
			searchClickAndExtractContactsFromGroups(errorGroupToReProcess, errorPhoneNums, _ERROR_GROUP_PROCESS_SLEEP_TIME, BROADCAST_MESSAGE_TO_SEND);
		} else {
			logger.fine( "Skipping error list from reprocessing " + errorPhoneNums);

		}

		logger.fine( "ErrorPhoneNum\n" + errorPhoneNums);
		logger.fine( "allPhoneContacts \n" + allPhoneContacts);
		Set<String> successfulGroups = new HashSet<>();
		successfulGroups.addAll(allPhNumsFromCsv);

		successfulGroups.removeAll(errorPhoneNums);
		if (!doWeReallyNeedToSendMessageToEveryGroup) {
			// 4. Export Contacts to a file
			WhatsappContactUtils.exportToFile(getxPathInterface().getSUPPORT_EXTRACTED_CONTACTS(), allPhoneContacts, successfulGroups,
					errorPhoneNums);
		}

	}

	
	private void searchClickAndExtractContactsFromGroups(Set<String> allGroupsFromCsv, Set<String> errorPhoneNums,
			int SLEEP_TIME,String BROADCAST_MESSAGE_TO_SEND) {

		logger.fine( "allGroupsFromCsv.size() Before Processing :: " + allGroupsFromCsv.size());
		logger.fine( "errorGroup" + errorPhoneNums+ " Before Processing with sleep time: " + SLEEP_TIME);

		Collections.synchronizedSet(allGroupsFromCsv);

		int[] counter = { 0 };
		errorPhoneNums.clear();

		try {
			logger.fine( "allGroupsFromCsv :: " + allGroupsFromCsv);
			allGroupsFromCsv.stream().forEach(eachGroupString -> {
				sendMessageOrExtractContactsFromEachGroup(errorPhoneNums, allGroupsFromCsv,SLEEP_TIME, counter, eachGroupString,BROADCAST_MESSAGE_TO_SEND);

			});
		} catch (Exception e) {
			logger.fine( "error external phoneNum: ");
			e.printStackTrace();
		}
	}

	private void sendMessageOrExtractContactsFromEachGroup(Set<String> errorGroup, Set<String> allGroupsFromCsv,int SLEEP_TIME, int[] counter,
			String eachPhNumString,String BROADCAST_MESSAGE_TO_SEND) {
		counter[0] = counter[0] + 1;
		logger.fine( "phoneNumprocssesing " + counter[0] + " Out of total PhoneNums : " + allGroupsFromCsv.size());

		WebElement webElement=null;
		try {
			driver.findElement(By.xpath(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH())).click();
			 webElement = driver.findElement(By.xpath(getxPathInterface().getXPATH_SEARCH()));
			logger.fine( "LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH : " +eachPhNumString);

			try {// 1.Search groupName in SearchBox

				webElement.sendKeys(eachPhNumString + "\n");
				Thread.sleep(SLEEP_TIME);

				webElement.sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER);
				// Send a Message
					logger.fine( "webElementPhoneNumsCheck driver : " +eachPhNumString);

					WebElement webElementPhoneNumsCheck = driver
							.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
					if (webElementPhoneNumsCheck!=null) {
						logger.fine( "webElementPhoneNumsCheck.get : " +eachPhNumString);

						WebElement webElementTypeMsg = driver.findElement(By.xpath(getxPathInterface().getDIV_TITLE_TYPE_A_MESSAGE()));
						webElementTypeMsg.sendKeys(BROADCAST_MESSAGE_TO_SEND);
						driver.findElement(By.xpath(getxPathInterface().getSPAN_DATA_TESTID_SEND())).click();
						Thread.sleep(SLEEP_TIME/2);
				}

				Thread.sleep(SLEEP_TIME);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "exception out of allGroupsFromCsv loop : " + e.getMessage());
				errorGroup.add(eachPhNumString);
			}
			webElement.clear();
		} catch (Exception e) {
			try {
				Thread.sleep(SLEEP_TIME);
				logger.fine("\n\n\n");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();}
	}

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
		 * https://www.youtube.com/watch?v=aAWvwGFkySI
		 *
		 * Examples ----- //label[text()='Email']/following-sibling::input[1]
		 * //td[text()='Maria Anders']/preceding-sibling::td/child::input
		 * //label[text()='Email']/following-sibling::input[1]/parent::div
		 * //div[@class='container']/child::input[@type='text']
		 * //div[@class='container']/descendant::button
		 * //div[@class='buttons']/ancestor-or-self::div
		 * //label[text()='Password']/following::input[1]
		 * */

	public XPathInterfaceWATG getxPathInterface() {
		return xPathInterfaceWatg;
	}


	public void setxPathInterface(XPathInterfaceWATG xPathInterface) {
		this.xPathInterfaceWatg=xPathInterface;
	}


}
