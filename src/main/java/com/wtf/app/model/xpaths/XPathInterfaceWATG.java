package com.wtf.app.model.xpaths;

import java.util.function.Predicate;

public interface XPathInterfaceWATG extends XPathInterface {

	String getFIND_GROUP_NAME_RIGHT_TOP() ;

	String getXPATH_CANCEL_OR_CLOSE_INSTEAD_OF_JOIN_GROUP() ;

	String getJOIN_GROUP_OR_TEXT_REQUEST_TO_JOIN() ;

	String getTEXT_REQUEST_TO_EXIT() ;

	String getWA_PINNED_SPAN_TITLE() ;

	String getWA_PINNED_SPAN_TITLE_FOR_UNARCHIVE() ;

	String getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT() ;

	String getSUB_HEADER_OPENED_CHAT_WINDOW_EG_GROUP_INFO_PH_BELOW_GROUP_NAME_TOP_RIGHT() ;

	String getSPAN_DATA_TESTID_SEND() ;
	String getMessageDeliveryPendingStatus() ;

    String getMessageDeliveryErrorStatus();

    String getDIV_TITLE_TYPE_A_MESSAGE() ;

	String getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH() ;

	String getXPATH_SEARCH() ;

	String getXPATH_GROUP_CONTACTS_MOUSEHOVER() ;

	String getCHROME_DRIVE_PATH() ;

	String getSUPPORT_EXTRACTED_CONTACTS() ;

	String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_ENTRY_PAGE() ;

	String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_MENU_ENTRY_PAGE() ;

	String getLEFT_PANEL_XREF_TO_ARCHIVE_INSIDE_PAGE() ;

	String getLEFT_PANEL_XREF_TO_UN_ARCHIVE_EACH_ELEMENT() ;

	String getARRORW_FROM_ARCHIVED_TO_CHATS() ;

	int getMoveToChatVkNext() ;

	int getMoveToChatVkPrevious();

	int getMoveToChatVkShift();

	int getMoveToChatVkControl();

	int getMoveToChatVkAlt();



	CharSequence getMoveToNextChatKeys() ;

	CharSequence getMoveToPreviousChatKeys();

	CharSequence getMoveToChatKeysShift();

	CharSequence getMoveToChatKeysControl();

	CharSequence getMoveToChatKeysAlt();


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

	String getFIND_MESSAGE_TEXT_NOT_ALLOWED_IN_POST_BOX();
}
