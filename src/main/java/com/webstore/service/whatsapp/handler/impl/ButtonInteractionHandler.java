package com.webstore.service.whatsapp.handler.impl;

import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.handler.InteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ButtonInteractionHandler implements InteractionHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(ButtonInteractionHandler.class);

    private final List<ButtonActionStrategy> strategies;
    private final WhatsAppMessageSender messageSender;

    public ButtonInteractionHandler(List<ButtonActionStrategy> strategies,
                                    WhatsAppMessageSender messageSender) {
        this.strategies = strategies;
        this.messageSender = messageSender;
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling button click: {}", buttonId);

        for (ButtonActionStrategy strategy : strategies) {
            if (strategy.supports(buttonId)) {
                strategy.handle(phoneNumberId, from, buttonId);
                return;
            }
        }

        logger.warn("No strategy found for button ID: {}", buttonId);
        messageSender.sendTextMessage(phoneNumberId, from,
                "I didn't understand that selection. Please try again.");
    }
}
