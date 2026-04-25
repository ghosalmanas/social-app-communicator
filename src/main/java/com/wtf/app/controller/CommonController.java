package com.wtf.app.controller;

// Java imports
import java.util.Arrays;
import java.util.List;

// Spring imports
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.ParallelAutomationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final ParallelAutomationService parallelAutomationService;

    @Autowired
    public CommonController(ApplicationContext applicationContext, 
                          InitialSetup initialSetup,
                          ParallelAutomationService parallelAutomationService) {
        this.applicationContext = applicationContext;
        this.initialSetup = initialSetup;
        this.parallelAutomationService = parallelAutomationService;
        
        logger.info("CommonController initialized with RequestContext");
        // Call test API on initialization
        testApiEasily();
    }

    /**
     * Start all enabled automations in parallel.
     * Copied from AutomationController for easy access at top of CommonController.
     */
    @PostMapping("/automation/start")
    public ResponseEntity<String> startAllAutomations() {
        try {
            TaskType whatsappFetchContacts = TaskType.WHATSAPP_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(whatsappFetchContacts);
            logger.info("Kicking off all automations with context ID: {}", context.getTaskId());

            parallelAutomationService.startAllAutomations(context, List.of(whatsappFetchContacts, TaskType.TELEGRAM_FETCH_CONTACTS, TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS, TaskType.TELEGRAM_BROADCAST_AD, TaskType.FACEBOOK_BROADCAST_AD, TaskType.FACEBOOK_BROADCAST_SENSITIVE_AD, TaskType.TELEGRAM_SEARCH_LOOKING_FOR, TaskType.WHATSAPP_SEARCH_LOOKING_FOR));

            return ResponseEntity.ok("All automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start automations: " + e.getMessage());
        }
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
