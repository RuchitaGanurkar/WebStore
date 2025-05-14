package com.webstore.implementation;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.WhatsAppMessageRequestDto;
import com.webstore.dto.request.WhatsAppTemplateMessageRequestDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class WhatsAppServiceImplementation implements WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppServiceImplementation.class);

    private final WhatsAppConfiguration whatsAppConfig;
    private final RestTemplate restTemplate;

    public WhatsAppServiceImplementation(WhatsAppConfiguration whatsAppConfig, RestTemplate restTemplate) {
        this.whatsAppConfig = whatsAppConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public void processIncomingMessage(WhatsAppWebhookRequestDto webhookData) {
        logger.info("Incoming webhook message: {}", webhookData);

        if (webhookData.getEntry() == null || webhookData.getEntry().isEmpty()) {
            logger.warn("No entries in webhook data");
            return;
        }

        WhatsAppWebhookRequestDto.Entry entry = webhookData.getEntry().get(0);
        if (entry.getChanges() == null || entry.getChanges().isEmpty()) {
            logger.warn("No changes in webhook entry");
            return;
        }

        WhatsAppWebhookRequestDto.Change change = entry.getChanges().get(0);
        WhatsAppWebhookRequestDto.Value value = change.getValue();

        if (value.getMessages() == null || value.getMessages().isEmpty()) {
            logger.warn("No messages in webhook value");
            return;
        }

        WhatsAppWebhookRequestDto.Message message = value.getMessages().get(0);
        String phoneNumberId = value.getMetadata().getPhoneNumberId();

        // Process text messages
        if ("text".equals(message.getType()) && message.getText() != null) {
            String from = message.getFrom();
            String messageText = message.getText().getBody();
            String messageId = message.getId();

            // Echo the message back to the user
            sendTextMessage(phoneNumberId, from, "Echo: " + messageText, messageId);

        }
    }

    @Override
    public void sendTextMessage(String phoneNumberId, String to, String messageText, String replyToMessageId) {
        String url = String.format("%s/%s/%s",
                whatsAppConfig.getApi().getBaseUrl(),
                whatsAppConfig.getApi().getVersion(),
                phoneNumberId);

        System.out.println("Sending message to URL: " + url); // Add this for debugging

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        // Build the message request
        WhatsAppMessageRequestDto requestBody = WhatsAppMessageRequestDto.builder()
                .messaging_product("whatsapp")
                .to(to)
                .text(new WhatsAppMessageRequestDto.TextBody(messageText))
                .build();

        // Add reply context if replying to a message
        if (replyToMessageId != null) {
            requestBody.setContext(new WhatsAppMessageRequestDto.Context(replyToMessageId));
        }

        HttpEntity<WhatsAppMessageRequestDto> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Message sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    @Override
    public String verifyWebhook(String mode, String token, String challenge) {
        if ("subscribe".equals(mode) && whatsAppConfig.getWebhook().getVerifyToken().equals(token)) {
            logger.info("Webhook verified successfully!");
            return challenge;
        } else {
            logger.warn("Webhook verification failed. Mode: {}, Token: {}", mode, token);
            return null;
        }
    }

}