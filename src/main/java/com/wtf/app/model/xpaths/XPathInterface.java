package com.wtf.app.model.xpaths;

import com.wtf.app.model.enums.SocialType;

public interface XPathInterface {
	String getBASE_URL();
	SocialType getSocialType();
	String getSOCIAL_MEDIA_SCANNER_IDENTIFIER_AFTER_URL();
    String getXPATH_SEARCH_EXTRACT_LOOKING_FOR();
}
