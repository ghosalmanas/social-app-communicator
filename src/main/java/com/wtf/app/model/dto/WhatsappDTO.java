package com.wtf.app.model.dto;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterface;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("whatsappDTO")
public class WhatsappDTO implements WATG_DTO {

    @Autowired
    private XPathInterfaceWATG xPathInterfaceWA;
    @Autowired
    private SocialType socialTypeWA;
    @Autowired
    @Qualifier("baseURLWA")
    private String baseURLWA;

    @Override
    public XPathInterface getxPathInterface() {
        return xPathInterfaceWA;
    }

    @Override
    public XPathInterfaceWATG getxPathInterfaceWATG() {
        return xPathInterfaceWA;
    }

    @Override
    public SocialType getSocialType() {
        return socialTypeWA;
    }

    @Override
    public String getBaseURL() {
        return baseURLWA;
    }

    @Override
    public String getFileNameToRead() {
        // This DTO does not have a specific file to read, returning null.
        return null;
    }

    public String getConsultantsPhoneNumbersFile() {
        return com.wtf.app.parent.Constants.CONSULTANTS_PHONE_NUMBERS_FILE;
    }
}
