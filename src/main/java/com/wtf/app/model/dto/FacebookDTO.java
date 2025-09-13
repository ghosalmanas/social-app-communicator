package com.wtf.app.model.dto;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterface;
import com.wtf.app.model.xpaths.XPathInterfaceFB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("facebookDTO")
public class FacebookDTO implements SocialModel {

    @Autowired
    private XPathInterfaceFB xPathInterfaceFB;
    @Autowired
    private SocialType socialTypeFB;
    @Autowired
    private String baseURLFB;

    @Override
    public XPathInterface getxPathInterface() {
        return xPathInterfaceFB;
    }

    public XPathInterfaceFB getxPathInterfaceFB() {
        return xPathInterfaceFB;
    }

    @Override
    public SocialType getSocialType() {
        return socialTypeFB;
    }

    @Override
    public String getBaseURL() {
        return baseURLFB;
    }

    @Override
    public String getFileNameToRead() {
        // This DTO does not have a specific file to read, returning null.
        return null;
    }
}
