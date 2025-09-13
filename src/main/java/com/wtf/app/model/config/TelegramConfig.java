package com.wtf.app.model.config;

import com.wtf.app.model.dto.TelegramDTO;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.Telegram_Xpaths;
import com.wtf.app.model.xpaths.XPathInterface;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfig extends WATGConfig {
    
    //@Override
    @Bean
    public XPathInterfaceWATG xPathInterfaceTG() {
        return new Telegram_Xpaths();
    }

    @Bean
    public XPathInterface xPathInterfaceTGG() {
        return new Telegram_Xpaths();
    }

    //@Override
    @Bean
    public SocialType socialTypeTG() {
        return SocialType.TELEGRAM;
    }

    @Bean
    TelegramDTO telegramDTO() {
        return new TelegramDTO(xPathInterfaceTG(), xPathInterfaceTGG(), socialTypeTG(), xPathInterfaceTG().getBASE_URL());
    }


   /* @Override
    @Bean
    public XPathInterfaceWATG xPathInterfaceWATG() {
        return (XPathInterfaceWATG) xPathInterface();
    }
*/

}