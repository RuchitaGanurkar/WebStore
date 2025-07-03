package com.webstore.service.whatsapp.button.impl;

import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

@Component
@RequiredArgsConstructor
public class WelcomeButtonStrategy implements ButtonActionStrategy {

    private final WhatsAppMessageSender messageSender;
    private final CategoryFlowService categoryFlowService;

    @Override
    public boolean supports(String buttonId) {
        return buttonId.startsWith("welcome_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        if ("welcome_hi".equals(buttonId)) {
            messageSender.sendTextMessage(phoneNumberId, from, "Hi there! 👋 Welcome to our store!");
            categoryFlowService.sendCategorySelection(API_VERSION, phoneNumberId, from);
        } else if ("welcome_info".equals(buttonId)) {
            String infoMessage = """
                    🏪 *WebStore is a multi-category e-commerce platform* supporting agricultural products, cooked food, and more.
                    Let me show you our categories!
                    """;
            messageSender.sendTextMessage(phoneNumberId, from, infoMessage);
            categoryFlowService.sendCategorySelection(API_VERSION, phoneNumberId, from);
        }
    }


}
