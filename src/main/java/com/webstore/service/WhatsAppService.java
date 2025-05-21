package com.webstore.service;

import com.webstore.dto.request.WhatsAppWebhookRequestDto;

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

    /**
     * Send welcome message template to a user
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     */
    void sendWelcomeMessageTemplate(String version, String phoneNumberId, String recipientPhoneNumber);

    /**
     * Send category template message to a user
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     */
    void sendCategoryTemplateMessage(String version, String phoneNumberId, String recipientPhoneNumber);

    /**
     * Send interactive message with category buttons to a user
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     */
    void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber);

    /**
     * Handle user's category selection
     * @param phoneNumberId business phone number ID
     * @param from user's phone number
     * @param categoryId selected category ID
     */
    void handleCategorySelection(String phoneNumberId, String from, String categoryId);
}