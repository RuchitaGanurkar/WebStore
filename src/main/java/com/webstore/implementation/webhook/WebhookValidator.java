package com.webstore.implementation.webhook;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.WebhookRequestDto;
import com.webstore.service.whatsapp.handler.impl.ButtonInteractionHandler;
import com.webstore.service.whatsapp.handler.impl.ListInteractionHandler;
import com.webstore.service.whatsapp.handler.impl.TextMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebhookValidator {

    private static final Logger logger = LoggerFactory.getLogger(WebhookValidator.class);

    private final WhatsAppConfiguration whatsAppConfig;
    private final ButtonInteractionHandler buttonHandler;
    private final ListInteractionHandler listHandler;
    private final TextMessageHandler textHandler;

    public WebhookValidator(WhatsAppConfiguration whatsAppConfig,                              ButtonInteractionHandler buttonHandler,
                            ListInteractionHandler listHandler,
                            TextMessageHandler textHandler) {
        this.whatsAppConfig = whatsAppConfig;
        this.buttonHandler = buttonHandler;
        this.listHandler = listHandler;
        this.textHandler = textHandler;
    }

    public void processIncomingMessage(WebhookRequestDto webhookData) {
        logger.info("Processing incoming webhook message");

        if (!isValidWebhookData(webhookData)) {
            logger.warn("Invalid webhook data received");
            return;
        }

        WebhookRequestDto.Message message = extractMessage(webhookData);
        if (message == null) {
            logger.warn("No message found in webhook data");
            return;
        }

        String phoneNumberId = extractPhoneNumberId(webhookData);
        String from = message.getFrom();

        // Route message based on type
        if ("text".equals(message.getType()) && message.getText() != null) {
            textHandler.handle(phoneNumberId, from, message.getText().getBody());
        } else if ("interactive".equals(message.getType()) && message.getInteractive() != null) {
            handleInteractiveMessage(phoneNumberId, from, message.getInteractive());
        }
    }

    private void handleInteractiveMessage(String phoneNumberId, String from,
                                          WebhookRequestDto.Interactive interactive) {
        logger.info("Processing interactive message type: {}", interactive.getType());

        if ("button_reply".equals(interactive.getType())) {
            String buttonId = interactive.getButtonReply().getId();
            logger.info("Button clicked: {}", buttonId);
            buttonHandler.handle(phoneNumberId, from, buttonId);
        } else if ("list_reply".equals(interactive.getType())) {
            String listId = interactive.getListReply().getId();
            logger.info("List item selected: {}", listId);
            listHandler.handle(phoneNumberId, from, listId);
        }
    }

    public String verifyWebhook(String mode, String token, String challenge) {
        if ("subscribe".equals(mode) && whatsAppConfig.getWebhook().getVerifyToken().equals(token)) {
            logger.info("Webhook verified successfully!");
            return challenge;
        }
        logger.warn("Webhook verification failed. Mode: {}, Token: {}", mode, token);
        return null;
    }

    public boolean isValidWebhookData(WebhookRequestDto webhookData) {
        return webhookData.getEntry() != null && !webhookData.getEntry().isEmpty() &&
                webhookData.getEntry().get(0).getChanges() != null &&
                !webhookData.getEntry().get(0).getChanges().isEmpty();
    }

    public WebhookRequestDto.Message extractMessage(WebhookRequestDto webhookData) {
        if (!isValidWebhookData(webhookData)) {
            return null;
        }

        WebhookRequestDto.Value value = webhookData.getEntry().get(0)
                .getChanges().get(0).getValue();
        return (value.getMessages() != null && !value.getMessages().isEmpty())
                ? value.getMessages().get(0) : null;
    }

    public String extractPhoneNumberId(WebhookRequestDto webhookData) {
        if (!isValidWebhookData(webhookData)) {
            return null;
        }

        return webhookData.getEntry().get(0).getChanges().get(0)
                .getValue().getMetadata().getPhoneNumberId();
    }
}