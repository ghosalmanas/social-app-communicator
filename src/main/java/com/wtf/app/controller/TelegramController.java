package com.wtf.app.controller;

import com.wtf.app.service.impls.starter.service.zTelegram.TelegramBroadcastSelfSkillsToTGGroupsStarter;
import com.wtf.app.service.impls.starter.service.zTelegram.TelegramFetchAllWAContactsGroupNamesJoinLinksStarter;
import com.wtf.app.service.impls.starter.service.zTelegram.TelegramSearchAndExtractLookingForStarter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;

@RestController
@RequestMapping("/social-app/tg")
public class TelegramController {
    private static final Logger logger = LogManager.getLogger(TelegramController.class);

    private final TelegramFetchAllWAContactsGroupNamesJoinLinksStarter telegramFetchAllWAContactsGroupNamesJoinLinksStarter;
    private final TelegramBroadcastSelfSkillsToTGGroupsStarter telegramBroadcastSelfSkillsToTGGroupsStarter;
    private final TelegramSearchAndExtractLookingForStarter telegramSearchAndExtractLookingForStarter;

    @Value("${telegram.search.looking.for.enabled:false}")
    private boolean telegramSearchLookingForEnabled;
    

    @Autowired
    public TelegramController(@Lazy TelegramFetchAllWAContactsGroupNamesJoinLinksStarter telegramFetchAllWAContactsGroupNamesJoinLinksStarter,
                              @Lazy TelegramBroadcastSelfSkillsToTGGroupsStarter telegramBroadcastSelfSkillsToTGGroupsStarter,
                              @Lazy TelegramSearchAndExtractLookingForStarter telegramSearchAndExtractLookingForStarter
                              ) {
        this.telegramFetchAllWAContactsGroupNamesJoinLinksStarter = telegramFetchAllWAContactsGroupNamesJoinLinksStarter;
        this.telegramBroadcastSelfSkillsToTGGroupsStarter = telegramBroadcastSelfSkillsToTGGroupsStarter;
        this.telegramSearchAndExtractLookingForStarter = telegramSearchAndExtractLookingForStarter;
        
        logger.info("TelegramController initialized");
    }



    /* Telegram Operations */

    @GetMapping("/traverse")
    public ResponseEntity<Map<String, Object>> startTraverseWAAndJoinNewGroupsTG() {
        logger.info("Starting TG contacts and groups traversal");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/traverse");
        
        try {

            // Execute the operation
            AutomationContext context = new AutomationContext(TaskType.TELEGRAM_FETCH_CONTACTS);
            telegramFetchAllWAContactsGroupNamesJoinLinksStarter.start(context);
            
            logger.info("Successfully completed TG contacts and groups traversal");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during TG contacts and groups traversal", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> startBroadcastSelfSkillsToTGGroups() {
        logger.info("Starting TG broadcast");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast");
        
        try {

            // Execute the operation
            AutomationContext context = new AutomationContext(TaskType.TELEGRAM_BROADCAST_AD);
            telegramBroadcastSelfSkillsToTGGroupsStarter.start(context);
            
            logger.info("Successfully completed TG broadcast");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during TG broadcast", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testApiEasily() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Telegram API is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-looking-for")
    public ResponseEntity<Map<String, Object>> startSearchAndExtractLookingFor() {
        logger.info("Starting TG search and extract 'I am looking for' messages");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/search-looking-for");
        
        // Check if the feature is enabled
        if (!telegramSearchLookingForEnabled) {
            logger.warn("Telegram search and extract 'I am looking for' is disabled in application.properties");
            response.put("status", "DISABLED");
            response.put("message", "Feature is disabled. Set telegram.search.looking.for.enabled=true in application.properties to enable.");
            return ResponseEntity.ok(response);
        }
        
        try {
            // Execute the operation
            AutomationContext context = new AutomationContext(TaskType.TELEGRAM_FETCH_CONTACTS);
            telegramSearchAndExtractLookingForStarter.start(context);
            
            logger.info("Successfully completed TG search and extract 'I am looking for' messages");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during TG search and extract 'I am looking for' messages", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
