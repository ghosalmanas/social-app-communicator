package com.wtf.app.app.com.parent;

import com.wtf.app.app.commons.InitialSetup;
import com.wtf.app.app.enums.SocialType;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public abstract class Constants {

	public static final int MINIMUM_EXPORT_COUNT_INTERVAL = 50;
	public static final int DURATION_DRIVER_SETUP_AND_LOADING_AFTER_SCANNED = 60000;//120000
	public static final int _ERROR_GROUP_PROCESS_SLEEP_TIME = 7000;
	public static final int GROUP_EXECUTION_INTERVAL_MS = 6000;
	public final static Logger logger = InitialSetup.getLogger();

	public static int counter = 0;
	public static Boolean yesReporocessErrorList = false;

	public InitialSetup initialSetup;
	public static WebDriver driver = null;
	WebDriverWait wait;

	public Constants(String baseURL, SocialType socialType) {
		if(driver==null) {//Browser keeps open even after program ends
			initialSetup = InitialSetup.getInstance(baseURL,socialType);
			driver=initialSetup.getDriver();
			wait = new WebDriverWait(driver, Duration.ofSeconds(240l));
		}
	}

	protected static String ERROR_GROUPS_TXT = "errorGroups.txt";
	protected static String ALL_SUPPORT_GROUPS = "successfulGroups.txt";
	protected Set<String> allSuccessfulSupportGroupsFromFile = extractListOfDataFromFile(ALL_SUPPORT_GROUPS);

	protected String EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV = "exclude_personal_group_names_always.txt";
	protected final Set<String> excludePersonalGroupNamesSet = extractListOfDataFromFile(EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV);

	protected String EXCLUDE_SUPPORT_GROUP_NAMES_FROM_CSV = "exclude_group_support_names_always.txt";
	protected final Set<String> excludeSupportGroupNamesSet = extractListOfDataFromFile(EXCLUDE_SUPPORT_GROUP_NAMES_FROM_CSV);

	protected String EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV = "exclude_group_contact_names_always_for_the_day.txt";//to avoid newly added/created personal group or newly joined members
	protected Set<String> excludePersonalGroupContactNamesOrNumbersSet = extractListOfDataFromFile(EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV);

	protected String NEGLECT_TO_JOIN_GROUPS_WITH_TEXT = "neglects_group_names_text_to_join_always.txt";
	private Set<String> neglectToJoinGroupsWithTextSet = extractListOfDataFromFile(NEGLECT_TO_JOIN_GROUPS_WITH_TEXT);
	protected final Set<String> neglectToJoinGroupsWithTextSetLowerCase=neglectToJoinGroupsWithTextSet.parallelStream().map(i->i.toLowerCase()).collect(Collectors.toSet());

	private static String ALREADY_JOINED_GROUP_NAMES_STR = "watg_group_join_links_already_joined.txt";
	protected Set<String> alreadyJoinedGroupsSet = extractListOfDataFromFile(ALREADY_JOINED_GROUP_NAMES_STR);

	protected String BROADCAST_GROUPS = "watg_group_names.txt";
	protected Set<String> broadcastGroupsSet = extractListOfDataFromFile(BROADCAST_GROUPS);
	public  static final String GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG="group_traverse_already_date_";
	protected static String GROUP_NAMES_SPECIAL_CHAR_TO_SPLIT_EXTRACTOR_CLASS_REF_TXT = "group_names_specialChar_to_split_ExtractorClassRef.txt";

	private static String GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG = "watg_groups_already_message_sent_for_the_day.txt";//avoid sending same message multiple times to the same group, clean the data is date differs
	protected Set<String> groupsAlreadyMessageSentForTheDaySetWA = extractListOfDataFromFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG);

	/* Not required as skipping as single users. for future use case*/
	private static String GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA = "watg_groups_and_admins_already_traversed_for_the_day.txt";//avoid processing same admins groups every time
	protected Set<String> groupsAdminsAlreadyTraversedForTheDaySetWA = extractListOfDataFromFile(GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA);


	/* for Facebook */
	private final static String BROADCAST_GROUP_ID_NAMES_FB = "fb_group_id_name_mapping_support_proxy.txt";
	protected static Set<String> broadcastGroupIdNameFBSet = extractListOfDataFromFile(BROADCAST_GROUP_ID_NAMES_FB);

	private final static String BROADCAST_GROUP_ID_PENDING_POST_PILED_GROUPS_FB = "fb_groups_pending_posts_message_piled.txt";
	protected static Set<String> pendingPostPiledGroupsFB_set = extractListOfDataFromFile(BROADCAST_GROUP_ID_PENDING_POST_PILED_GROUPS_FB);

	private final static String BROADCAST_SENSITIVE_GROUP_ID_NAMES_FB = "fb_sensitive_group_id_name_mapping_training.txt";
	protected static Set<String> broadcastSensitiveGroupIdNameFBSet = extractListOfDataFromFile(BROADCAST_SENSITIVE_GROUP_ID_NAMES_FB);

	protected final static String GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB = "fb_groups_already_message_sent_forTheDay.txt";//avoid sending same message multiple times to the same group, clean the data is date differs
	protected final static String GROUPS_PENDING_POSTS_MESSAGE_PILED_UP_FB = "fb_groups_pending_posts_message_piled.txt";
	protected Set<String> groupsAlreadyMessageSentForTheDaySetFB = extractListOfDataFromFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB);
	protected String GROUP_STRING_TRAVERSE_ALREADY_DATE_FB="group_traverse_already_date_fb_";

	public final static String _TEST_SUITE="_TEST_SUITE";
	public static Function<String,String> runningTestSuiteApenderFunction = fileName-> fileName.replace(".",(_TEST_SUITE+"."));
	public Supplier<String> getLocalTimeInFormat = ()-> LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("ddMMyyyy-HH:MM"));
	public Supplier<String> getLocalDateOnlyInFormat = ()-> LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("ddMMyyyy"));



	public static Set<String> extractListOfDataFromFile(String groupNamesFromCsv) {
		Set<String> fetchAllFromFile = null;
		try {
			fetchAllFromFile = getPersonalGroupListFromCSV("dbFiles/" + groupNamesFromCsv).parallelStream().collect(Collectors.toSet());
			//logger.fine( "data extracted from " + groupNamesFromCsv + " size:"+ fetchAllFromFile.size() + " : " + fetchAllFromFile);
		} catch (Exception ex) {
			logger.log(Level.SEVERE,"Exception in reading file extractListOfDataFromFile : " + ex.getLocalizedMessage());
		}
		return fetchAllFromFile;
	}


	public static Set<String> getPersonalGroupListFromCSV(String considerGroupNamesFromCsv) {

		Path path = Paths.get(considerGroupNamesFromCsv);
		Charset charset = StandardCharsets.ISO_8859_1;
		Set<String> excludedPersonalGroupList = new CopyOnWriteArraySet<>();
		String line = null;
		String lastExecutedLine=null;

		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			while ((line=reader.readLine())!= null) {
				lastExecutedLine=line;
				excludedPersonalGroupList.add(line);
			}
		} catch (IOException e) {
			System.out.println("Last Executed Line for debugging : "+lastExecutedLine);
			e.printStackTrace();
		}
		System.out.println("getPersonalGroupListFromCSV Completed..."+considerGroupNamesFromCsv);
		return excludedPersonalGroupList;
	}


	@FunctionalInterface
	public interface CustomFunction {
		Object execute();
	}

	public static boolean containsOnlyPrintableAscii(String text) {
		if (text == null) {
			return false; // Or throw an IllegalArgumentException, depending on your null policy
		}
		for (char c : text.toCharArray()) {
			if (!(c >= 32 && c <= 126)) {
				logger.severe("Junk character found in text: "+ text);
				return false; // Found a character outside the allowed range
			}
		}
		return true; // All characters are within the allowed range
	}

	public static String removeJunkCharactersIfExists(String text) {
		if(containsOnlyPrintableAscii(text)) return text;
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			// Check if it's a letter, digit, whitespace, or basic printable character like +,?,% all.
			if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) ||
					(c >= 32 && c <= 126)) { // ASCII printable range
				sb.append(c);
			}
		}
		return sb.toString();
	}

	protected Optional<Object> runTC(CustomFunction func) {
		try {
			return Optional.of(func.execute());
		} catch (Exception ex) {
			logger.fine( "Function execution Failed : " + func.toString());
		}
		return Optional.ofNullable(null);
	}


	protected String waitIdentifyAndClickOnThePinnedElement(String waPinnedSpanTitle) throws InterruptedException {

		try {
			WebElement element = waitTillClickable(waPinnedSpanTitle);
			// interact with your element
			element.click();
			Thread.sleep(1000);
			return element.getText();

		} catch (Exception ex) {
			System.out
					.println("Exception occurred in waitIdentifyAndClickOnThePinnedElement" + ex.getLocalizedMessage());
			WebElement findElement = febx(waPinnedSpanTitle);
			findElement.click();
			Thread.sleep(1000);
			return findElement.getText();
		}
	}


	protected WebElement febx(String xPath) {
		logger.fine("febx XPath :"+xPath);
		return driver.findElement(By.xpath(xPath));
		//return waitTillVisible(xPath); reliable for self healing but very slow
	}

	protected List<WebElement> febxs(String xPath) {
		logger.fine("febxs XPath :"+xPath);
		return driver.findElements(By.xpath(xPath));
	}

	/* Use this as much as possible */
	protected boolean febxsAndClick(String xPath, String xPathAlternate, boolean clickOnAllIfMultiplesFound, String callerBasicInformation) {

		logger.fine("febxsAndClick invoked...........xPath:"+xPath+" xPathAlternate (if 1st one returns empty) :"+xPathAlternate +" clickOnAllIfMultiplesFound:"+clickOnAllIfMultiplesFound+" : callerBasicInformation :"+ callerBasicInformation);

		try {
			Thread.sleep(2000);
			List<WebElement> webElements = febxs(xPath);
			if(!webElements.isEmpty()) {
				if(clickOnAllIfMultiplesFound) {
					try {
						webElements.forEach(WebElement::click);
						logger.fine("febxsAndClick all webElements are clicked....size " + webElements.size() + " : " + callerBasicInformation);
						Thread.sleep(1000);
						return true;
					}catch (Exception ex) {
						logger.fine("febxsAndClick all webElements are not clicked....size " + webElements.size() + " : " + callerBasicInformation);
						ex.printStackTrace();
					}
				}
				try {
					webElements.getFirst().click();
					logger.fine("febxsAndClick the particular webElement is clicked....size " + webElements.size() + " : " + callerBasicInformation);
					Thread.sleep(2000);
					return true;
				}catch (Exception ex) {
					logger.fine("febxsAndClick the particular webElement is not clicked....size " + webElements.size() + " : " + callerBasicInformation);
					ex.printStackTrace();
				}
			}
			if(xPathAlternate!=null && !xPathAlternate.isEmpty()) {
				Thread.sleep(2000);
				logger.fine("febxsAndClick NO webElement found....trying xPathAlternate xPath "+xPathAlternate+" : "+ callerBasicInformation);
				febxsAndClick(xPathAlternate, null,false,"Called_Recursion_For_Alternate_xPath");
			}
		}catch(org.openqa.selenium.ElementClickInterceptedException ex) {
			logger.fine("febxsAndClick ElementClickInterceptedException occurred \n"+ex+" : "+ callerBasicInformation);
			actionsKeyPerform(Keys.ESCAPE);
		}catch(Exception ex) {
			logger.fine("febxsAndClick EXCEPTION occurred \n"+ex+" : "+ callerBasicInformation);
			actionsKeyPerform(Keys.ESCAPE);
			ex.printStackTrace();
		}

		logger.fine("febxsAndClick NONE webElement found for xPath "+xPathAlternate);
		return false;
	}

	public void actionsKeyPerform(Keys keys) {
		Actions actions = new Actions(driver);
		actions.sendKeys(keys).perform();
		actions.release();
	}

	public WebElement waitTillVisible(String xPath) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
	}

	public WebElement waitTillClickable(String waPinnedSpanTitle) {
		return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(waPinnedSpanTitle)));
	}


	public static String getALREADY_JOINED_GROUP_NAMES_STR() {
		return ALREADY_JOINED_GROUP_NAMES_STR;
	}


	public static void setALREADY_JOINED_GROUP_NAMES_STR(String aLREADY_JOINED_GROUP_NAMES_STR) {
		ALREADY_JOINED_GROUP_NAMES_STR = aLREADY_JOINED_GROUP_NAMES_STR;
	}


	public static String getGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG() {
		return GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG;
	}


	public static void setGROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG(
			String gROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG) {
		GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG = gROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_WATG;
	}


	public static String getGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA() {
		return GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA;
	}


	public static void setGROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA(
			String gROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA) {
		GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA = gROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT_WA;
	}




}
