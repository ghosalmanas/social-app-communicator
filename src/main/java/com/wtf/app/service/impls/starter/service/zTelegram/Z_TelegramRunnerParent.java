package com.wtf.app.service.impls.starter.service.zTelegram;

import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.model.xpaths.Telegram_Xpaths;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component( "zTelegramRunnerParent")
@AllArgsConstructor
//@NoArgsConstructor
public class Z_TelegramRunnerParent {


	public static void execute(WATGCommonUtils instance) throws InterruptedException, AWTException, IOException {
		XPathInterfaceWATG xPathInterface = new Telegram_Xpaths();
		instance.setxPathInterface(xPathInterface);
		instance.traverseGroupsTemplate(instance);
		//instance.setSocialType(SocialType.TELEGRAM);
	}
}
