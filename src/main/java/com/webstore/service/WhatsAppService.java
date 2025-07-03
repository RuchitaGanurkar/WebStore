package com.webstore.service;

public interface WhatsAppService {

    String verifyWebhook(String mode, String token, String challenge);
    void sendWelcomeMessage(String version, String phoneNumberId, String recipientPhoneNumber);
    void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber);
    void sendProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName);
    void sendOneProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName);
    void showProductPriceInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName);
}
