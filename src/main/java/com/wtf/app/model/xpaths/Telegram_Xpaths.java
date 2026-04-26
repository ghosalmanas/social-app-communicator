package com.wtf.app.model.xpaths;

import com.wtf.app.model.enums.SocialType;
import org.openqa.selenium.Keys;

import java.awt.event.KeyEvent;
import java.util.function.Function;
import java.util.function.Predicate;

public class Telegram_Xpaths implements XPathInterfaceWATG {

    private static final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS = null;
    private final String BASE_URL = "https://web.telegram.org/a/";
    // "//div[@class='chat-info-wrapper']//child::div[contains(@class,'hat') and
    // contains(@class,'nfo')]//child::div[@class='info']";

    /* DECIDE THE XPATH in below FORMAT ONLY so that its easy and standard format to find any descendant and easy recovery when ** tag or html or tag label changes***/

    /* CONSIDER the whole page is divided into
     * 1.HEADER WITH SEARCH BAR
     * 1.LEFT_SCROLL_BAR Chat-List Panel
     * 2.RIGHT CHAT GROUP STATUS PANEL
     *
     * Preferred: //* based, text() based, contains with and to avoid duplicates, descendant/anscester based
     * NOW Prepare the specific xpath in Left Panel by  //*[contains[text = Chat-List and other]/descendant::tag[] use tree and branch structure and each constant will be an xpath so that only two constant value can construct the whole xpath
     * Less recommended : create chunk and assign in the constant variable and reuse so that change in one constant can be reflected everywhere
     * Print all the constant in getter before returning to see the value in log
     *  */

    private final String LEFT_PANEL_CHAT_LIST_STARTING_POINT_HEAD_DECIDER_PRIME_COMPONENT = "//*[contains(@class, 'chat-list') and contains(@class, 'custom-scroll')]";
    private final String LEFT_PANEL_CLICKABLE_CHAT_ITEM_IN_CHAT_LIST_APPENDER = "/ancestor::div[@class='info']/preceding-sibling::div[contains(@class,'ripple-container')]";

    private final String RIGHT_PANEL_GROUP_PROFILE = "@class='group-status'";
    private final String RIGHT_PANEL_MEMBERS = "contains(text(),'members')";//try to make each a single xpath
    private final String RIGHT_PANEL_SUBSCRIBERS = "contains(text(),'subscribers')";

    private final String RIGHT_PANEL_GROUP_PROFILE_STARTING_POINT_HEAD_DECIDER_PRIME_COMPONENT = RIGHT_PANEL_GROUP_PROFILE + " and (" + RIGHT_PANEL_MEMBERS + " or " + RIGHT_PANEL_SUBSCRIBERS + ")";


    /*Utility variables and appenders*/
    private final Function<String, String> IF_ANY_NODE_CONTAINS_TEXT_TO_LOWERCASE = (uniqueLowercaseText) -> "//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + uniqueLowercaseText + "')]";

    private final String SUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT = "//span[" + RIGHT_PANEL_GROUP_PROFILE_STARTING_POINT_HEAD_DECIDER_PRIME_COMPONENT + "]";
    // FIND_NEXT_CHAT_IND_AFTER_CLICK_GROUP_NAME_RIGHT_TOP_P2C+
    // "//following::div[1]//child::span[@title]";

    private final String FIND_NAME_RIGHT_TOP_APPENDER = "/../preceding-sibling::div/descendant::h3[contains(@class,'fullName')]";
    private final String FIND_GROUP_NAME_RIGHT_TOP = SUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT + FIND_NAME_RIGHT_TOP_APPENDER;
    private final String FIND_ADMIN_GROUP_NAME_RIGHT_TOP = "//span[@class='group-status' and contains(text(),'subscribers') or text()='Sending messages is not allowed in this group.']";//+FIND_NAME_RIGHT_TOP_APPENDER;

    //Gert Full GroupName
    // "/html/body/div[1]/div/div[2]/div[1]/div/div[2]/div[1]/div[1]/div/div[2]/div[1]/div/span";
    // FIND_NEXT_CHAT_IND_AFTER_CLICK_GROUP_NAME_RIGHT_TOP_P2C+"//descendant::span[@class='group-status'
    // and (contains(text(),'members') or contains(text(),'subscribers'))]";
    // "//div[contains(@class,'ChatInfo']//descendant::h3[contains(@class,'fullName')]";
    private final String FIND_INDV_USER_NAME_RIGHT_TOP = "//span[@class='user-status' ]" + FIND_NAME_RIGHT_TOP_APPENDER;
    private final String LEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS = "//*[contains (@class,'chat-list')]";
    private final String LEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN = LEFT_PANEL_CHAT_LIST_STARTING_POINT_HEAD_DECIDER_PRIME_COMPONENT;
    //Only Cancel/Close
    private final String XPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP = "//*[text()='Cancel' or text()='cancel' or text()='CANCEL' or text()='Close' or text()='close' or text()='CLOSE' ]";
    private final String JOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN = "//*[text()='Join Group' or text()='JOIN GROUP' or text()='Request to Join' or text()='Request to Join Group' or text()='APPLY TO JOIN GROUP' or text()='Join Channel' or text()='Request to Join Channel']";
    private final String TEXT_REQUEST_TO_EXIT = "//div[(text()='Exit group' or text()='Archive instead')]";
    private final String BUTTON_CANCEL_CLOSE_BACK_CLICK_POPUP = XPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP.replace("]", "") + " or @aria-label='Back' or @title='Back']";

    // private final String WA_PINNED_SPAN_TITLE = "//*[text()='Backend
    // Developer']";
    private final String WA_PINNED_SPAN_TITLE = LEFT_PANEL_CHAT_LIST_STARTING_POINT_HEAD_DECIDER_PRIME_COMPONENT + "/descendant::h3[@role='button' and text()='MG Communications']" + LEFT_PANEL_CLICKABLE_CHAT_ITEM_IN_CHAT_LIST_APPENDER;//**Best format** with reusable separate[]
    //*[contains(@class, 'chat-list') and contains(@class, 'custom-scroll')]/descendant::h3[@role='button' and text()='MG Communications']/ancestor::div[@class='info']/preceding-sibling::div[contains(@class,"ripple-container")]

    //// *[text()='Backend Developer']//ancestor::a[@class='ListItem-button' or
    // @class]";
    // "//*[text()='Backend
    // Developer']//ancestor::[contains(@class,'chatlist')]::div[@class='c-ripple']";
    // "//*[text()='Backend Developer']//ancestor::ul[@class='chatlist']";
    //// h3[@dir='auto'and contains(@class,'fullName') and text()='Backend
    //// Developer']//ancestor::div[@class='ListItem Chat chat-item-clickable
    //// group']
    // "//div[@class='info-row' ]//child::div//child::h3[@dir='auto' and
    /// / contains(@class,'fullName') and text()='Backend Developer']";

    private final String WA_PINNED_SPAN_TITLE_FOR_UNARCHIVE = "//h3[@dir='auto' and contains(@class,'fullName') and text()='Archived Chats']";
    private final String SPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT = "//span[@data-testid='x-alt' or @data-icon='x-alt']";

    private final String SPAN_DATA_TESTID_SEND = "//button[@title='Send Message']";
    private final String DIV_TITLE_TYPE_A_MESSAGE = "//div[@aria-label='Message']";
    private final String FIND_MESSAGE_TEXT_NOT_ALLOWED_IN_POST_BOX = "//*[contains(text(),'Text not allowed') or contains(text(),'The admins of this group have restricted your ability to send messages.') or text()='Message for ' or text()='This channel is private']";
    //"//div[@id='message-input-text']//child::div//child::span[text()='Message']";

    private final String LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH = "//input[@id='telegram-search-input' and @placeholder='Search']";
    private final String XPATH_SEARCH = LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH;
    private final String BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH = "//*[@aria-label='Return to chat list']";

    private final String SELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP = "//span[contains(@title,'%s')]";
    private final String CLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION = "//button[@title='Return to chat list']";

    private final String XPATH_GROUP_CONTACTS_MOUSEHOVER = "//*[@id=\"main\"]/header/div[2]/div[2]/span";
    private final String CHROME_DRIVE_PATH = "D:\\SupportDetails\\Support_ExtractSupportProxyContacts\\chromedriver_win64\\chromedriver.exe";

    private final String SUPPORT_EXTRACTED_CONTACTS = "D:\\SupportDetails\\Support_ExtractSupportProxyContacts\\contacts";

    //"//button[@title='Back']/..";
    //"//div[@data-testid='content' and (text()='Cancel')]";
    private final String XPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON = FIND_GROUP_NAME_RIGHT_TOP;
    private final String JOIN_NEW_GROUP_LINK = "//a[contains(text(),'https://t.me/')]";

    // Unarchive XRef Data Set
    private final String LEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE = "//div[text()='Archived' or text()='MG Communications']";
    private final String LEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE = "//span[@data-icon='menu']";
    private final String LEFT_PANEL_XREF_TO_ARCHIVE_INSIDE_PAGE = "//div[@title='Archived']";
    private final String LEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT = "//div[@role='listitem']//child::span[@title and not(contains(@title,'#')) and @dir='auto']";
    private final String ARRORW_FROM_ARCHIVED_TO_CHATS = "//span[@data-icon='back']";
    private final String MESSAGE_PENDING_STATUS = "//span[@aria-label=' Pending ']";
    private final String MESSAGE_WITH_ERROR_STATUS = "//span[@data-icon='error']";

    private final String TELEGRAM_SCANNER_IDENTIFIER = "//title[text()='Telegram']";

    // ROBOT Properties
    private final int MOVE_TO_CHAT_VK_NEXT = KeyEvent.VK_DOWN;
    private final int MOVE_TO_CHAT_VK_PREVIOUS = KeyEvent.VK_UP;
    private final int MOVE_TO_CHAT_VK_SHIFT = 0;
    private final int MOVE_TO_CHAT_VK_CONTROL = 0;
    private final int MOVE_TO_CHAT_VK_ALT = KeyEvent.VK_ALT;

    private final CharSequence MOVE_TO_NEXT_CHAT_KEYS = Keys.ARROW_DOWN;
    private final CharSequence MOVE_TO_PREVIOUS_CHAT_KEYS = Keys.ARROW_UP;
    private final CharSequence MOVE_TO_CHAT_KEYS_SHIFT = "0";
    private final CharSequence MOVE_TO_CHAT_KEYS_CONTROL = "0";
    private final CharSequence MOVE_TO_CHAT_KEYS_ALT = Keys.ALT;


    // Search and extract specific XPaths for contact selection and message extraction
    private final String XPATH_SEARCH_RESULT_BY_CONTACT_NAME = "//*[contains(@class, 'chat-list')]//h3[contains(text(), '%s')]/ancestor::div[@class='info']/preceding-sibling::div[contains(@class,'ripple-container')]";
    private final String XPATH_SEARCH_EXTRACT_MESSAGE_ELEMENTS = "//div[@class='ListItem-button']/descendant::div[@class='message']";
    private final String XPATH_SEARCH_EXTRACT_LOOKING_FOR = "//div[@class='ListItem-button']//div[@class='message' and contains(text(),'I am looking for')]";

    private final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT =
            //""+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)+
            "I am a **Fullstack, DevOps and Backend Proxy and Support** direct individual Person with 16+ Years of **Expertise IT Industry Experience**. WhatsApp: **+918274848227**"
                    + " _NL_ "
                    + " _NL_ "
                    //+ "with 9+ Years Experience in Interview Support and Job Support in ''Fullstack or Backend requirements'' on below areas: "
                    //+ "** Call/Message ** me directly (''Direct Person'') for any ''Fullstack or Backend requirements'' on - "
                    + "**GUARANTEED, SAFE AND SECURE** : I take calls from **USA, Canada, UK and India since last 9+** years in Fullstack, DevOps, Data Engineer, Big Data and Backend profiles especially for **FAANG and MNCs** in below areas: "
                    + " _NL_ "
                    + " _NL_ "
                    + "**INTERVIEW SUPPORT (PROXY Call)**/ Assignment/ Job Support/ Coding Test/ Training/ Task"
                    + " _NL_ "
                    + " _NL_ "
                    + "#WhatsApp: **+918274848227**"
                    + " _NL_ "
                    + " _NL_ "
                    + "**100% GENUINE : Feel free to Ask me any Technical Question, I am always ready to face and answer**"
                    + " _NL_ "
                    + " _NL_ "
                    + "**Technology Stacks** :"
                    + " _NL_ "
                    + " _NL_ "
                    + "  1. **JAVA 8,11,17,21,24, Python, C# and Complex Codings and LeetCode problem solving**"
                    + " _NL_ "
                    + "  2. Spring, Spring Boot, Spring MVC, Spring AI"
                    + " _NL_ "
                    + "  3. Microservices, Soap, Rest Api"
                    + " _NL_ "
                    + "  4. **Angular, React, Javascript**"
                    + " _NL_ "
                    + "  5. Apache/Confluent Kafka, Apache Spark, Hadoop, Desk, Data Lake, Snowflake, ETL"
                    + " _NL_ "
                    + "  6. AWS/Azure/GCP, DevOps, CICD, Selenium, Cucumber, Automation, PowerBI, Salesforce & Appian"
                    + " _NL_ "
                    + "  7. Docker, Kubernetes, Redis, JPA, SQL and NoSQL Databases"
                    + " _NL_ "
                    + "  8. AI/ML, MLOps, Bedrock, GenAI, FM, LLM, PyTorch, Vector Database"
                    + " _NL_ "
                    + "  9. **Data Structures and Algorithms**"
                    + " _NL_ "
                    + " _NL_ "
                    + "**Process** : - Prompting & Transcript with **Otter Pro Premium** with **100% IN-VISIBLE and UN-TRACEABLE Pro Premium Screen Control apps** for live coding. "
                    + " _NL_ "
                    + " _NL_ "
                    +"WhatsApp: **+918274848227**";

    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+ "**Domain Experiences**: Banking, Capital Market/Securities Trading, Telecom, Healthcare, Aviation"
			/*+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "**Expert* in *Java 8, Java 11, Java 17, Spring boot, Microservices, Kafka*, *Data Structures and Algorithms*, Hibernate,"
			+ " *JPA, Docker, Kubernetes*, *AWS*, UI: *Angular*, React,**"
			+ " Node JS, *Redis Cache, SQL DBs (Oracle, SqlServer, MySQL, Postgress, Cockroach DB)*, *NoSQL DBs(Couchbase, MongoDB, Cassandra)*, Spring MVC, Spring AOP,"
			+ " *Spring Security*, Auth Services (OAuth2 with OpenId Connect, OAuth with Google Api, LDAP, SSO, *JWT*, OKTA), *SQL, PL-SQL*, ELK,"
			+ " CICD: *Gitlab, Bamboo, Jenkins*, TFS, DevOps, C#, CDC with Debezium and Kafka Connect, Splunk with HEC, Salesforce Data integration,"
			+ " Git, GitHub, BitBucket, *Junit, Mockito, MockMvc, PowerMock, EasyMock*, XLDEPLOY, Appian Low Code, FIX protocol"
			+ " (Amazon web Service EC2, S3, Route 53, Kinesis, ECS, EKS, *Lambda*, RDS, SQS, SNS, SES)"
			+ " and in Various Technologies"*/


    //+ "** Support Process: Two Supports are allocated for a single consultant for Delivery Assurance";
    //" NOTE: Be Aware of Fake People with Fake Experiences. Verify them by asking their Linkedin profile and crosscheck industry experience"


    // Code related
    private final Predicate<String> groupInfo = groupTG -> !groupTG.contains("last seen")
            && (groupTG.contains("members") || groupTG.contains("subscribers") || groupTG.contains(","));

    public Telegram_Xpaths() {
        super();
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.TELEGRAM;
    }

    @Override
    public String getBASE_URL() {
        return BASE_URL;
    }

    @Override
    public String getFIND_GROUP_NAME_RIGHT_TOP() {
        return FIND_GROUP_NAME_RIGHT_TOP;
    }

    @Override
    public String getFIND_ADMIN_GROUP_NAME_RIGHT_TOP() {
        return FIND_ADMIN_GROUP_NAME_RIGHT_TOP;
    }


    @Override
    public String getFIND_INDV_USER_NAME_RIGHT_TOP() {
        return FIND_INDV_USER_NAME_RIGHT_TOP;
    }


    @Override
    public String getXPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP() {
        return XPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP;
    }

    @Override
    public String getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN() {
        return JOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN;
    }

    @Override
    public String getTEXT_REQUEST_TO_EXIT() {
        return TEXT_REQUEST_TO_EXIT;
    }

    @Override
    public String getWA_PINNED_SPAN_TITLE() {
        return WA_PINNED_SPAN_TITLE;
    }

    @Override
    public String getWA_PINNED_SPAN_TITLE_FOR_UNARCHIVE() {
        return WA_PINNED_SPAN_TITLE_FOR_UNARCHIVE;
    }

    @Override
    public String getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT() {
        return SPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT;
    }

    @Override
    public String getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT() {
        return SUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT;
    }

    @Override
    public String getSPAN_DATA_TESTID_SEND() {
        return SPAN_DATA_TESTID_SEND;
    }

    @Override
    public String getMessageDeliveryPendingStatus() {
        return MESSAGE_PENDING_STATUS;
    }

    @Override
    public String getMessageDeliveryErrorStatus() {
        return MESSAGE_WITH_ERROR_STATUS;
    }

    @Override
    public String getDIV_TITLE_TYPE_A_MESSAGE() {
        return DIV_TITLE_TYPE_A_MESSAGE;
    }

    @Override
    public String getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH() {
        return LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH;
    }

    @Override
    public String getXPATH_SEARCH() {
        return XPATH_SEARCH;
    }

    @Override
    public String getXPATH_GROUP_CONTACTS_MOUSEHOVER() {
        return XPATH_GROUP_CONTACTS_MOUSEHOVER;
    }

    @Override
    public String getCHROME_DRIVE_PATH() {
        return CHROME_DRIVE_PATH;
    }

    @Override
    public String getSUPPORT_EXTRACTED_CONTACTS() {
        return SUPPORT_EXTRACTED_CONTACTS;
    }

    @Override
    public String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE() {
        return LEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE;
    }

    @Override
    public String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE() {
        return LEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE;
    }

    @Override
    public String getLEFT_PANEL_XREF_TO_ARCHIVE_INSIDE_PAGE() {
        return LEFT_PANEL_XREF_TO_ARCHIVE_INSIDE_PAGE;
    }

    @Override
    public String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT() {
        return LEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT;
    }

    @Override
    public String getARRORW_FROM_ARCHIVED_TO_CHATS() {
        return ARRORW_FROM_ARCHIVED_TO_CHATS;
    }

    @Override
    public int getMoveToChatVkNext() {
        return MOVE_TO_CHAT_VK_NEXT;
    }

    @Override
    public int getMoveToChatVkPrevious() {
        return MOVE_TO_CHAT_VK_PREVIOUS;
    }

    @Override
    public int getMoveToChatVkShift() {
        return MOVE_TO_CHAT_VK_SHIFT;
    }

    @Override
    public int getMoveToChatVkControl() {
        return MOVE_TO_CHAT_VK_CONTROL;
    }

    @Override
    public int getMoveToChatVkAlt() {
        return MOVE_TO_CHAT_VK_ALT;
    }

    @Override
    public CharSequence getMoveToNextChatKeys() {
        return MOVE_TO_NEXT_CHAT_KEYS;
    }

    @Override
    public CharSequence getMoveToPreviousChatKeys() {
        return MOVE_TO_PREVIOUS_CHAT_KEYS;
    }

    @Override
    public CharSequence getMoveToChatKeysShift() {
        return MOVE_TO_CHAT_KEYS_SHIFT;
    }

    @Override
    public CharSequence getMoveToChatKeysControl() {
        return MOVE_TO_CHAT_KEYS_CONTROL;
    }

    @Override
    public CharSequence getMoveToChatKeysAlt() {
        return MOVE_TO_CHAT_KEYS_ALT;
    }


    @Override
    public String getBUTTON_CANCEL_CLICK_BACK() {
        return BUTTON_CANCEL_CLOSE_BACK_CLICK_POPUP;
    }

    @Override
    public String getXPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON() {
        return XPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON;
    }

    @Override
    public String getJOIN_NEW_GROUP_LINK() {
        return JOIN_NEW_GROUP_LINK;
    }

    @Override
    public Predicate<String> getGroupInfo() {
        return groupInfo;
    }

    @Override
    public String getSELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP() {
        return SELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP;
    }

    @Override
    public String getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION() {
        return CLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION;
    }

    @Override
    public String getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT() {
        return MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT;
    }

    @Override
    public String getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS() {
        return MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS;
    }

    @Override
    public String getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH() {
        return BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH;
    }

    @Override
    public String getLEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS() {
        return LEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS;
    }

    @Override
    public String getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN() {
        return LEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN;
    }

    @Override
    public String getSOCIAL_MEDIA_SCANNER_IDENTIFIER_AFTER_URL() {
        return TELEGRAM_SCANNER_IDENTIFIER;
    }

    @Override
    public String getFIND_MESSAGE_TEXT_NOT_ALLOWED_IN_POST_BOX() {
        return FIND_MESSAGE_TEXT_NOT_ALLOWED_IN_POST_BOX;
    }

    @Override
    public String getXPATH_SEARCH_RESULT_BY_CONTACT_NAME() {
        return XPATH_SEARCH_RESULT_BY_CONTACT_NAME;
    }

    @Override
    public String getXPATH_SEARCH_EXTRACT_MESSAGE_ELEMENTS_LOOKING_FOR() {
        return XPATH_SEARCH_EXTRACT_MESSAGE_ELEMENTS;
    }

}