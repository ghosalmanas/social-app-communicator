package com.wtf.app.controller;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.impls.starter.service.whatsApp.*;
import org.springframework.beans.factory.annotation.Value;
import com.wtf.app.service.impls.WATGBroadcastMessageToAllGroupsService;
import com.wtf.app.service.interfaces.IAllWAGroupNamesRetrievalAndJoinNewGroups;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.wtf.app.model.dto.AutomationContext;

@RestController
@RequestMapping("/social-app/wa")
public class WhatsappController {
    private static final Logger logger = LogManager.getLogger(WhatsappController.class);

    private final WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter whatsappFetchAllWAContactsGroupNamesJoinLinksStarter;
    private final WhatsappBroadcastSelfSkillsToWAGroupsStarter whatsappBroadcastSelfSkillsToWAGroupsStarter;
    private final WhatsAppSearchAndExtractLookingForStarter whatsappSearchAndExtractLookingForStarter;
    /*private final WhatsappBroadcastReqsToOffSuppMembersStarter.WhatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter whatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter;
    private final WhatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter whatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter;*/
    private final IAllWAGroupNamesRetrievalAndJoinNewGroups watgFetchAllWAContactsWGroupNamesService;
    private final WATGBroadcastMessageToAllGroupsService watgBroadcastMessageToAllGroupsService;
    private final ApplicationContext applicationContext;
    private final InitialSetup initialSetup;

    @Value("${whatsapp.search.looking.for.enabled:false}")
    private boolean whatsappSearchLookingForEnabled;

    @Autowired
    public WhatsappController(WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter whatsappFetchAllWAContactsGroupNamesJoinLinksStarter,
                            WhatsappBroadcastSelfSkillsToWAGroupsStarter whatsappBroadcastSelfSkillsToWAGroupsStarter,
                            WhatsAppSearchAndExtractLookingForStarter whatsappSearchAndExtractLookingForStarter,/*
                            WhatsappBroadcastReqsToOffSuppMembersStarter.WhatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter whatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter,
                            WhatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter whatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter,*/

                              @Qualifier("whatsAppFetchService")
                              IAllWAGroupNamesRetrievalAndJoinNewGroups watgFetchAllWAContactsWGroupNamesService,
                            @Qualifier("whatsAppBroadcastService")
                            WATGBroadcastMessageToAllGroupsService watgBroadcastMessageToAllGroupsService,
                            ApplicationContext applicationContext,
                            InitialSetup initialSetup) {
        this.whatsappFetchAllWAContactsGroupNamesJoinLinksStarter = whatsappFetchAllWAContactsGroupNamesJoinLinksStarter;
        this.whatsappBroadcastSelfSkillsToWAGroupsStarter = whatsappBroadcastSelfSkillsToWAGroupsStarter;
        this.whatsappSearchAndExtractLookingForStarter = whatsappSearchAndExtractLookingForStarter;
        /*this.whatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter = whatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter;
        this.whatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter = whatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter;*/
        this.watgFetchAllWAContactsWGroupNamesService = watgFetchAllWAContactsWGroupNamesService;
        this.watgBroadcastMessageToAllGroupsService = watgBroadcastMessageToAllGroupsService;
        this.applicationContext = applicationContext;
        this.initialSetup = initialSetup;
        
        logger.info("WhatsappController initialized");
    }

    /* Whatsapp Operations */
    @GetMapping("/traverse")
    public ResponseEntity<Map<String, Object>> startTraverseWAAndJoinNewGroupsWA() {
        logger.info("Starting WA contacts and groups traversal");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/traverse");
        
        try {
            // Execute the operation
            AutomationContext context = new AutomationContext(TaskType.WHATSAPP_FETCH_CONTACTS);
            whatsappFetchAllWAContactsGroupNamesJoinLinksStarter.start(context);
            
            logger.info("Successfully completed WA contacts and groups traversal");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during WA contacts and groups traversal", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> startBroadcastSelfSkillsToWAGroups() {
        logger.info("Starting WA broadcast");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast");
        
        try {
            AutomationContext context = new AutomationContext(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            whatsappBroadcastSelfSkillsToWAGroupsStarter.start(context);
            logger.info("Successfully completed WA broadcast");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during WA broadcast", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/broadcast/known_clients")
    public ResponseEntity<Map<String, Object>> startBroadcastSelfSkillsToWA_Known_Clients() {
        logger.info("Starting WA broadcast to known clients");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast/known_clients");
        
        try {
            AutomationContext context = new AutomationContext(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            //whatsappBroadcastSelfSkillsToKnownConsultantClients_from9007Only_sendAt8pmStarter.start(context);
            logger.info("Successfully completed WA broadcast to known clients");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during WA broadcast to known clients", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/broadcast/unknown_clients")
    public ResponseEntity<Map<String, Object>> startBroadcastSelfSkillsToWA_Unknown_Clients() {
        logger.info("Starting WA broadcast to unknown clients");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/broadcast/unknown_clients");
        
        try {
            AutomationContext context = new AutomationContext(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            //whatsappBroadcastSelfSkillsToUnknownConsultantClientsStarter.start(context);
            logger.info("Successfully completed WA broadcast to unknown clients");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during WA broadcast to unknown clients", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search-looking-for")
    public ResponseEntity<Map<String, Object>> startSearchAndExtractLookingFor() {
        logger.info("Starting WA search and extract 'I am looking for' messages");
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/search-looking-for");
        
        // Check if the feature is enabled
        if (!whatsappSearchLookingForEnabled) {
            logger.warn("WhatsApp search and extract 'I am looking for' is disabled in application.properties");
            response.put("status", "DISABLED");
            response.put("message", "Feature is disabled. Set whatsapp.search.looking.for.enabled=true in application.properties to enable.");
            return ResponseEntity.ok(response);
        }
        
        try {
            // Execute the operation
            AutomationContext context = new AutomationContext(TaskType.WHATSAPP_SEARCH_LOOKING_FOR);
            whatsappSearchAndExtractLookingForStarter.start(context);
            
            logger.info("Successfully completed WA search and extract 'I am looking for' messages");
            response.put("status", "COMPLETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during WA search and extract 'I am looking for' messages", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/clear/already_broadcasted")
    public ResponseEntity<Map<String, Object>> clearAlreadyBroadcasted() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/clear/already_broadcasted");
        
        try {
            boolean result = watgBroadcastMessageToAllGroupsService.clearAlreadyTraversedOrBroadcastedGroupsInExecution(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            response.put("status", "SUCCESS");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing already broadcasted groups", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/clear/already_traversed_or_broadcasted")
    public ResponseEntity<Map<String, Object>> clearAlreadyTraversed() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/clear/already_traversed_or_broadcasted");
        
        try {
            boolean result = watgFetchAllWAContactsWGroupNamesService.clearAlreadyTraversedOrBroadcastedGroupsInExecution(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            response.put("status", "SUCCESS");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing already traversed or broadcasted groups", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/clear/already_traversed_or_broadcasted_db_and_execution")
    public ResponseEntity<Map<String, Object>> clearAlreadyTraversedDbExec() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/clear/already_traversed_or_broadcasted_db_execution");

        try {
            boolean result = watgFetchAllWAContactsWGroupNamesService.clearAlreadyTraversedOrBroadcastedGroupsFromDBExecution(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            response.put("status", "SUCCESS");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing already traversed or broadcasted groups", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/clear/already_traversed_or_broadcasted_db_and_execution/groups")
    public ResponseEntity<Map<String, Object>> clearAlreadyTraversedDbExecRequest(@RequestParam String groups) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/clear/already_traversed_or_broadcasted_db_execution");

        try {
            boolean result = watgFetchAllWAContactsWGroupNamesService.removeGroupsFromAlreadyTraversed(Arrays.asList(groups.split(",")));
            response.put("status", "SUCCESS");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing already traversed or broadcasted groups", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/clear/already_traversed_in_db_file")
    public ResponseEntity<Map<String, Object>> clearAllAlreadyTraversedFromDBFile() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/clear/already_traversed_in_db_file");
        
        try {
            boolean result = watgFetchAllWAContactsWGroupNamesService.clearAllAlreadyTraversedInDBFile(TaskType.WHATSAPP_FETCH_CONTACTS);
            response.put("status", "SUCCESS");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing already traversed groups from DB file", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/clear/already_broadcasted_in_db_file")
    public ResponseEntity<Map<String, Object>> clearAlreadyBroadcastedFromDBFile() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint", "/clear/already_broadcasted_in_db_file");
        
        try {
            boolean result = watgBroadcastMessageToAllGroupsService.clearAllAlreadyTraversedInDBFile(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS);
            response.put("status", "SUCCESS");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing already broadcasted groups from DB file", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
