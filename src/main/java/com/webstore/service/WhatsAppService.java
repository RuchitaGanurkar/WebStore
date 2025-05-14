package com.webstore.service;

import com.webstore.dto.request.WhatsAppWebhookRequestDto;

import java.util.List;
import java.util.Map;

public interface WhatsAppService {

    /**
     * Process incoming webhook message
     * @param webhookData the webhook payload
     */
    void processIncomingMessage(WhatsAppWebhookRequestDto webhookData);

    /**
     * Send a text message to a WhatsApp user
     * @param phoneNumberId business phone number ID
     * @param to recipient's phone number
     * @param messageText the message text
     * @param replyToMessageId optional message ID to reply to
     */
    void sendTextMessage(String phoneNumberId, String to, String messageText, String replyToMessageId);
    /**
     * Verify webhook token
     * @param mode the hub mode
     * @param token the verification token
     * @param challenge the challenge string
     * @return the challenge string if verification succeeds, null otherwise
     */
    String verifyWebhook(String mode, String token, String challenge);
}