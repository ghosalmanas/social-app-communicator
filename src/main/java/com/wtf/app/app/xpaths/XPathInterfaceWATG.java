package com.wtf.app.app.xpaths;

import com.wtf.app.app.enums.SocialType;

import java.util.function.Predicate;

public interface XPathInterfaceWATG extends XPathInterface {

	public SocialType getSocialType();//Make it enum

	public String getFIND_GROUP_NAME_RIGHT_TOP() ;

	public String getXPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP() ;

	public String getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN() ;

	public String getTEXT_REQUEST_TO_EXIT() ;

	public String getWA_PINNED_SPAN_TITLE() ;

	public String getWA_PINNED_SPAN_TITLE_FOR_UNARCHIVE() ;

	public String getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT() ;

	public String getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT() ;

	public String getSPAN_DATA_TESTID_SEND() ;

	public String getDIV_TITLE_TYPE_A_MESSAGE() ;

	public String getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH() ;

	public String getXPATH_SEARCH() ;

	public String getXPATH_GROUP_CONTACTS_MOUSEHOVER() ;

	public String getCHROME_DRIVE_PATH() ;

	public String getSUPPORT_EXTRACTED_CONTACTS() ;

	public String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE() ;

	public String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE() ;

	public String getLEFT_PANEL_XREF_TO_ARCHIVE_INSIDE_PAGE() ;

	public String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT() ;

	public String getARRORW_FROM_ARCHIVED_TO_CHATS() ;

	public int getMoveToChatVkNext() ;

	public int getMoveToChatVkPrevious();

	public int getMoveToChatVkShift();

	public int getMoveToChatVkControl();

	public int getMoveToChatVkAlt();

	String getBUTTON_CANCEL_CLICK_BACK();

	String getXPATH_AVOID_TO_JOIN_NEW_GROUP_MSG_WITH_CREATED_ON();

	String getJOIN_NEW_GROUP_LINK();

	Predicate<String> getGroupInfo();

	String getSELECT_FIRST_RESULT_FROM_SEARCH_OF_PREVIOUS_GROUP();

	String getCLEAR_SEARCH_OF_PREVIOUS_GROUP_AFTER_SELECTION();

	String getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT();

	String getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS();

	String getBACK_ARROW__BESIDE_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH();

	String getFIND_INDV_USER_NAME_RIGHT_TOP();

	String getFIND_ADMIN_GROUP_NAME_RIGHT_TOP();

	String getLEFT_PANEL_CHAT_LIST_WITH_MULTIPLE_GROUPS();

	String getLEFT_PANEL_CHAT_LIST_WITH_CUSTOM_SCROLL_TO_SCROLL_DOWN();
}
