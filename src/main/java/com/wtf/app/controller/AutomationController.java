package com.wtf.app.controller;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.service.ParallelAutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing automation tasks.
 * Provides endpoints to start, stop, and monitor automations.
 * Handles concurrent requests with deduplication and rate limiting.
 */
@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private static final Logger logger = LoggerFactory.getLogger(AutomationController.class);
    private final ParallelAutomationService parallelAutomationService;

    @Autowired
    public AutomationController(ParallelAutomationService parallelAutomationService) {
        this.parallelAutomationService = parallelAutomationService;
    }

    /**
     * Start all enabled automations in parallel.
     */
    @PostMapping("/start")
    public ResponseEntity<String> startAllAutomations() {
        try {
            TaskType whatsappFetchContacts = TaskType.WHATSAPP_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(whatsappFetchContacts); // Or a more generic type
            logger.info("Kicking off all automations with context ID: {}", context.getTaskId());

            parallelAutomationService.startAllAutomations(context, List.of(whatsappFetchContacts, TaskType.TELEGRAM_FETCH_CONTACTS, TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS, TaskType.TELEGRAM_BROADCAST_AD, TaskType.FACEBOOK_BROADCAST_AD,TaskType.FACEBOOK_BROADCAST_SENSITIVE_AD));

            return ResponseEntity.ok("All automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start automations: " + e.getMessage());
        }
    }

    /**
     * Start only WhatsApp automations.
     */
    @PostMapping("/whatsapp/both/start")
    public ResponseEntity<String> startWhatsAppAutomation() {
        try {

            TaskType whatsappFetchContacts = TaskType.WHATSAPP_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(whatsappFetchContacts); // TaskType can be refined
            logger.info("Kicking off WhatsApp automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(whatsappFetchContacts, TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS)); // This will run all, can be refined later
            return ResponseEntity.ok("WhatsApp automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start WhatsApp automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start WhatsApp automations: " + e.getMessage());
        }
    }

    /**
     * Start only WhatsApp automations to fetch contacts.
     */
    @PostMapping("/whatsapp/fetch/start")
    public ResponseEntity<String> startWhatsAppFetchAutomations() {
        try {
            TaskType whatsappFetchContacts = TaskType.WHATSAPP_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(whatsappFetchContacts);
            logger.info("Kicking off WhatsApp Fetch automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(whatsappFetchContacts));
            return ResponseEntity.ok("WhatsApp Fetch automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start WhatsApp Fetch automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start WhatsApp Fetch automations: " + e.getMessage());
        }
    }

    /**
     * Start only WhatsApp broadcast automations.
     */
    @PostMapping("/whatsapp/broadcast/start")
    public ResponseEntity<String> startWhatsAppBroadcastAutomation() {
        try {
            TaskType whatsappBroadcastAd = TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS;
            AutomationContext context = new AutomationContext(whatsappBroadcastAd);
            logger.info("Kicking off WhatsApp Broadcast automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(whatsappBroadcastAd));
            return ResponseEntity.ok("WhatsApp Broadcast automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start WhatsApp Broadcast automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start WhatsApp Broadcast automations: " + e.getMessage());
        }
    }

    /**
     * Start only Telegram automations.
     */
    @PostMapping("/telegram/both/start")
    public ResponseEntity<String> startTelegramAutomation() {
        try {
            TaskType telegramFetchContacts = TaskType.TELEGRAM_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(telegramFetchContacts); // TaskType can be refined
            logger.info("Kicking off Telegram automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(telegramFetchContacts, TaskType.TELEGRAM_BROADCAST_AD)); // This will run all, can be refined later
            return ResponseEntity.ok("Telegram automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start Telegram automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start Telegram automations: " + e.getMessage());
        }
    }

    /**
     * Start both WhatsApp and Telegram automations to fetch contacts.
     */
    @PostMapping("/watg/fetch/both/start")
    public ResponseEntity<String> startWhatsAppTelegramFetchAutomations() {
        try {
            TaskType whatsappFetchContacts = TaskType.WHATSAPP_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(whatsappFetchContacts); // Generic context
            logger.info("Kicking off WA & TG Fetch automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(whatsappFetchContacts, TaskType.TELEGRAM_FETCH_CONTACTS));
            return ResponseEntity.ok("WhatsApp and Telegram Fetch automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start WA & TG Fetch automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start WA & TG Fetch automations: " + e.getMessage());
        }
    }

    /**
     * Start both WhatsApp and Telegram broadcast automations.
     */
    @PostMapping("/watg/broadcast/both/start")
    public ResponseEntity<String> startWhatsAppTelegramBroadcastAutomations() {
        try {
            TaskType whatsappBroadcastAd = TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS;
            AutomationContext context = new AutomationContext(whatsappBroadcastAd); // Generic context
            logger.info("Kicking off WA & TG Broadcast automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(whatsappBroadcastAd, TaskType.TELEGRAM_BROADCAST_AD));
            return ResponseEntity.ok("WhatsApp and Telegram Broadcast automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start WA & TG Broadcast automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start WA & TG Broadcast automations: " + e.getMessage());
        }
    }

    /**
     * Start both WhatsApp and Telegram broadcast automations.
     */
    @PostMapping("/facebook/broadcast/start")
    public ResponseEntity<String> startFacebookBroadcastAutomation() {
        try {
            TaskType facebookBroadcastAd = TaskType.FACEBOOK_BROADCAST_AD;
            AutomationContext context = new AutomationContext(facebookBroadcastAd); // Generic context
            logger.info("Kicking off Facebook Broadcast automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(facebookBroadcastAd));
            return ResponseEntity.ok("Facebook Broadcast automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start Facebook Broadcast automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start Facebook Broadcast automations: " + e.getMessage());
        }
    }

    /**
     * Start both WhatsApp and Telegram broadcast automations.
     */
    @PostMapping("/waTgFb/broadcast/all-3/start")
    public ResponseEntity<String> startWhatsAppTelegramFacebookBroadcastAutomations() {
        try {
            TaskType whatsappBroadcastAd = TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS;
            AutomationContext context = new AutomationContext(whatsappBroadcastAd); // Generic context
            logger.info("Kicking off WA,TG & FB Broadcast automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(whatsappBroadcastAd, TaskType.TELEGRAM_BROADCAST_AD, TaskType.FACEBOOK_BROADCAST_AD));
            return ResponseEntity.ok("WhatsApp, Telegram and Facebook Broadcast automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start WA,TG & FB Broadcast automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start WA,TG & FB Broadcast automations: " + e.getMessage());
        }
    }


    /**
     * Start only Telegram automations to fetch contacts.
     */
    @PostMapping("/telegram/fetch/start")
    public ResponseEntity<String> startTelegramFetchAutomations() {
        try {
            TaskType telegramFetchContacts = TaskType.TELEGRAM_FETCH_CONTACTS;
            AutomationContext context = new AutomationContext(telegramFetchContacts);
            logger.info("Kicking off Telegram Fetch automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(telegramFetchContacts));
            return ResponseEntity.ok("Telegram Fetch automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start Telegram Fetch automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start Telegram Fetch automations: " + e.getMessage());
        }
    }

    /**
     * Start only Telegram broadcast automations.
     */
    @PostMapping("/telegram/broadcast/start")
    public ResponseEntity<String> startTelegramBroadcastAutomation() {
        try {
            TaskType telegramBroadcastAd = TaskType.TELEGRAM_BROADCAST_AD;
            AutomationContext context = new AutomationContext(telegramBroadcastAd);
            logger.info("Kicking off Telegram Broadcast automations with context ID: {}", context.getTaskId());
            parallelAutomationService.startAllAutomations(context, List.of(telegramBroadcastAd));
            return ResponseEntity.ok("Telegram Broadcast automation tasks started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start Telegram Broadcast automations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start Telegram Broadcast automations: " + e.getMessage());
        }
    }


    // The methods below are now obsolete with the new design but are kept for reference or future use if needed.
    // They should be removed in a future cleanup.

    @PostMapping("/whatsapp/broadcast/old")
    public ResponseEntity<String> runWhatsAppBroadcast() {
        return ResponseEntity.status(HttpStatus.GONE).body("This endpoint is obsolete.");
    }

    @PostMapping("/telegram/broadcast/old")
    public ResponseEntity<String> runTelegramBroadcast() {
        return ResponseEntity.status(HttpStatus.GONE).body("This endpoint is obsolete.");
    }
}
