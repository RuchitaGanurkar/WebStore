package com.webstore.controller;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.service.WhatsAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
public class WhatsAppController {

    private final WhatsAppConfiguration whatsAppConfig;
    private final WhatsAppService whatsAppService;

    @Autowired
    public WhatsAppController(WhatsAppConfiguration whatsAppConfig, WhatsAppService whatsAppService) {
        this.whatsAppConfig = whatsAppConfig;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Webhook verification endpoint for WhatsApp Business API
     * This endpoint is called when you set up the webhook in Meta Developer Dashboard
     */
    @GetMapping("/whatsapp")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        log.info("Received verification request: mode={}, token={}", mode, token);

        // Verify the token matches our configuration
        if ("subscribe".equals(mode) && whatsAppConfig.getVerifyToken().equals(token)) {
            log.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }

    /**
     * Webhook endpoint to receive messages and events from WhatsApp
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("Received webhook payload");
        log.debug("Webhook payload: {}", payload);

        try {
            // Process the webhook asynchronously to prevent timeouts
            new Thread(() -> {
                try {
                    whatsAppService.processWebhook(payload);
                } catch (Exception e) {
                    log.error("Error processing webhook asynchronously", e);
                }
            }).start();

            // Always return 200 OK to acknowledge receipt
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("Error in webhook endpoint", e);
            // Still return 200 OK to prevent WhatsApp from retrying
            return ResponseEntity.ok("ERROR_HANDLED");
        }
    }
        /**
         * Send welcome message to a phone number
         */
        @PostMapping("/welcome")
        public ResponseEntity<String> sendWelcomeMessage(@RequestParam String phoneNumber) {
            boolean sent = whatsAppService.sendWelcomeMessage(phoneNumber);
            if (sent) {
                return ResponseEntity.ok("Welcome message sent successfully to " + phoneNumber);
            } else {
                return ResponseEntity.internalServerError().body("Failed to send welcome message");
            }
        }

        /**
         * Send catalogue list to a phone number
         */
        @PostMapping("/catalogues")
        public ResponseEntity<String> sendCatalogueList(@RequestParam String phoneNumber) {
            boolean sent = whatsAppService.sendCatalogueList(phoneNumber);
            if (sent) {
                return ResponseEntity.ok("Catalogue list sent successfully to " + phoneNumber);
            } else {
                return ResponseEntity.internalServerError().body("Failed to send catalogue list");
            }
        }

        /**
         * Send category list for a specific catalogue to a phone number
         */
        @PostMapping("/categories")
        public ResponseEntity<String> sendCategoryList(
                @RequestParam String phoneNumber,
                @RequestParam Integer catalogueId) {
            boolean sent = whatsAppService.sendCategoryList(phoneNumber, catalogueId);
            if (sent) {
                return ResponseEntity.ok("Category list sent successfully to " + phoneNumber);
            } else {
                return ResponseEntity.internalServerError().body("Failed to send category list");
            }
        }

        /**
         * Send product list for a specific category to a phone number
         */
        @PostMapping("/products")
        public ResponseEntity<String> sendProductList(
                @RequestParam String phoneNumber,
                @RequestParam Integer categoryId) {
            boolean sent = whatsAppService.sendProductList(phoneNumber, categoryId);
            if (sent) {
                return ResponseEntity.ok("Product list sent successfully to " + phoneNumber);
            } else {
                return ResponseEntity.internalServerError().body("Failed to send product list");
            }
        }

}