package com.wtf.app.app.a_runner.whatsApp;

import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.impls.WA_C_TG_BroadcastMessageToAllGroups;
import com.wtf.app.app.xpaths.WhatsApp_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class WA_C_BroadcastSelfSkillsToWA_Groups extends WhatsappRunnerParent{
	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		XPathInterfaceWATG xPathInterface = new WhatsApp_Xpaths();
		WhatsappCommonUtils instance = new WA_C_TG_BroadcastMessageToAllGroups(xPathInterface.getBASE_URL(), SocialType.WHATSAPP);
		instance.setTraverseDependsOnAllSuccessGroupList(false);
		execute(instance);
	}
}
