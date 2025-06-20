package com.webstore.controller.whatsapp;

import com.webstore.dto.request.WhatsAppRequestDto;
import com.webstore.dto.request.WebhookRequestDto;
import com.webstore.service.WhatsAppService;
import com.webstore.implementation.webhook.WebhookValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WhatsAppController {

    private final WhatsAppService whatsAppService;
    private final WebhookValidator webhookValidator;

    public WhatsAppController(WhatsAppService whatsAppService, WebhookValidator webhookValidator) {
        this.whatsAppService = whatsAppService;
        this.webhookValidator = webhookValidator;
    }

    /**
     * Handles incoming webhook messages from WhatsApp
     */
    @PostMapping("/")
    public ResponseEntity<Void> receiveMessage(@RequestBody WebhookRequestDto webhookData) {
        webhookValidator.processIncomingMessage(webhookData);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles webhook verification for WhatsApp API
     */
    @GetMapping("/")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        String response = whatsAppService.verifyWebhook(mode, token, challenge);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Send welcome message to start the conversation flow
     */
    @PostMapping("/{version}/{phoneNumberId}/send-welcome/messages")
    public ResponseEntity<String> sendWelcomeMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestBody WhatsAppRequestDto requestBody) {

        String recipientPhoneNumber = requestBody.getTo();
        whatsAppService.sendWelcomeMessage(version, phoneNumberId, recipientPhoneNumber);
        return ResponseEntity.ok("Welcome message sent successfully");
    }

    /**
     * Send category interactive message
     */
    @PostMapping("/{version}/{phoneNumberId}/send-categories/messages")
    public ResponseEntity<String> sendCategoryMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone) {

        whatsAppService.sendCategoryInteractiveMessage(version, phoneNumberId, phone);
        return ResponseEntity.ok("All categories details sent successfully");
    }

    /**
     * Send product list by category
     */
    @PostMapping("/{version}/{phoneNumberId}/send-products/messages")
    public ResponseEntity<String> sendProductMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String categoryName) {

        whatsAppService.sendProductInteractiveMessage(version, phoneNumberId, phone, categoryName);
        return ResponseEntity.ok("All products details sent successfully");
    }

    /**
     * Send single product details
     */
    @PostMapping("/{version}/{phoneNumberId}/send-product-details/messages")
    public ResponseEntity<String> sendProductDetailsMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String productName) {

        whatsAppService.sendOneProductInteractiveMessage(version, phoneNumberId, phone, productName);
        return ResponseEntity.ok("Single product detail sent successfully");
    }

    /**
     * Send product pricing
     */
    @PostMapping("/{version}/{phoneNumberId}/send-pricing/messages")
    public ResponseEntity<String> sendPricingMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String productName) {

        whatsAppService.showProductPriceInteractiveMessage(version, phoneNumberId, phone, productName);
        return ResponseEntity.ok("Products pricing details sent successfully");
    }
}
