package com.webstore.service;

import com.webstore.dto.request.WhatsAppWebhookRequestDto;

public interface WhatsAppService {

    /**
     * Process incoming webhook payload from WhatsApp
     * @param payload The webhook payload as a string
     */
    void processWebhook(String payload);

    /**
     * Handle an incoming message from WhatsApp
     * @param message The message object from the webhook
     */
    void handleIncomingMessage(WhatsAppWebhookRequestDto.Entry.Change.Value.Message message);

    /**
     * Send a text message to a WhatsApp user
     * @param to Recipient's phone number
     * @param message Text message content
     * @return true if successful, false otherwise
     */
    boolean sendTextMessage(String to, String message);

    /**
     * Send a welcome message to a user
     * @param to Recipient's phone number
     * @return true if successful, false otherwise
     */
    boolean sendWelcomeMessage(String to);

    /**
     * Send the list of available catalogues
     * @param to Recipient's phone number
     * @return true if successful, false otherwise
     */
    boolean sendCatalogueList(String to);

    /**
     * Send categories for a specific catalogue
     * @param to Recipient's phone number
     * @param catalogueId The ID of the selected catalogue
     * @return true if successful, false otherwise
     */
    boolean sendCategoryList(String to, Integer catalogueId);

    /**
     * Send products for a specific category
     * @param to Recipient's phone number
     * @param categoryId The ID of the selected category
     * @return true if successful, false otherwise
     */
    boolean sendProductList(String to, Integer categoryId);
}