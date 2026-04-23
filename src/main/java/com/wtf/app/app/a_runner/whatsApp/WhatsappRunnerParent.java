package com.wtf.app.app.a_runner.whatsApp;

import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.xpaths.WhatsApp_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;

public class WhatsappRunnerParent {

	public static void execute(WhatsappCommonUtils instance) throws InterruptedException, AWTException, IOException {
		XPathInterfaceWATG xPathInterface = new WhatsApp_Xpaths();
		instance.setxPathInterface(xPathInterface);
		instance.traverseGroupsTemplate(instance);
	}
}
