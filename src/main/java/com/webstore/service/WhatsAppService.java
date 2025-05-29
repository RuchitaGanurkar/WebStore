package com.webstore.service;

import com.webstore.dto.request.WhatsAppWebhookRequestDto;

public interface WhatsAppService {

    /**
     * Process incoming webhook message (handles both button and list interactions)
     * @param webhookData the webhook payload
     */
    void processIncomingMessage(WhatsAppWebhookRequestDto webhookData);

    /**
     * Verify webhook token
     * @param mode the hub mode
     * @param token the verification token
     * @param challenge the challenge string
     * @return the challenge string if verification succeeds, null otherwise
     */
    String verifyWebhook(String mode, String token, String challenge);

    /**
     * Send welcome message to a user
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     */
    void sendWelcomeMessage(String version, String phoneNumberId, String recipientPhoneNumber);

    /**
     * Send interactive message with categories (smart selection: buttons ≤3, list 4+)
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     */
    void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber);

    /**
     * Send interactive message with products by category (smart selection: buttons ≤3, list 4+)
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     * @param categoryName selected category name
     */
    void sendProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName);

    /**
     * Send interactive message with single product details
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     * @param productName selected product name
     */
    void sendOneProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName);

    /**
     * Show product pricing in INR currency
     * @param version API version
     * @param phoneNumberId business phone number ID
     * @param recipientPhoneNumber recipient's phone number
     * @param productName selected product name
     */
    void showProductPriceInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName);

    /**
     * Handle user's category selection from buttons
     * @param phoneNumberId business phone number ID
     * @param from user's phone number
     * @param categoryId selected category ID
     */
    void handleCategorySelection(String phoneNumberId, String from, String categoryId);

    /**
     * Handle user's product selection from buttons
     * @param phoneNumberId business phone number ID
     * @param from user's phone number
     * @param productId selected product ID
     */
    void handleProductSelection(String phoneNumberId, String from, String productId);
}