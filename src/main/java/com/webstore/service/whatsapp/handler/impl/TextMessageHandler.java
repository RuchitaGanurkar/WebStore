package com.webstore.service.whatsapp.handler.impl;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.flow.CartFlowService;
import com.webstore.service.whatsapp.handler.InteractionHandler;
import org.springframework.stereotype.Service;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

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
        String trimmed = messageText.trim().toLowerCase();

        switch (trimmed) {
            case "hi":
            case "hello":
            case "hey":
                sendInitialWelcome(phoneNumberId, from);
                break;
            case "categories":
            case "menu":
            case "start":
                categoryFlowService.sendCategorySelection(API_VERSION, phoneNumberId, from);
                break;
            case "cart":
                cartFlowService.viewCart(phoneNumberId, from);
                break;
            default:
                messageSender.sendTextMessage(phoneNumberId, from,
                        "Echo: " + messageText + "\n\nType 'categories' to browse products!");
        }
    }

    private void sendInitialWelcome(String phoneNumberId, String from) {
        String welcomeText = """
                👋 *Welcome to WebStore*
                Thanks for joining us! What would you like to do next?

                Type *categories* to see product categories.
                """;

        messageSender.sendTextMessage(phoneNumberId, from, welcomeText);
    }
}
