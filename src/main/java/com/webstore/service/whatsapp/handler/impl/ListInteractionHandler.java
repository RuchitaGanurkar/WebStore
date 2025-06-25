package com.webstore.service.whatsapp.handler.impl;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.handler.InteractionHandler;
import com.webstore.service.whatsapp.list.ListActionStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListInteractionHandler implements InteractionHandler<String> {

    private final List<ListActionStrategy> strategies;
    private final WhatsAppMessageSender messageSender;

    public ListInteractionHandler(List<ListActionStrategy> strategies,
                                  WhatsAppMessageSender messageSender) {
        this.strategies = strategies;
        this.messageSender = messageSender;
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        for (ListActionStrategy strategy : strategies) {
            if (strategy.supports(listId)) {
                strategy.handle(phoneNumberId, from, listId);
                return;
            }
        }
        messageSender.sendTextMessage(phoneNumberId, from,
                "I didn't understand that selection. Please try again.");
    }
}
