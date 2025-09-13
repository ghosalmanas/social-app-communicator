package com.wtf.app.model.dto;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterface;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;

//@Component("telegramDTO")
public class TelegramDTO implements WATG_DTO{
    
    private final XPathInterface xPathInterface;
    
    private final XPathInterfaceWATG xPathInterfaceWATG;
    
    private final SocialType socialType;
    
    private final String baseURL;

    public TelegramDTO(XPathInterfaceWATG xPathInterfaceWATG, XPathInterface xPathInterface, SocialType socialType, String baseUrl) {
        this.xPathInterface = xPathInterface;
        this.xPathInterfaceWATG = xPathInterfaceWATG;
        this.socialType = socialType;
        this.baseURL = baseUrl;
    }

//creating bean with constructor injection instead of field injection in TelegramConfig

    @Override
    public XPathInterface getxPathInterface() {
        return xPathInterface;
    }

    //@Override
    public XPathInterfaceWATG getxPathInterfaceWATG() {
        return xPathInterfaceWATG;
    }

    @Override
    public SocialType getSocialType() {
        return socialType;
    }

    @Override
    public String getBaseURL() {
        return baseURL;
    }

    @Override
    public String getFileNameToRead() {
        // This DTO does not have a specific file to read, returning null.
        return null;
    }
}
