package com.wtf.app.service.impls.starter.service.whatsApp;

import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.model.xpaths.WhatsApp_Xpaths;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
@NoArgsConstructor
public class WhatsappRunnerParent {
    protected static final Logger logger = LogManager.getLogger(WhatsappRunnerParent.class);

	public static void execute(WATGCommonUtils instance, TaskType taskType) throws InterruptedException, AWTException, IOException {
		XPathInterfaceWATG xPathInterface = new WhatsApp_Xpaths();
		instance.setxPathInterface(xPathInterface);
		instance.setTaskTypeMapping(taskType,instance);
		instance.traverseGroupsTemplate(instance);
	}
}
