package com.webstore.implementation;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.WhatsAppInteractiveMessageRequestDto;
import com.webstore.dto.request.WhatsAppMessageRequestDto;
import com.webstore.dto.request.WhatsAppTemplateMessageRequestDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.ProductPrice;
import com.webstore.repository.CategoryRepository;
import com.webstore.repository.ProductPriceRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.CategoryService;
import com.webstore.service.ProductService;
import com.webstore.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductPriceRepository productPriceRepository;

    public WhatsAppServiceImplementation(WhatsAppConfiguration whatsAppConfig,
                                         RestTemplate restTemplate,
                                         CategoryRepository categoryRepository,
                                         CategoryService categoryService,
                                         ProductRepository productRepository,
                                         ProductService productService,
                                         ProductPriceRepository productPriceRepository) {
        this.whatsAppConfig = whatsAppConfig;
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.productService = productService;
        this.productPriceRepository = productPriceRepository;
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
                sendCategoryInteractiveMessage("v18.0", phoneNumberId, from);
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

                // Handle different button types
                if (buttonId.startsWith("cat_")) {
                    // Handle category selection
                    handleCategorySelection(phoneNumberId, from, buttonId);
                }
                else if (buttonId.startsWith("prod_")) {
                    // Handle product selection
                    handleProductSelection(phoneNumberId, from, buttonId);
                }
                else if (buttonId.startsWith("price_")) {
                    // Handle price viewing
                    handlePriceSelection(phoneNumberId, from, buttonId);
                }
                else if (buttonId.startsWith("add_cart_")) {
                    // Handle add to cart
                    handleAddToCart(phoneNumberId, from, buttonId);
                }
                else if (buttonId.startsWith("back_to_product_")) {
                    // Handle back to product navigation
                    handleBackToProduct(phoneNumberId, from, buttonId);
                }
                else if ("back_to_products".equals(buttonId)) {
                    // Handle back to products list
                    sendCategoryInteractiveMessage("v18.0", phoneNumberId, from);
                }
                else if (buttonId.startsWith("welcome_")) {
                    // Handle welcome button responses
                    handleWelcomeButtons(phoneNumberId, from, buttonId);
                }
                else {
                    // Unknown button ID
                    logger.warn("Unknown button ID received: {}", buttonId);
                    sendTextMessage(phoneNumberId, from, "I didn't understand that selection. Please try again.", null);
                }
            }
        }
    }

    @Override
    public void sendTextMessage(String phoneNumberId, String to, String messageText, String replyToMessageId) {
        // FIXED: Use getGraphUrl() instead of getBaseUrl()
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),  // Use GraphUrl, not BaseUrl
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
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),
                version,
                phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        // Prepare welcome buttons
        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = List.of(
                WhatsAppInteractiveMessageRequestDto.Button.builder()
                        .type("reply")
                        .reply(
                                WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                        .id("welcome_hi")
                                        .title("Hi 👋")
                                        .build()
                        )
                        .build(),
                WhatsAppInteractiveMessageRequestDto.Button.builder()
                        .type("reply")
                        .reply(
                                WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                        .id("welcome_info")
                                        .title("Tell me more")
                                        .build()
                        )
                        .build()
        );

        // Build the welcome message payload
        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(
                        WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                                .type("button")
                                .header(
                                        WhatsAppInteractiveMessageRequestDto.Header.builder()
                                                .type("text")
                                                .text("👋 Welcome to WebStore")
                                                .build()
                                )
                                .body(
                                        WhatsAppInteractiveMessageRequestDto.Body.builder()
                                                .text("Thanks for joining us! What would you like to do next?")
                                                .build()
                                )
                                .footer(
                                        WhatsAppInteractiveMessageRequestDto.Footer.builder()
                                                .text("Choose an option below")
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
            logger.info("Welcome interactive message sent to {}: {}", recipientPhoneNumber, response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send welcome interactive message: {}", e.getMessage(), e);
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

                    // Send products for the selected category
                    sendProductInteractiveMessage("v18.0", phoneNumberId, from, selectedCategory);
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing category ID: {}", e.getMessage());
                sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.", null);
            }
        }
    }

    @Override
    public void sendProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        String url = String.format("%s/%s/%s/messages",
                whatsAppConfig.getApi().getGraphUrl(),
                version,
                phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        // Get products by category name
        Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);
        if (categoryId == null) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Category not found: " + categoryName, null);
            return;
        }

        List<String> products = productRepository.findProductNamesByCategoryId(categoryId);
        logger.info("Fetched products from DB for category {}: {}", categoryName, products);

        if (products.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No products found in " + categoryName + " category.", null);
            return;
        }

        // Create buttons for each product (limit to 3 for interactive buttons)
        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = new ArrayList<>();

        int productCount = Math.min(products.size(), 3);
        for (int i = 0; i < productCount; i++) {
            String product = products.get(i);
            if (product != null && !product.isEmpty()) {
                buttons.add(
                        WhatsAppInteractiveMessageRequestDto.Button.builder()
                                .type("reply")
                                .reply(
                                        WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                                .id("prod_" + (i+1) + "_" + categoryName)
                                                .title(product.length() > 20 ? product.substring(0, 17) + "..." : product)
                                                .build()
                                )
                                .build()
                );
            }
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
                                                .text("Products in " + categoryName)
                                                .build()
                                )
                                .body(
                                        WhatsAppInteractiveMessageRequestDto.Body.builder()
                                                .text("Here are the available products:\n" +
                                                        products.stream()
                                                                .limit(3)
                                                                .map(prod -> "- " + prod)
                                                                .collect(Collectors.joining("\n")))
                                                .build()
                                )
                                .footer(
                                        WhatsAppInteractiveMessageRequestDto.Footer.builder()
                                                .text("Select a product to view details")
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
            logger.info("Interactive product message sent to {}: {}", recipientPhoneNumber, response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send interactive product message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendOneProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productRepository.findProductIdByProductName(productName);
            if (productId == null) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "Product not found: " + productName, null);
                return;
            }

            ProductResponseDto product = productService.getProductById(productId);

            String productDetails = String.format(
                    "📦 *%s*\n\n" +
                            "📝 Description: %s\n" +
                            "🏷️ Category: %s\n\n" +
                            "Would you like to see the pricing?",
                    product.getProductName(),
                    product.getProductDescription() != null ? product.getProductDescription() : "No description available",
                    product.getCategory() != null ? product.getCategory().getCategoryName() : "Unknown"
            );

            String url = String.format("%s/%s/%s/messages",
                    whatsAppConfig.getApi().getGraphUrl(),
                    version,
                    phoneNumberId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

            // Create buttons for product actions
            List<WhatsAppInteractiveMessageRequestDto.Button> buttons = List.of(
                    WhatsAppInteractiveMessageRequestDto.Button.builder()
                            .type("reply")
                            .reply(
                                    WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                            .id("price_" + productId)
                                            .title("View Price")
                                            .build()
                            )
                            .build(),
                    WhatsAppInteractiveMessageRequestDto.Button.builder()
                            .type("reply")
                            .reply(
                                    WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                            .id("back_to_products")
                                            .title("Back to Products")
                                            .build()
                            )
                            .build()
            );

            WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                    .to(recipientPhoneNumber)
                    .interactive(
                            WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                                    .type("button")
                                    .header(
                                            WhatsAppInteractiveMessageRequestDto.Header.builder()
                                                    .type("text")
                                                    .text("Product Details")
                                                    .build()
                                    )
                                    .body(
                                            WhatsAppInteractiveMessageRequestDto.Body.builder()
                                                    .text(productDetails)
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
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Product details message sent to {}: {}", recipientPhoneNumber, response.getBody());

        } catch (Exception e) {
            logger.error("Failed to send product details message: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Sorry, there was an error retrieving product details.", null);
        }
    }

    @Override
    public void showProductPriceInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productRepository.findProductIdByProductName(productName);
            if (productId == null) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "Product not found: " + productName, null);
                return;
            }

            // Get all prices for the product
            List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);

            if (productPrices.isEmpty()) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "No pricing information available for " + productName, null);
                return;
            }

            // Filter for INR currency (assuming currency ID 1 is INR)
            ProductPrice inrPrice = productPrices.stream()
                    .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                    .findFirst()
                    .orElse(productPrices.get(0)); // fallback to first available price

            // Format price from paise to rupees
            BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));

            String priceDetails = String.format(
                    "💰 *Price Information*\n\n" +
                            "📦 Product: %s\n" +
                            "💵 Price: %s %.2f\n" +
                            "💱 Currency: %s\n\n" +
                            "Would you like to add this to your cart?",
                    productName,
                    inrPrice.getCurrency().getCurrencySymbol(),
                    priceInRupees,
                    inrPrice.getCurrency().getCurrencyCode()
            );

            String url = String.format("%s/%s/%s/messages",
                    whatsAppConfig.getApi().getGraphUrl(),
                    version,
                    phoneNumberId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

            // Create buttons for pricing actions
            List<WhatsAppInteractiveMessageRequestDto.Button> buttons = List.of(
                    WhatsAppInteractiveMessageRequestDto.Button.builder()
                            .type("reply")
                            .reply(
                                    WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                            .id("add_cart_" + productId)
                                            .title("Add to Cart")
                                            .build()
                            )
                            .build(),
                    WhatsAppInteractiveMessageRequestDto.Button.builder()
                            .type("reply")
                            .reply(
                                    WhatsAppInteractiveMessageRequestDto.Reply.builder()
                                            .id("back_to_product_" + productId)
                                            .title("Back to Product")
                                            .build()
                            )
                            .build()
            );

            WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                    .to(recipientPhoneNumber)
                    .interactive(
                            WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                                    .type("button")
                                    .header(
                                            WhatsAppInteractiveMessageRequestDto.Header.builder()
                                                    .type("text")
                                                    .text("💰 Pricing Details")
                                                    .build()
                                    )
                                    .body(
                                            WhatsAppInteractiveMessageRequestDto.Body.builder()
                                                    .text(priceDetails)
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
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("Product price message sent to {}: {}", recipientPhoneNumber, response.getBody());

        } catch (Exception e) {
            logger.error("Failed to send product price message: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Sorry, there was an error retrieving price information.", null);
        }
    }

    @Override
    public void handleProductSelection(String phoneNumberId, String from, String productId) {
        logger.info("Handling product selection: {}", productId);

        // Parse product selection from button ID (e.g., "prod_1_Electronics" -> get first product from Electronics)
        String[] parts = productId.split("_");
        if (parts.length >= 3) {
            try {
                int productIndex = Integer.parseInt(parts[1]) - 1; // Convert to 0-based index
                String categoryName = parts[2];

                // Get category ID by name
                Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);
                if (categoryId == null) {
                    sendTextMessage(phoneNumberId, from, "Category not found: " + categoryName, null);
                    return;
                }

                // Get products in this category
                List<String> products = productRepository.findProductNamesByCategoryId(categoryId);

                if (productIndex >= 0 && productIndex < products.size()) {
                    String selectedProduct = products.get(productIndex);
                    logger.info("User selected product: {} from category: {}", selectedProduct, categoryName);

                    // Send detailed product information
                    sendOneProductInteractiveMessage("v18.0", phoneNumberId, from, selectedProduct);
                } else {
                    sendTextMessage(phoneNumberId, from, "Product not found at index: " + (productIndex + 1), null);
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing product selection: {}", e.getMessage());
                sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.", null);
            }
        } else {
            logger.error("Invalid product ID format: {}", productId);
            sendTextMessage(phoneNumberId, from, "Invalid product selection format.", null);
        }
    }

    // Method to handle price button clicks
    public void handlePriceSelection(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling price selection: {}", buttonId);

        // Extract product ID from button ID (e.g., "price_123" -> "123")
        String productIdStr = buttonId.replace("price_", "");

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // Get product name by ID
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                showProductPriceInteractiveMessage("v18.0", phoneNumberId, from, product.getProductName());
            } else {
                sendTextMessage(phoneNumberId, from, "Product not found.", null);
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing product ID from price button: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your request.", null);
        } catch (Exception e) {
            logger.error("Error retrieving product for price display: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error retrieving product information.", null);
        }
    }

    // Method to handle add to cart button clicks
    public void handleAddToCart(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling add to cart: {}", buttonId);

        // Extract product ID from button ID (e.g., "add_cart_123" -> "123")
        String productIdStr = buttonId.replace("add_cart_", "");

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // Get product name by ID
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                // For now, just send a confirmation message
                // TODO: Implement actual cart functionality
                String confirmationMessage = String.format(
                        "✅ *Added to Cart!*\n\n" +
                                "📦 Product: %s\n" +
                                "🛒 Your item has been added to your cart.\n\n" +
                                "Type 'cart' to view your cart or 'checkout' to proceed to checkout.",
                        product.getProductName()
                );

                sendTextMessage(phoneNumberId, from, confirmationMessage, null);
            } else {
                sendTextMessage(phoneNumberId, from, "Product not found.", null);
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing product ID from add to cart button: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem adding the item to your cart.", null);
        } catch (Exception e) {
            logger.error("Error adding product to cart: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error adding the item to your cart.", null);
        }
    }

    // Method to handle back to product navigation
    public void handleBackToProduct(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling back to product: {}", buttonId);

        // Extract product ID from button ID (e.g., "back_to_product_123" -> "123")
        String productIdStr = buttonId.replace("back_to_product_", "");

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // Get product name by ID
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                sendOneProductInteractiveMessage("v18.0", phoneNumberId, from, product.getProductName());
            } else {
                sendTextMessage(phoneNumberId, from, "Product not found.", null);
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing product ID from back to product button: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your request.", null);
        } catch (Exception e) {
            logger.error("Error retrieving product: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error retrieving product information.", null);
        }
    }

    // Method to handle welcome button responses
    public void handleWelcomeButtons(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling welcome button: {}", buttonId);

        if ("welcome_hi".equals(buttonId)) {
            sendTextMessage(phoneNumberId, from, "Hi there! 👋 Welcome to our store! Type 'categories' to see our products.", null);
        } else if ("welcome_info".equals(buttonId)) {
            String infoMessage = "🏪 *About Our Store*\n\n" +
                    "We offer a wide variety of products including:\n" +
                    "• Electronics\n" +
                    "• Clothing\n" +
                    "• Home Decor\n" +
                    "• And much more!\n\n" +
                    "Type 'menu' or 'categories' to start shopping!";
            sendTextMessage(phoneNumberId, from, infoMessage, null);

            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        }
    }
}