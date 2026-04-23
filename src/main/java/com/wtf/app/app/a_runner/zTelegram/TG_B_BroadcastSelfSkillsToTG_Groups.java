package com.wtf.app.app.a_runner.zTelegram;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.impls.WA_C_TG_BroadcastMessageToAllGroups;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class TG_B_BroadcastSelfSkillsToTG_Groups extends Z_TelegramRunnerParent{

	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		XPathInterfaceWATG xPathInterface= new Telegram_Xpaths();
		WhatsappParent instance = new WA_C_TG_BroadcastMessageToAllGroups(xPathInterface.getBASE_URL(),SocialType.TELEGRAM);
		instance.setTraverseDependsOnAllSuccessGroupList(false);
		execute(instance);
	}
}
