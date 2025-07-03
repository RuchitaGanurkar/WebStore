package com.webstore.service.whatsapp.strategy;

public interface ProductDisplayStrategy {
    boolean supports(int productCount);
    void display(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName);
}
