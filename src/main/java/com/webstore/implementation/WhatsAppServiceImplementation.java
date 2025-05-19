package com.webstore.implementation;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.WhatsAppMessageRequestDto;
import com.webstore.dto.request.WhatsAppTemplateMessageRequestDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.repository.CategoryRepository;
import com.webstore.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final CategoryRepository categoryRepository;


    public WhatsAppServiceImplementation(WhatsAppConfiguration whatsAppConfig, RestTemplate restTemplate, CategoryRepository categoryRepository) {
        this.whatsAppConfig = whatsAppConfig;
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
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
    public void sendWelcomeMessageTemplate(String version, String phoneNumberId, String recipientPhoneNumber) {
        // Construct the URL using the version and phoneNumberId parameters
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),
                version,
                phoneNumberId);

        System.out.println("Sending template message to URL: " + url); // Debugging log

        // Set up the HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        // Build the message request matching the required JSON structure
        WhatsAppTemplateMessageRequestDto requestBody = WhatsAppTemplateMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .template(
                        WhatsAppTemplateMessageRequestDto.Template.builder()
                                .name("first_welcome_template")
                                .language(
                                        WhatsAppTemplateMessageRequestDto.Language.builder()
                                                .code("en")
                                                .build()
                                )
                                .build()
                )
                .build();

        // Note: We don't need to set messaging_product and type as they have default values in the DTO

        HttpEntity<WhatsAppTemplateMessageRequestDto> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Template message sent successfully to {}: {}", recipientPhoneNumber, response.getBody());
        } catch (Exception e) {
            logger.error("Error sending template message: {}", e.getMessage(), e);
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

    @Override
    public void sendCategoryTemplateMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),
                version,
                phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        List<String> categories = categoryRepository.findTop3CategoryNames();

        // Ensure 3 categories max to match template
        while (categories.size() < 3) {
            categories.add("-");
        }

        logger.info("Fetched categories from DB: {}", categories);

        WhatsAppTemplateMessageRequestDto requestBody = WhatsAppTemplateMessageRequestDto.builder()
                .messaging_product("whatsapp")
                .to(recipientPhoneNumber)
                .template(
                        WhatsAppTemplateMessageRequestDto.Template.builder()
                                .name("list_all_categories_template")
                                .language(WhatsAppTemplateMessageRequestDto.Language.builder().code("en").build())
                                .components(List.of(
                                        WhatsAppTemplateMessageRequestDto.Component.builder()
                                                .type("button")
                                                .parameters(List.of(
                                                        new WhatsAppTemplateMessageRequestDto.Parameter("button", categories.get(0)),
                                                        new WhatsAppTemplateMessageRequestDto.Parameter("button", categories.get(1)),
                                                        new WhatsAppTemplateMessageRequestDto.Parameter("button", categories.get(2))
                                                ))
                                                .build()
                                ))
                                .build()
                )
                .build();

        HttpEntity<WhatsAppTemplateMessageRequestDto> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Category template message sent to {}: {}", recipientPhoneNumber, response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send category template message: {}", e.getMessage(), e);
        }
    }

}