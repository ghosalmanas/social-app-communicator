package com.wtf.app.app.com.parent;

import com.wtf.app.app.commons.FacebookCommonUtils;

import java.awt.*;
import java.io.IOException;

public abstract class FacebookBroadcastParent extends FacebookCommonUtils {

	public FacebookBroadcastParent(String baseURL) {
		super(baseURL);
	}

	@Override
	public void traverseGroupsTemplate(FacebookCommonUtils instance)
			throws InterruptedException, AWTException, IOException {
		instance.runFacebookAction(instance);
	}

}
