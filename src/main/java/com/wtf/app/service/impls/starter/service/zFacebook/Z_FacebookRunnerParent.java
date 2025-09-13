package com.wtf.app.service.impls.starter.service.zFacebook;

import com.wtf.app.parent.FacebookCommonUtils;
import com.wtf.app.model.xpaths.Facebook_Xpaths;
import com.wtf.app.model.xpaths.XPathInterfaceFB;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
public class Z_FacebookRunnerParent {

	public static void execute(FacebookCommonUtils instance) throws InterruptedException, AWTException, IOException {
		XPathInterfaceFB xPathInterface = new Facebook_Xpaths();
		instance.setTraverseDependsOnAllSuccessGroupList(false);
		instance.setxPathInterface(xPathInterface);
		instance.traverseGroupsTemplate(instance);
	}
}
