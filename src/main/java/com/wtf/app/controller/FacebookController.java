package com.wtf.app.controller;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.impls.starter.service.zFacebook.FacebookBroadcastSelfSkillsToFBGroupsStarter;
import com.wtf.app.service.impls.starter.service.zFacebook.FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter;

import com.wtf.app.service.impls.starter.service.zFacebook.FacebookBroadcastGreetOffFreelancerInFBGroupsStarter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/social-app/fb")
@Lazy
public class FacebookController {
    private static final Logger logger = LogManager.getLogger(FacebookController.class);

    private final FacebookBroadcastSelfSkillsToFBGroupsStarter facebookBroadcastSelfSkillsToFBGroupsStarter;
    private final FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter facebookBroadcastSelfSkillsToFBSensitiveGroupsStarter;
    private final FacebookBroadcastGreetOffFreelancerInFBGroupsStarter facebookBroadcastGreetOffFreelancerInFBGroupsStarter;
    

    public FacebookController(FacebookBroadcastSelfSkillsToFBGroupsStarter facebookBroadcastSelfSkillsToFBGroupsStarter,
                             FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter facebookBroadcastSelfSkillsToFBSensitiveGroupsStarter,
                             FacebookBroadcastGreetOffFreelancerInFBGroupsStarter facebookBroadcastGreetOffFreelancerInFBGroupsStarter) {
        this.facebookBroadcastSelfSkillsToFBGroupsStarter = facebookBroadcastSelfSkillsToFBGroupsStarter;
        this.facebookBroadcastSelfSkillsToFBSensitiveGroupsStarter = facebookBroadcastSelfSkillsToFBSensitiveGroupsStarter;
        this.facebookBroadcastGreetOffFreelancerInFBGroupsStarter = facebookBroadcastGreetOffFreelancerInFBGroupsStarter;
        
        logger.info("FacebookController initialized");
    }


    /* Facebook Operations */

    @GetMapping("/broadcast/skillset")
    public ResponseEntity<Map<String, Object>> startBroadcastSelfSkillsToFBGroups() {
        logger.info("Starting FB skillset broadcast");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast/skillset");

        try {
            AutomationContext context = new AutomationContext(TaskType.FACEBOOK_BROADCAST_AD);
            facebookBroadcastSelfSkillsToFBGroupsStarter.start(context);
            logger.info("Successfully completed FB skillset broadcast");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during FB skillset broadcast", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/broadcast/sensitive")
    public ResponseEntity<Map<String, Object>> startBroadcastSelfSkillsToFBSensitiveGroups() {
        logger.info("Starting FB sensitive group broadcast");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast/sensitive");

        try {
            AutomationContext context = new AutomationContext(TaskType.FACEBOOK_BROADCAST_AD);
            facebookBroadcastSelfSkillsToFBSensitiveGroupsStarter.start(context);
            logger.info("Successfully completed FB sensitive group broadcast");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during FB sensitive group broadcast", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/broadcast/offshore")
    public ResponseEntity<Map<String, Object>> startBroadcastGreetOffFreelancerInFBGroups() {
        logger.info("Starting FB offshore broadcast");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast/offshore");

        try {
            AutomationContext context = new AutomationContext(TaskType.FACEBOOK_BROADCAST_AD);
            facebookBroadcastGreetOffFreelancerInFBGroupsStarter.start(context);
            logger.info("Successfully completed FB offshore broadcast");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during FB offshore broadcast", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
