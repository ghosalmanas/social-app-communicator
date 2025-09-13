package com.wtf.app.model.dto;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterface;

public interface SocialModel {


    XPathInterface getxPathInterface() ;

    SocialType getSocialType();

    String getBaseURL();

    String getFileNameToRead();

}
