package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import com.wtf.app.model.xpaths.XPathInterfaceFB;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.springframework.beans.factory.annotation.Autowired;
import java.awt.*;
import java.io.IOException;

@Component("facebookBroadcastParent")
public abstract class FacebookBroadcastParent extends FacebookCommonUtils {

    private static final Logger logger = LogManager.getLogger(FacebookBroadcastParent.class);
    
    @Autowired
    protected FacebookBroadcastParent(XPathInterfaceFB xPathInterfaceFB, 
                                    
                                    InitialSetup initialSetup,
									ParallelWebDriverManager parallelWebDriverManager) {
        super(xPathInterfaceFB,  initialSetup, parallelWebDriverManager);
        logger.info("FacebookBroadcastParent initialized with  InitialSetup, and ParallelWebDriverManager for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
    }

	@Override
	public void traverseGroupsTemplate(SocialParentCommonUtils instance)
			throws InterruptedException, AWTException, IOException {
		traverseGroupsTemplate((FacebookCommonUtils)instance);

	}
	@Override
	public void traverseGroupsTemplate(FacebookCommonUtils instance)
			throws InterruptedException, AWTException, IOException {
		initialSetup.setupAndExecuteSocialBySocialType(/*instance.getSocialModel().getBaseURL(),*/ instance.getSocialModel().getSocialType());
		instance.runFacebookAction(instance);
	}

}
