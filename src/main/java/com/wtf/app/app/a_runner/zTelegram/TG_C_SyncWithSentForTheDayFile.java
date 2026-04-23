package com.wtf.app.app.a_runner.zTelegram;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.impls.WA_CC_SyncGroupNamesWithSentForTheDayFile;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class TG_C_SyncWithSentForTheDayFile extends Z_TelegramRunnerParent{

	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		XPathInterfaceWATG xPathInterface= new Telegram_Xpaths();
		WhatsappParent instance = new WA_CC_SyncGroupNamesWithSentForTheDayFile(xPathInterface.getBASE_URL(),SocialType.TELEGRAM);
		instance.setTraverseDependsOnAllSuccessGroupList(false);
		execute(instance);
	}
}
