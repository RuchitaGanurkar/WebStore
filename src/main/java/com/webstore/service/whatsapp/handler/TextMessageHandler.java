package com.webstore.service.whatsapp.handler;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.flow.CartFlowService;
import org.springframework.stereotype.Service;

@Service
public class TextMessageHandler implements InteractionHandler<String> {

    private final WhatsAppMessageSender messageSender;
    private final CategoryFlowService categoryFlowService;
    private final CartFlowService cartFlowService;

    public TextMessageHandler(WhatsAppMessageSender messageSender,
                              CategoryFlowService categoryFlowService,
                              CartFlowService cartFlowService) {
        this.messageSender = messageSender;
        this.categoryFlowService = categoryFlowService;
        this.cartFlowService = cartFlowService;
    }

    @Override
    public void handle(String phoneNumberId, String from, String messageText) {
        if (messageText.equalsIgnoreCase("categories") ||
                messageText.equalsIgnoreCase("menu") ||
                messageText.equalsIgnoreCase("start")) {
            categoryFlowService.sendCategorySelection("v22.0", phoneNumberId, from);
        } else if (messageText.equalsIgnoreCase("cart")) {
            cartFlowService.viewCart(phoneNumberId, from);
        } else {
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Echo: " + messageText + "\n\nType 'categories' to browse products!");
        }
    }
}
