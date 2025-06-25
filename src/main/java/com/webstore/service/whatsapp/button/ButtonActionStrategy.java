package com.webstore.service.whatsapp.button;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;

public interface ButtonActionStrategy {
    boolean supports(String buttonId);
    void handle(String phoneNumberId, String from, String buttonId);
}