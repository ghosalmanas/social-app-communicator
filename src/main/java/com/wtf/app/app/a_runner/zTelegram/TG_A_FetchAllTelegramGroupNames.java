package com.wtf.app.app.a_runner.zTelegram;

import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.impls.WA_B_FetchAllWAContactsWGroupNames;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class TG_A_FetchAllTelegramGroupNames extends Z_TelegramRunnerParent{

	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		XPathInterfaceWATG xPathInterface= new Telegram_Xpaths();
		WhatsappCommonUtils instance = new WA_B_FetchAllWAContactsWGroupNames(xPathInterface.getBASE_URL(), SocialType.TELEGRAM);
		execute(instance);
	}
}
