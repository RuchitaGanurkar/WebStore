package com.webstore.implementation;

import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.request.WhatsAppInteractiveMessageRequestDto;
import com.webstore.dto.request.WhatsAppMessageRequestDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.ProductPrice;
import com.webstore.repository.CategoryRepository;
import com.webstore.repository.ProductPriceRepository;
import com.webstore.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductPriceRepository productPriceRepository;

    public WhatsAppServiceImplementation(WhatsAppConfiguration whatsAppConfig,
                                         RestTemplate restTemplate,
                                         CategoryRepository categoryRepository,
                                         ProductRepository productRepository,
                                         ProductService productService,
                                         ProductPriceRepository productPriceRepository) {
        this.whatsAppConfig = whatsAppConfig;
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.productPriceRepository = productPriceRepository;
    }

    @Override
    public void processIncomingMessage(WhatsAppWebhookRequestDto webhookData) {
        logger.info("Processing incoming webhook message");

        if (!isValidWebhookData(webhookData)) return;

        WhatsAppWebhookRequestDto.Message message = extractMessage(webhookData);
        if (message == null) return;

        String phoneNumberId = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMetadata().getPhoneNumberId();
        String from = message.getFrom();

        // Handle text messages
        if ("text".equals(message.getType()) && message.getText() != null) {
            processTextMessage(phoneNumberId, from, message.getText().getBody());
        }
        // Handle interactive button clicks
        else if ("interactive".equals(message.getType()) && message.getInteractive() != null) {
            processInteractiveMessage(phoneNumberId, from, message.getInteractive());
        }
    }

    @Override
    public String verifyWebhook(String mode, String token, String challenge) {
        if ("subscribe".equals(mode) && whatsAppConfig.getWebhook().getVerifyToken().equals(token)) {
            logger.info("Webhook verified successfully!");
            return challenge;
        }
        logger.warn("Webhook verification failed. Mode: {}, Token: {}", mode, token);
        return null;
    }

    @Override
    public void sendWelcomeMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = List.of(
                createButton("welcome_hi", "Hi 👋"),
                createButton("welcome_info", "Tell me more")
        );

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("button")
                        .header(createHeader("👋 Welcome to WebStore"))
                        .body(createBody("Thanks for joining us! What would you like to do next?"))
                        .footer(createFooter("Choose an option below"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder().buttons(buttons).build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Welcome message");
    }

    @Override
    public void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        List<String> categories = categoryRepository.findTop3CategoryNames();
        logger.info("Fetched categories: {}", categories);

        if (categories.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No categories available at the moment.");
            return;
        }

        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            buttons.add(createButton("cat_" + (i + 1), categories.get(i)));
        }

        if (categoryRepository.count() > 3) {
            buttons.add(createButton("cat_see_all", "See all options"));
        }

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("button")
                        .header(createHeader("WebStore Available Categories"))
                        .body(createBody("Hi, here are your categories:\n" +
                                categories.stream().map(cat -> "- " + cat).collect(Collectors.joining("\n"))))
                        .footer(createFooter("Select a category"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder().buttons(buttons).build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Category message");
    }

    @Override
    public void sendProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);
        if (categoryId == null) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Category not found: " + categoryName);
            return;
        }

        // Get products using existing repository method
        List<String> productNames = productRepository.findProductNamesByCategoryId(categoryId);
        logger.info("Fetched products for category {}: {}", categoryName, productNames);

        if (productNames.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No products found in " + categoryName + " category.");
            return;
        }

        // Build product list with prices (limit to 3 for WhatsApp buttons)
        StringBuilder productListText = new StringBuilder();
        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = new ArrayList<>();

        int productCount = Math.min(productNames.size(), 3);
        for (int i = 0; i < productCount; i++) {
            String productName = productNames.get(i);
            Integer productId = productRepository.findProductIdByProductName(productName);

            if (productId != null) {
                // Get pricing using existing ProductPriceRepository
                List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
                String priceDisplay = "Price not available";

                if (!productPrices.isEmpty()) {
                    // Filter for INR currency or use first available
                    ProductPrice inrPrice = productPrices.stream()
                            .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                            .findFirst()
                            .orElse(productPrices.get(0));

                    // Format price using existing logic pattern
                    BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
                    priceDisplay = String.format("%s %.2f", inrPrice.getCurrency().getCurrencySymbol(), priceInRupees);
                }

                // Build product display text
                productListText.append(String.format("%d. %s\n   💰 %s\n\n", i + 1, productName, priceDisplay));

                // Create button for this product
                String buttonTitle = productName.length() > 20 ? productName.substring(0, 17) + "..." : productName;
                buttons.add(createButton("view_product_" + productId, buttonTitle));
            }
        }

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("button")
                        .header(createHeader("🛍️ " + categoryName + " Products"))
                        .body(createBody("💰 Here are the products with prices:\n\n" + productListText.toString()))
                        .footer(createFooter("Click to view product details"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder().buttons(buttons).build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Products with prices message");
    }

    @Override
    public void sendOneProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productRepository.findProductIdByProductName(productName);
            if (productId == null) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "Product not found: " + productName);
                return;
            }

            ProductResponseDto product = productService.getProductById(productId);

            // Get pricing information to display in product details
            List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
            String priceInfo = "Price not available";
            if (!productPrices.isEmpty()) {
                ProductPrice inrPrice = productPrices.stream()
                        .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                        .findFirst()
                        .orElse(productPrices.get(0));

                BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
                priceInfo = String.format("%s %.2f", inrPrice.getCurrency().getCurrencySymbol(), priceInRupees);
            }

            String productDetails = String.format("📦 *%s*\n\n📝 Description: %s\n🏷️ Category: %s\n💰 Price: %s\n\nReady to purchase?",
                    product.getProductName(),
                    product.getProductDescription() != null ? product.getProductDescription() : "No description available",
                    product.getCategory() != null ? product.getCategory().getCategoryName() : "Unknown",
                    priceInfo);

            // ENHANCED: Add both "Add to Cart" and "Checkout" buttons
            List<WhatsAppInteractiveMessageRequestDto.Button> buttons = List.of(
                    createButton("add_cart_" + productId, "🛒 Add to Cart"),
                    createButton("checkout_" + productId, "💳 Checkout"),
                    createButton("back_to_products", "⬅️ Back")
            );

            WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                    .to(recipientPhoneNumber)
                    .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                            .type("button")
                            .header(createHeader("Product Details"))
                            .body(createBody(productDetails))
                            .footer(createFooter("Choose your next action"))
                            .action(WhatsAppInteractiveMessageRequestDto.Action.builder().buttons(buttons).build())
                            .build())
                    .build();

            sendInteractiveMessage(version, phoneNumberId, requestBody, "Product details message");

        } catch (Exception e) {
            logger.error("Failed to send product details: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Sorry, there was an error retrieving product details.");
        }
    }

    @Override
    public void showProductPriceInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productRepository.findProductIdByProductName(productName);
            if (productId == null) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "Product not found: " + productName);
                return;
            }

            List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
            if (productPrices.isEmpty()) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "No pricing information available for " + productName);
                return;
            }

            ProductPrice inrPrice = productPrices.stream()
                    .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                    .findFirst()
                    .orElse(productPrices.get(0));

            BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
            String priceDetails = String.format("💰 *Price Information*\n\n📦 Product: %s\n💵 Price: %s %.2f\n💱 Currency: %s\n\nWould you like to add this to your cart?",
                    productName, inrPrice.getCurrency().getCurrencySymbol(), priceInRupees, inrPrice.getCurrency().getCurrencyCode());

            List<WhatsAppInteractiveMessageRequestDto.Button> buttons = List.of(
                    createButton("add_cart_" + productId, "Add to Cart"),
                    createButton("back_to_product_" + productId, "Back to Product")
            );

            WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                    .to(recipientPhoneNumber)
                    .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                            .type("button")
                            .header(createHeader("💰 Pricing Details"))
                            .body(createBody(priceDetails))
                            .action(WhatsAppInteractiveMessageRequestDto.Action.builder().buttons(buttons).build())
                            .build())
                    .build();

            sendInteractiveMessage(version, phoneNumberId, requestBody, "Product price message");

        } catch (Exception e) {
            logger.error("Failed to send product price: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Sorry, there was an error retrieving price information.");
        }
    }

    @Override
    public void handleCategorySelection(String phoneNumberId, String from, String categoryId) {
        String categoryIdStr = categoryId.replace("cat_", "");

        if ("see_all".equals(categoryIdStr)) {
            sendTextMessage(phoneNumberId, from, "Here are all available categories:");
            return;
        }

        try {
            int categoryNumber = Integer.parseInt(categoryIdStr);
            List<String> categories = categoryRepository.findTop3CategoryNames();

            if (categoryNumber > 0 && categoryNumber <= categories.size()) {
                String selectedCategory = categories.get(categoryNumber - 1);
                sendTextMessage(phoneNumberId, from, "You selected: " + selectedCategory);
                sendProductInteractiveMessage("v22.0", phoneNumberId, from, selectedCategory);
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing category ID: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.");
        }
    }

    @Override
    public void handleProductSelection(String phoneNumberId, String from, String productId) {
        String[] parts = productId.split("_");
        if (parts.length >= 3) {
            try {
                int productIndex = Integer.parseInt(parts[1]) - 1;
                String categoryName = parts[2];

                Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);
                if (categoryId == null) {
                    sendTextMessage(phoneNumberId, from, "Category not found: " + categoryName);
                    return;
                }

                List<String> products = productRepository.findProductNamesByCategoryId(categoryId);
                if (productIndex >= 0 && productIndex < products.size()) {
                    String selectedProduct = products.get(productIndex);
                    sendOneProductInteractiveMessage("v22.0", phoneNumberId, from, selectedProduct);
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing product selection: {}", e.getMessage());
                sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.");
            }
        }
    }

    //  Handle product view button clicks - Show interactive message with Add to Cart/Checkout buttons
    private void handleProductView(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("view_product_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);

            // Use existing ProductService to get product details
            ProductResponseDto product = productService.getProductById(productId);

            if (product != null) {
                // Call the interactive message method to show Add to Cart/Checkout buttons
                sendOneProductInteractiveMessage("v22.0", phoneNumberId, from, product.getProductName());
            } else {
                sendTextMessage(phoneNumberId, from, "Product not found.");
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing product ID: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your request.");
        } catch (Exception e) {
            logger.error("Error retrieving product details: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error retrieving product details.");
        }
    }

    // Private helper methods
    private boolean isValidWebhookData(WhatsAppWebhookRequestDto webhookData) {
        return webhookData.getEntry() != null && !webhookData.getEntry().isEmpty() &&
                webhookData.getEntry().get(0).getChanges() != null && !webhookData.getEntry().get(0).getChanges().isEmpty();
    }

    private WhatsAppWebhookRequestDto.Message extractMessage(WhatsAppWebhookRequestDto webhookData) {
        WhatsAppWebhookRequestDto.Value value = webhookData.getEntry().get(0).getChanges().get(0).getValue();
        return (value.getMessages() != null && !value.getMessages().isEmpty()) ? value.getMessages().get(0) : null;
    }

    private void processTextMessage(String phoneNumberId, String from, String messageText) {
        if (messageText.equalsIgnoreCase("categories") || messageText.equalsIgnoreCase("menu") || messageText.equalsIgnoreCase("start")) {
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        } else {
            sendTextMessage(phoneNumberId, from, "Echo: " + messageText);
        }
    }

    private void processInteractiveMessage(String phoneNumberId, String from, WhatsAppWebhookRequestDto.Interactive interactive) {
        if (!"button_reply".equals(interactive.getType())) return;

        String buttonId = interactive.getButtonReply().getId();

        if (buttonId.startsWith("cat_")) {
            handleCategorySelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("prod_")) {
            handleProductSelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("view_product_")) {
            // NEW: Handle product view button clicks (simple display only)
            handleProductView(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("price_")) {
            handlePriceSelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("add_cart_")) {
            handleAddToCart(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("checkout_")) {
            // NEW: Handle Checkout button selection
            handleCheckout(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("back_to_product_")) {
            handleBackToProduct(phoneNumberId, from, buttonId);
        } else if ("back_to_products".equals(buttonId)) {
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        } else if (buttonId.startsWith("welcome_")) {
            handleWelcomeButtons(phoneNumberId, from, buttonId);
        } else {
            sendTextMessage(phoneNumberId, from, "I didn't understand that selection. Please try again.");
        }
    }

    private void handlePriceSelection(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("price_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                showProductPriceInteractiveMessage("v22.0", phoneNumberId, from, product.getProductName());
            }
        } catch (Exception e) {
            logger.error("Error handling price selection: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your request.");
        }
    }

    private void handleAddToCart(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("add_cart_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                String echoMessage = String.format("🛒 *Button Selected: Add to Cart*\n\n📦 Product: %s\n\n✅ You selected to add this item to your cart!\n\nType 'categories' to continue shopping.",
                        product.getProductName());
                sendTextMessage(phoneNumberId, from, echoMessage);
            }
        } catch (Exception e) {
            logger.error("Error handling add to cart echo: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error processing your Add to Cart selection.");
        }
    }

    private void handleCheckout(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("checkout_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                String echoMessage = String.format("💳 *Button Selected: Checkout*\n\n📦 Product: %s\n\n✅ You selected to checkout with this item!\n\nType 'categories' to continue shopping.",
                        product.getProductName());
                sendTextMessage(phoneNumberId, from, echoMessage);
            }
        } catch (Exception e) {
            logger.error("Error handling checkout echo: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error processing your Checkout selection.");
        }
    }

    private void handleBackToProduct(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("back_to_product_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                sendOneProductInteractiveMessage("v22.0", phoneNumberId, from, product.getProductName());
            }
        } catch (Exception e) {
            logger.error("Error handling back to product: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your request.");
        }
    }

    private void handleWelcomeButtons(String phoneNumberId, String from, String buttonId) {
        if ("welcome_hi".equals(buttonId)) {
            sendTextMessage(phoneNumberId, from, "Hi there! 👋 Welcome to our store!");
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        } else if ("welcome_info".equals(buttonId)) {
            String infoMessage = "🏪 *About Our Store*\n\nWe offer a wide variety of products including:\n• Electronics\n• Clothing\n• Home Decor\n• And much more!\n\nLet me show you our categories!";
            sendTextMessage(phoneNumberId, from, infoMessage);
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        }
    }

    private void sendTextMessage(String phoneNumberId, String to, String messageText) {
        String url = String.format("%s/%s/%s/messages", whatsAppConfig.getApi().getGraphUrl(), whatsAppConfig.getApi().getVersion(), phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        WhatsAppMessageRequestDto requestBody = WhatsAppMessageRequestDto.builder()
                .messaging_product("whatsapp")
                .to(to)
                .text(new WhatsAppMessageRequestDto.TextBody(messageText))
                .build();

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(requestBody, headers), String.class);
            logger.info("Text message sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error sending text message: {}", e.getMessage(), e);
        }
    }

    private void sendInteractiveMessage(String version, String phoneNumberId, WhatsAppInteractiveMessageRequestDto requestBody, String messageType) {
        String url = String.format("%s/%s/%s/messages", whatsAppConfig.getApi().getGraphUrl(), version, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsAppConfig.getApi().getAccessToken());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(requestBody, headers), String.class);
            logger.info("{} sent successfully: {}", messageType, response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send {}: {}", messageType, e.getMessage(), e);
        }
    }

    private WhatsAppInteractiveMessageRequestDto.Button createButton(String id, String title) {
        return WhatsAppInteractiveMessageRequestDto.Button.builder()
                .type("reply")
                .reply(WhatsAppInteractiveMessageRequestDto.Reply.builder().id(id).title(title).build())
                .build();
    }

    private WhatsAppInteractiveMessageRequestDto.Header createHeader(String text) {
        return WhatsAppInteractiveMessageRequestDto.Header.builder().type("text").text(text).build();
    }

    private WhatsAppInteractiveMessageRequestDto.Body createBody(String text) {
        return WhatsAppInteractiveMessageRequestDto.Body.builder().text(text).build();
    }

    private WhatsAppInteractiveMessageRequestDto.Footer createFooter(String text) {
        return WhatsAppInteractiveMessageRequestDto.Footer.builder().text(text).build();
    }
}