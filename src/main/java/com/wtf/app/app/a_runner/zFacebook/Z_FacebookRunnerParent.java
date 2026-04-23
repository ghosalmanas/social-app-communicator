package com.wtf.app.app.a_runner.zFacebook;

import com.wtf.app.app.commons.FacebookCommonUtils;
import com.wtf.app.app.xpaths.Facebook_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceFB;

import java.awt.*;
import java.io.IOException;

public class Z_FacebookRunnerParent {

	public static void execute(FacebookCommonUtils instance) throws InterruptedException, AWTException, IOException {
		XPathInterfaceFB xPathInterface = new Facebook_Xpaths();
		instance.setTraverseDependsOnAllSuccessGroupList(false);
		instance.setxPathInterface(xPathInterface);
		instance.traverseGroupsTemplate(instance);
	}
}
