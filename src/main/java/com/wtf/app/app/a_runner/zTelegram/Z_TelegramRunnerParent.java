package com.wtf.app.app.a_runner.zTelegram;

import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class Z_TelegramRunnerParent {

	public static void execute(WhatsappCommonUtils instance) throws InterruptedException, AWTException, IOException {
		XPathInterfaceWATG xPathInterface = new Telegram_Xpaths();
		instance.setxPathInterface(xPathInterface);
		instance.traverseGroupsTemplate(instance);
		//instance.setSocialType(SocialType.TELEGRAM);
	}
}
