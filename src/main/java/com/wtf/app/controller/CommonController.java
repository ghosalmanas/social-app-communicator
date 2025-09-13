package com.wtf.app.controller;

// Java imports
import java.util.Arrays;
import java.util.List;

// Spring imports
import com.wtf.app.model.enums.SocialType;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Log4j2 imports
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Application imports
import com.wtf.app.commons.InitialSetup;

@RestController
@RequestMapping("/social-app")
public class CommonController {

    private static final Logger logger = LogManager.getLogger(CommonController.class);

    private final ApplicationContext applicationContext;
    private final InitialSetup initialSetup;
    

    public CommonController(ApplicationContext applicationContext, 
                          InitialSetup initialSetup) {
        this.applicationContext = applicationContext;
        this.initialSetup = initialSetup;
        
        logger.info("CommonController initialized with RequestContext");
        // Call test API on initialization
        testApiEasily();
    }

    @GetMapping("/test")
    public String testApiEasily() {
        logger.info("testApiEasily invoked in CommonController");
        return "SUCCESS";
    }

    @GetMapping("/beans")
    public List<String> getAllBeans() {
        return Arrays.asList(applicationContext.getBeanDefinitionNames());
    }

    @GetMapping("/kill")
    public String killTheInitialSetupExecution() {
        if (initialSetup.getDriver() != null) {
           try {
               //kill the browser
               initialSetup.getDriver().close();
               initialSetup.getDriver().quit();
               initialSetup.closeBrowser();
           } catch (Exception e) {
               e.printStackTrace();
           }
        }
        return "KILLED";
    }

    @GetMapping("/reset-browser-session/whatsapp")
    public String resetBrowserSessionWhatsapp() {
        try {
            initialSetup.setupAndExecuteSocialBySocialType(SocialType.WHATSAPP);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "RESET DONE";
    }
    @GetMapping("/reset-browser-session/telegram")
    public String resetBrowserSessionTelegram() {
        try {
            initialSetup.setupAndExecuteSocialBySocialType(SocialType.TELEGRAM);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "RESET DONE";
    }
    @GetMapping("/reset-browser-session/facebook")
    public String resetBrowserSessionFacebook() {
        try {
            initialSetup.setupAndExecuteSocialBySocialType(SocialType.FACEBOOK);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "RESET DONE";
    }

    @GetMapping("/kill-app")
    public String killTheApplication() {
        System.exit(1);
        return "KILLED APPLICATION";
    }
}
