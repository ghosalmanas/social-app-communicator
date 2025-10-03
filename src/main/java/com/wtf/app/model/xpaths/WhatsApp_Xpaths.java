package com.wtf.app.model.xpaths;

import com.wtf.app.model.enums.SocialType;
import org.openqa.selenium.Keys;

import java.awt.event.KeyEvent;
import java.util.function.Predicate;


public class WhatsApp_Xpaths implements XPathInterfaceWATG {

    private static final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS = null;


    private final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT =

            //""+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)+
            /*" _NL_ "*/

            "I am a *Fullstack Proxy and Support individual direct Person with 16+ Years of Expertise IT Industry Experience*. WhatsApp: *+918274848227*"
                    + " _NL_ "
                    + " _NL_ "
                    //+ "and 7+ Years Experience in Interview Support and Job Support."
                    + "*100% GUARANTEED, SAFE AND SECURE*, I take calls from _*USA, Canada, UK and India since last 9+ years*_ in Fullstack and Backend profiles in below areas:"
                    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
                    + " _NL_ "
                    //+ "*>* Call/Message me directly (*Direct Contact*) for any *Fullstack or Backend requirements* on - "
                    + "*INTERVIEW SUPPORT (PROXY Call)*/ Assignment/ *Job Support/ Coding Test*/ Training/ Task"
                    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
                    + " _NL_ "
                    + " _NL_ "
                    //+ "*>* WhatsApp:- https://wa.me/+918274848227"
                    + "#WhatsApp: _*+918274848227*_"
                    + " _NL_ "
                    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
                    + " _NL_ "
                    + "** 100% GENUINE : Feel free to Ask me any Technical Question, I am always ready to face and answer**"
                    + " _NL_ "
                    + " _NL_ "
                    // + "*>* I am a *Java Fullstack Technical Lead Developer* expertise in"
                    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)+
                    + "*Technology Stacks* :"
                    + " _NL_ "
                    + "  1. *JAVA 8,11,17,21 and Complex Codings*"
                    + " _NL_ "
                    + "  2. Spring, Spring Boot, Spring MVC, Spring AI"
                    + " _NL_ "
                    + "  3. Microservices, Soap, Rest Api"
                    + " _NL_ "
                    + "  4. *Angular, React, Javascript*"
                    + " _NL_ "
                    + "  5. Kafka, Spark"
                    + " _NL_ "
                    + "  6. *AWS*/Azure/GCP"
                    + " _NL_ "
                    + "  7. Docker, Kubernetes, Redis, JPA, SQL and NoSQL Databases"
                    + " _NL_ "
                    + "  8. *Data Structures and Algorithms*"
                    //+ "for - *INTERVIEW SUPPORT (PROXY Call)*/ Assignment/ *Job Support/ Coding Test*/ Training/ Tasks"
                    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
                    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
                    + " _NL_ "
                    //+ "*>* I am a *Professional Direct Proxy* and I do *Prompting and with Premium Otter or Google Transcript * wth Screen Control for coding."
                    //+ "*>* Besides, I'm a *Professional Java Fullstack Proxy*"
                    + "*Process*: - Prompting & Transcript with *Otter Premium* with *100% _IN-VISIBLE and UN-TRACEABLE_ Screen Control app* for live coding."
                    + " _NL_ "
                    +" WhatsApp: *+918274848227*";
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+ "*> 14* years of *Expertise Industry Experience* as *Java Fullstack Technical Architect*"
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			/*+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*> Expert* in *Java 8, Java 11, Java 17, Spring boot, Microservices, Kafka*, *Data Structures and Algorithms*, Hibernate,"
			+ " *JPA, Docker, Kubernetes*, *AWS*, UI: *Angular*, React,"
			+ " Node JS, *Redis Cache, SQL DBs (Oracle, SqlServer, MySQL, Postgress, Cockroach DB)*, *NoSQL DBs(Couchbase, MongoDB, Cassandra)*, Spring MVC, Spring AOP,"
			+ " *Spring Security*, Auth Services (OAuth2 with OpenId Connect, OAuth with Google Api, LDAP, SSO, *JWT*, OKTA), *SQL, PL-SQL*, ELK,"
			+ " CICD: *Gitlab, Bamboo, Jenkins*, TFS, DevOps, C#, CDC with Debezium and Kafka Connect, Splunk with HEC, Salesforce Data integration,"
			+ " Git, GitHub, BitBucket, *Junit, Mockito, MockMvc, PowerMock, EasyMock*, XLDEPLOY, Appian Low Code, FIX protocol"
			+ " (Amazon web Service EC2, S3, Route 53, Kinesis, ECS, EKS, *Lambda*, RDS, SQS, SNS, SES)"
			+ " and in Various Technologies"*/
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+ "*>* *Domain Experiences*: _Banking, Capital Market/Securities Trading, Telecom, Healthcare, Aviation_"
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT);
    //+" _NL_ ";
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+ "*>* *Screen Sharing Apps*: VNC, *Chrome Remote Desktop*, *Anydesk*, TeamViewer, Ultraviewer, Zoom, Teams, WebEx, GetScreen and *GotoMypc*"
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
    //+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT);
    //+ "*>* Support Process: Two Supports are allocated for a single consultant for Delivery Assurance";
    //">> NOTE: Be Aware of Fake People with Fake Experiences. Verify them by asking their Linkedin profile and crosscheck industry experience"



    /* Always try to use //* and text() but not tags like div,title and class as they might change but text will rarely change and if changed its visible*/

    private final String BASE_URL = "https://web.whatsapp.com/";
    private final String FIND_GROUP_NAME_RIGHT_TOP = "//div[@title='Profile details']/following-sibling::div[@role='button']/descendant::span[@dir='auto']";
    //"//header[@class='_amid']//child::div//child::span[@dir='auto']";
    private final String FIND_USER_OR_GROUP_NAME_RIGHT_TOP_APPENDER = "/following::span[(text() and (contains(text(),'group info') or contains(text(),',')))]";
    private final String SUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT = FIND_GROUP_NAME_RIGHT_TOP + FIND_USER_OR_GROUP_NAME_RIGHT_TOP_APPENDER;

    /* Allow Admins with groups during contact extractions */
    private final String FIND_ADMIN_GROUP_NAME_RIGHT_TOP = "//*[@role='button' and text()='admins']/ancestor::div/descendant::" + FIND_GROUP_NAME_RIGHT_TOP.substring(2);
    //"//*[@role='button' and text()='admins']";
    private final String FIND_INDV_USER_NAME_RIGHT_TOP = FIND_GROUP_NAME_RIGHT_TOP;
    private final String LEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS = "//div[@aria-label='Chat list']/div";
    private final String LEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN = "//*[contains(@id,'pane-side')]";
    //"//*[contains(@class, 'chat list') and contains(@class, 'custom-scroll')]";//verify

    private final String XPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP = "//div[(text()='Cancel' or text()='Close')]";
    private final String JOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN = "//div[(text()='Join group' or text()='Request to join')]";
    private final String TEXT_REQUEST_TO_EXIT = "//div[@data-testid='content' and (text()='Exit group' or text()='Archive instead')]";
    private final String WA_PINNED_SPAN_TITLE = "//span[@title='MG Communications']";
    //"//span[@title='Oindrila' or @title='+91 82748 42669' or @title='Akash Ghosal' or @title='+91 9836509607' or @title='MG Communications']";
    private final String WA_PINNED_SPAN_TITLE_FOR_UNARCHIVE = "//span[@title='Archived']";
    private final String SPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT = "//span[@data-testid='x-alt' or @data-icon='x-alt']";

    private final String SPAN_DATA_TESTID_SEND = "//div[@aria-label='Send']";
    private final String DIV_TITLE_TYPE_A_MESSAGE = "//div[@aria-placeholder='Type a message']";
    private final String LABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH = "//*[@aria-label='Search input textbox']/parent::div";
    //"//button[@aria-label='Search or start new chat']";
    private final String BACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH = "//div[@aria-label='Chat list']";

    private final String XPATH_SEARCH = "//*[@aria-label='Search input textbox']";
    //"//*[@id=\"side\"]/div[1]/div/div/div[2]/div/div[1]/p";
    private final String SELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP = "//span[contains(@title,'%s')]";
    private final String CLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION = "//span[@data-icon='close-refreshed']";

    private final String XPATH_GROUP_CONTACTS_MOUSEHOVER = "//*[@id=\"main\"]/header/div[2]/div[2]/span";
    private final String CHROME_DRIVE_PATH = "D:\\SupportDetails\\Support_ExtractSupportProxyContacts\\chromedriver_win64\\chromedriver.exe";

    private final String SUPPORT_EXTRACTED_CONTACTS = "D:\\SupportDetails\\Support_ExtractSupportProxyContacts\\contacts";

    private final String DIV_TEXT_CANCEL_CLICK = "//div[@data-testid='content' and (text()='Cancel')]";
    private final String XPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON = "//div[contains(text(),'Created on')]//preceding-sibling::span//child::img";
    private final String JOIN_NEW_GROUP_LINK = "//a[contains(@href,'https://chat.whatsapp.com/')]";


    //Unarchive XRef Data Set
    private final String LEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE = "//div[text()='Archived']";
    private final String LEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE = "//span[@data-icon='menu']";
    private final String LEFT_PANEL_XREF_TO_ARCHIVE_INSIDE_PAGE = "//div[@title='Archived']";
    private final String LEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT = "//div[@role='listitem']//child::span[@title and not(contains(@title,'#')) and @dir='auto']";
    private final String ARRORW_FROM_ARCHIVED_TO_CHATS = "//span[@data-icon='back']";
    private final String MESSAGE_WITH_PENDING_STATUS = "//span[@aria-label=' Pending ']";
    private final String MESSAGE_WITH_ERROR_STATUS = "//span[@data-icon='error']";

    private final String WHATSAPP_SCANNER_IDENTIFIER = "//title[text()='WhatsApp']";

    //ROBOT Properties
    private final int MOVE_TO_CHAT_VK_NEXT = KeyEvent.VK_CLOSE_BRACKET;
    private final int MOVE_TO_CHAT_VK_PREVIOUS = KeyEvent.VK_OPEN_BRACKET;
    private final int MOVE_TO_CHAT_VK_SHIFT = KeyEvent.VK_SHIFT;
    private final int MOVE_TO_CHAT_VK_CONTROL = KeyEvent.VK_CONTROL;
    private final int MOVE_TO_CHAT_VK_ALT = KeyEvent.VK_ALT;

    private final CharSequence MOVE_TO_NEXT_CHAT_KEYS = "]";
    private final CharSequence MOVE_TO_PREVIOUS_CHAT_KEYS = "[";
    private final CharSequence MOVE_TO_CHAT_KEYS_SHIFT = Keys.SHIFT;
    private final CharSequence MOVE_TO_CHAT_KEYS_CONTROL = Keys.CONTROL;
    private final CharSequence MOVE_TO_CHAT_KEYS_ALT = Keys.ALT;


    private final Predicate<String> groupInfo = groupWA -> groupWA.contains("group info") || groupWA.contains(",");

    public WhatsApp_Xpaths() {
        super();
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.WHATSAPP;
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
        return MESSAGE_WITH_PENDING_STATUS;
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
        return DIV_TEXT_CANCEL_CLICK;
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
        return WHATSAPP_SCANNER_IDENTIFIER;
    }
}