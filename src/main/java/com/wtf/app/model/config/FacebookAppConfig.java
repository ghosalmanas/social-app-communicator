package com.wtf.app.model.config;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacebookAppConfig extends SocialMediaConfig {

    //@Override
    @Bean
    public XPathInterfaceFB xPathInterfaceFB() {
        return new Facebook_Xpaths();
    }

    //@Override
    @Bean
    public SocialType socialTypeFB() {
        return SocialType.FACEBOOK;
    }

    @Bean(name = "baseURLFB")
    public String baseURLFB() {
        return xPathInterfaceFB().getBASE_URL();
    }

}