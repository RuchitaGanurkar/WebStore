package com.webstore.implementation;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.WhatsAppInteractiveMessageRequestDto;
import com.webstore.dto.request.WhatsAppMessageRequestDto;
import com.webstore.dto.request.WhatsAppTemplateMessageRequestDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.repository.CategoryRepository;
import com.webstore.service.CategoryService;
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
import java.util.stream.Collectors;

@Service
public class WhatsAppServiceImplementation implements WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppServiceImplementation.class);

    private final WhatsAppConfiguration whatsAppConfig;
    private final RestTemplate restTemplate;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public WhatsAppServiceImplementation(WhatsAppConfiguration whatsAppConfig,
                                         RestTemplate restTemplate,
                                         CategoryRepository categoryRepository,
                                         CategoryService categoryService) {
        this.whatsAppConfig = whatsAppConfig;
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
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
        String from = message.getFrom();

        // Process text messages
        if ("text".equals(message.getType()) && message.getText() != null) {
            String messageText = message.getText().getBody();
            String messageId = message.getId();

            // Check for specific commands
            if (messageText.equalsIgnoreCase("categories") ||
                    messageText.equalsIgnoreCase("menu") ||
                    messageText.equalsIgnoreCase("start")) {

                // Send category list as interactive buttons
                sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
            } else {
                // Echo the message back to the user
                sendTextMessage(phoneNumberId, from, "Echo: " + messageText, messageId);
            }
        }
        // Process interactive messages (button clicks)
        else if ("interactive".equals(message.getType()) && message.getInteractive() != null) {
            String interactiveType = message.getInteractive().getType();

            if ("button_reply".equals(interactiveType)) {
                String buttonId = message.getInteractive().getButtonReply().getId();
                // Handle the button click (assuming button ID is the category ID)
                handleCategorySelection(phoneNumberId, from, buttonId);
            }
        }
    }

    @Override
    public void sendTextMessage(String phoneNumberId, String to, String messageText, String replyToMessageId) {
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getBaseUrl(),
                whatsAppConfig.getApi().getVersion(),
                phoneNumberId);

        logger.debug("Sending text message to URL: {}", url);

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
            logger.info("Text message sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error sending text message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendWelcomeMessageTemplate(String version, String phoneNumberId, String recipientPhoneNumber) {
        // Construct the URL using the version and phoneNumberId parameters
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),
                version,
                phoneNumberId);

        logger.debug("Sending welcome template message to URL: {}", url);

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

        HttpEntity<WhatsAppTemplateMessageRequestDto> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Welcome template message sent successfully to {}: {}", recipientPhoneNumber, response.getBody());
        } catch (Exception e) {
            logger.error("Error sending welcome template message: {}", e.getMessage(), e);
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
                whatsAppConfig.getApi().getBaseUrl(),
                version,
                phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        List<String> categories = categoryRepository.findTop3CategoryNames();
        logger.info("Fetched categories from DB: {}", categories);

        // Ensure 3 categories max to match template
        while (categories.size() < 3) {
            categories.add("-");
        }

        // This approach with templates will be deprecated - using interactive messages instead
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
                                                        new WhatsAppTemplateMessageRequestDto.Parameter("text", categories.get(0)),
                                                        new WhatsAppTemplateMessageRequestDto.Parameter("text", categories.get(1)),
                                                        new WhatsAppTemplateMessageRequestDto.Parameter("text", categories.get(2))
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

    @Override
    public void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),
                version,
                phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        // Get categories from database (limited to first 3 for simplicity)
        List<String> categories = categoryRepository.findTop3CategoryNames();
        logger.info("Fetched categories from DB for interactive message: {}", categories);

        // Create buttons for each category
        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = new ArrayList<>();

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            if (category != null && !category.isEmpty()) {
                buttons.add(
                        WhatsAppInteractiveMessageRequestDto.Button.builder()
                                .type("reply")
                                .reply(
                                        WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                                .id("cat_" + (i+1))
                                                .title(category)
                                                .build()
                                )
                                .build()
                );
            }
        }

        // Add a "See all options" button if we have more categories
        if (categoryRepository.count() > 3) {
            buttons.add(
                    WhatsAppInteractiveMessageRequestDto.Button.builder()
                            .type("reply")
                            .reply(
                                    WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                            .id("cat_see_all")
                                            .title("See all options")
                                            .build()
                            )
                            .build()
            );
        }

        // Build the interactive message
        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(
                        WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                                .type("button")
                                .header(
                                        WhatsAppInteractiveMessageRequestDto.Header.builder()
                                                .type("text")
                                                .text("WebStore Available Categories")
                                                .build()
                                )
                                .body(
                                        WhatsAppInteractiveMessageRequestDto.Body.builder()
                                                .text("Hi, here are your categories:\n" +
                                                        categories.stream()
                                                                .map(cat -> "- " + cat)
                                                                .collect(Collectors.joining("\n")))
                                                .build()
                                )
                                .footer(
                                        WhatsAppInteractiveMessageRequestDto.Footer.builder()
                                                .text("reply with category name")
                                                .build()
                                )
                                .action(
                                        WhatsAppInteractiveMessageRequestDto.Action.builder()
                                                .buttons(buttons)
                                                .build()
                                )
                                .build()
                )
                .build();

        HttpEntity<WhatsAppInteractiveMessageRequestDto> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Interactive category message sent to {}: {}", recipientPhoneNumber, response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send interactive category message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleCategorySelection(String phoneNumberId, String from, String categoryId) {
        // Extract the category number from the button ID (e.g., "cat_1" -> "1")
        String categoryIdStr = categoryId.replace("cat_", "");

        if ("see_all".equals(categoryIdStr)) {
            // Handle "See all options" button
            sendTextMessage(phoneNumberId, from, "Here are all available categories:", null);
            // TODO: Send a more comprehensive list of categories
        } else {
            try {
                int categoryNumber = Integer.parseInt(categoryIdStr);
                // Fetch the selected category by its position
                List<String> categories = categoryRepository.findTop3CategoryNames();

                if (categoryNumber > 0 && categoryNumber <= categories.size()) {
                    String selectedCategory = categories.get(categoryNumber - 1);

                    // Send a confirmation message
                    sendTextMessage(phoneNumberId, from, "You selected: " + selectedCategory, null);

                    // TODO: Send product list for the selected category
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing category ID: {}", e.getMessage());
                sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.", null);
            }
        }
    }
}