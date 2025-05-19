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
     * @param mode the hub mode
     * @param token the verification token
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
     * Follows the Facebook Graph API pattern with version and phoneNumberId as path variables
     */
    @PostMapping("/{version}/{phoneNumberId}/send-welcome-template")
    public ResponseEntity<String> sendWelcomeMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestBody WhatsAppTemplateMessageRequestDto requestBody) {

        String recipientPhoneNumber = requestBody.getTo();
        whatsAppService.sendWelcomeMessageTemplate(version, phoneNumberId, recipientPhoneNumber);
        return ResponseEntity.ok("Welcome template message sent successfully");
    }


    @PostMapping("/{version}/{phoneNumberId}/send-category-template/messages")
    public ResponseEntity<String> sendCategoryTemplateMessage(
            @PathVariable("version") String version,
            @PathVariable("phoneNumberId") String phoneNumberId,
            @RequestParam String phone) {

        whatsAppService.sendCategoryTemplateMessage(version, phoneNumberId, phone);
        return ResponseEntity.ok("Category template message sent successfully");
    }

}