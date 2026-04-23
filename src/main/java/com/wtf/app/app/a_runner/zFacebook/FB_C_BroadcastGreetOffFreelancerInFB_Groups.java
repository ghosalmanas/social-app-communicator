package com.wtf.app.app.a_runner.zFacebook;

import com.wtf.app.app.com.parent.FacebookBroadcastParent;
import com.wtf.app.app.impls.FB_C_BroadcastForOffSuppAnnonym;
import com.wtf.app.app.xpaths.Facebook_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceFB;

import java.awt.*;
import java.io.IOException;

public class FB_C_BroadcastGreetOffFreelancerInFB_Groups extends Z_FacebookRunnerParent{

	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		XPathInterfaceFB xPathInterface= new Facebook_Xpaths();
		FacebookBroadcastParent instance = new FB_C_BroadcastForOffSuppAnnonym(xPathInterface.getBASE_URL());
		execute(instance);
	}
}
