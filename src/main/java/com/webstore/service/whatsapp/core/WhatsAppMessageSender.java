package com.webstore.service.whatsapp.core;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppMessageSender.class);

    private final WhatsAppConfiguration whatsAppConfig;
    private final RestTemplate restTemplate;

    public WhatsAppMessageSender(WhatsAppConfiguration whatsAppConfig, RestTemplate restTemplate) {
        this.whatsAppConfig = whatsAppConfig;
        this.restTemplate = restTemplate;
    }

    public void sendMessage(String phoneNumberId, WhatsAppRequestDto requestBody, String messageType) {
        String url = buildUrl(phoneNumberId);

        // ✅ ADDED: Debug logging to see the actual request
        logger.info("Sending {} to URL: {}", messageType, url);
        logger.info("Request body: {}", requestBody);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(requestBody, createHeaders()), String.class);
            logger.info("{} sent successfully: {}", messageType, response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send {}: {}", messageType, e.getMessage());

            // ✅ IMPROVED: Better error logging
            if (e.getMessage().contains("400")) {
                logger.error("400 Bad Request - Check phone number ID and request body format");
                logger.error("URL used: {}", url);
                logger.error("Phone Number ID: {}", phoneNumberId);
            }

            // Fallback for list messages
            if (messageType.contains("list")) {
                String fallbackMessage = "🔧 **Technical Issue**\n\nSorry, there was a problem displaying the list. Please type 'categories' to try again or contact support.";
                WhatsAppRequestDto fallbackRequest = WhatsAppRequestDto.createTextMessage(requestBody.getTo(), fallbackMessage);
                sendMessage(phoneNumberId, fallbackRequest, "fallback text message");
            }
        }
    }

    public void sendTextMessage(String phoneNumberId, String to, String messageText) {
        WhatsAppRequestDto requestBody = WhatsAppRequestDto.createTextMessage(to, messageText);
        sendMessage(phoneNumberId, requestBody, "Text message");
    }

    // ✅ FIXED: Correct URL format with phone number ID in path
    private String buildUrl(String phoneNumberId) {
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),    // https://graph.facebook.com
                whatsAppConfig.getApi().getVersion(),     // v22.0
                phoneNumberId);                           // Your phone number ID

        logger.debug("Built URL: {}", url);
        return url;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());
        return headers;
    }
}