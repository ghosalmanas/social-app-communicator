package com.wtf.app.service.impls.starter.service.whatsApp;

import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.parent.WATGCommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Scope("prototype")
@Component("socialMediaParentRunner")
public class SocialMediaParentRunner {
    private static final Logger logger = LogManager.getLogger(SocialMediaParentRunner.class);

    public boolean execute(AutomationContext context, SocialParentCommonUtils instance, SocialModel socialModel, TaskType taskType) throws InterruptedException, AWTException, IOException {
        if (instance instanceof WATGCommonUtils) {
        }

        instance.setSocialModel(socialModel);
        instance.setxPathInterface(socialModel.getxPathInterface());
        instance.setBaseUrl(socialModel.getBaseURL());
        instance.setTaskTypeMapping(taskType, (WATGCommonUtils) instance);
        logger.info("socialType : " + socialModel.getSocialType() + "...baseURL : "+ socialModel.getBaseURL()+" taskType : "+taskType+" instance : "+instance);

        instance.traverseGroupsTemplate(instance);
        return true;
    }

}
