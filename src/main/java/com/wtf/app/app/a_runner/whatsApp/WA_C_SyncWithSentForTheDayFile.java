package com.wtf.app.app.a_runner.whatsApp;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.impls.WA_CC_SyncGroupNamesWithSentForTheDayFile;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class WA_C_SyncWithSentForTheDayFile extends WhatsappRunnerParent{

	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		XPathInterfaceWATG xPathInterface= new Telegram_Xpaths();
		WhatsappParent instance = new WA_CC_SyncGroupNamesWithSentForTheDayFile(xPathInterface.getBASE_URL(),SocialType.WHATSAPP);
		instance.setTraverseDependsOnAllSuccessGroupList(false);
		execute(instance);
	}
}
