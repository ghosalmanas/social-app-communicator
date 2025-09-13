package com.wtf.app.model.config;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.WhatsApp_Xpaths;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WhatsAppConfig /*extends WATGConfig */{


    @Bean
    public XPathInterfaceWATG xPathInterfaceWA() {
        return new WhatsApp_Xpaths();
    }


    @Bean
    public SocialType socialTypeWA() {
        return SocialType.WHATSAPP;
    }
    @Bean(name = "baseURLWA")
    public String baseURLWA() {
        return xPathInterfaceWA().getBASE_URL();
    }
/*

    @Override
    @Bean
    public XPathInterfaceWATG xPathInterfaceWATG() {
        return (XPathInterfaceWATG) xPathInterface();
    }
*/


}