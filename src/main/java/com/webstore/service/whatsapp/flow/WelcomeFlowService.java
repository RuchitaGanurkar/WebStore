package com.webstore.service.whatsapp.flow;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WelcomeFlowService {

    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;

    public WelcomeFlowService(WhatsAppMessageSender messageSender,
                              MessageBuilderService messageBuilder) {
        this.messageSender = messageSender;
        this.messageBuilder = messageBuilder;
    }

    public void sendWelcomeMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        List<WhatsAppRequestDto.Button> buttons = List.of(
                messageBuilder.createButton("welcome_hi", "Hi 👋"),
                messageBuilder.createButton("welcome_info", "Tell me more")
        );

        WhatsAppRequestDto requestBody = messageBuilder.buildButtonMessage(
                recipientPhoneNumber,
                "👋 Welcome to WebStore",
                "Thanks for joining us! What would you like to do next?",
                "Choose an option below",
                buttons
        );

        messageSender.sendMessage(phoneNumberId, requestBody, "Welcome message");
    }
}