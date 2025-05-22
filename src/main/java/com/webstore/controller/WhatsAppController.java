package com.webstore.controller;

import com.webstore.dto.request.WhatsAppTemplateMessageRequestDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.service.WhatsAppService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /**
     * Handles incoming webhook messages from WhatsApp
     *
     * @param webhookData the webhook payload
     * @return HTTP 200 OK response
     */
    @PostMapping("/")
    public ResponseEntity<Void> receiveMessage(@RequestBody WhatsAppWebhookRequestDto webhookData) {
        whatsAppService.processIncomingMessage(webhookData);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles webhook verification for WhatsApp API
     *
     * @param mode      the hub mode
     * @param token     the verification token
     * @param challenge the challenge string
     * @return the challenge string if verification succeeds, or 403 Forbidden if it fails
     */
    @GetMapping("/")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        String response = whatsAppService.verifyWebhook(mode, token, challenge);

        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Endpoint to send a welcome template message to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/send-welcome-template/messages")
    public ResponseEntity<String> sendWelcomeMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestBody WhatsAppTemplateMessageRequestDto requestBody) {

        String recipientPhoneNumber = requestBody.getTo();
        whatsAppService.sendWelcomeMessageTemplate(version, phoneNumberId, recipientPhoneNumber);
        return ResponseEntity.ok("Welcome template message sent successfully");
    }

    /**
     * Endpoint to send a category template message to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/send-category-template/messages")
    public ResponseEntity<String> sendCategoryTemplateMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone) {

        whatsAppService.sendCategoryTemplateMessage(version, phoneNumberId, phone);
        return ResponseEntity.ok("Category template message sent successfully");
    }

    /**
     * Endpoint to send interactive category buttons to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/send-category-interactive/messages")
    public ResponseEntity<String> sendCategoryInteractiveMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone) {

        whatsAppService.sendCategoryInteractiveMessage(version, phoneNumberId, phone);
        return ResponseEntity.ok("Interactive category message sent successfully");
    }

    /**
     * Endpoint to send interactive product buttons by category to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/send-product-interactive/messages")
    public ResponseEntity<String> sendProductInteractiveMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String categoryName) {

        whatsAppService.sendProductInteractiveMessage(version, phoneNumberId, phone, categoryName);
        return ResponseEntity.ok("Interactive product message sent successfully");
    }

    /**
     * Endpoint to send interactive single product details to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/send-one-product-interactive/messages")
    public ResponseEntity<String> sendOneProductInteractiveMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String productName) {

        whatsAppService.sendOneProductInteractiveMessage(version, phoneNumberId, phone, productName);
        return ResponseEntity.ok("Product details message sent successfully");
    }

    /**
     * Endpoint to show product pricing in INR currency to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/show-product-price-interactive/messages")
    public ResponseEntity<String> showProductPriceInteractiveMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String productName) {

        whatsAppService.showProductPriceInteractiveMessage(version, phoneNumberId, phone, productName);
        return ResponseEntity.ok("Product price message sent successfully");
    }

    /**
     * Endpoint to send a manual text message to a WhatsApp user
     */
    @PostMapping("/{version}/{phoneNumberId}/send-text/messages")
    public ResponseEntity<String> sendTextMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone,
            @RequestParam String message) {

        whatsAppService.sendTextMessage(phoneNumberId, phone, message, null);
        return ResponseEntity.ok("Text message sent successfully");
    }
}