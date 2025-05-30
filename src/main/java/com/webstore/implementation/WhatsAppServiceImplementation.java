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

    // UTILITY METHODS FOR TEXT TRUNCATION
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String truncateSectionTitle(String title) {
        return truncateText(title, 24); // WhatsApp limit: 24 characters
    }

    private String truncateRowTitle(String title) {
        return truncateText(title, 24); // WhatsApp limit: 24 characters
    }

    private String truncateRowDescription(String description) {
        return truncateText(description, 72); // WhatsApp limit: 72 characters
    }

    // WEBHOOK PROCESSING
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
        // Handle interactive messages (both buttons and lists)
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

    // WELCOME MESSAGE
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
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                .buttons(buttons)
                                .build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Welcome message");
    }

    // CATEGORY SELECTION - Auto-chooses Button vs List based on count
    @Override
    public void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        long totalCategories = categoryRepository.count();
        logger.info("Total categories in database: {}", totalCategories);

        if (totalCategories <= 3) {
            // Use buttons for 1-3 categories
            sendCategoryButtonMessage(version, phoneNumberId, recipientPhoneNumber);
        } else {
            // Use paginated list for 4+ categories
            sendCategoryListMessage(version, phoneNumberId, recipientPhoneNumber);
        }
    }

    // BUTTON CATEGORIES (1-3 categories) - Handle long category names
    private void sendCategoryButtonMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        List<String> categories = categoryRepository.findTop3CategoryNames();
        logger.info("Fetched top 3 categories: {}", categories);

        if (categories.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No categories available at the moment.");
            return;
        }

        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            String buttonTitle = truncateText(categories.get(i), 20);
            buttons.add(createButton("cat_" + (i + 1), buttonTitle));
        }

        if (categoryRepository.count() > 3) {
            buttons.add(createButton("cat_see_all", "See all options"));
        }

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("button")
                        .header(createHeader("🏪 WebStore Categories"))
                        .body(createBody("Choose a category to explore:\n" +
                                categories.stream()
                                        .map(cat -> "• " + truncateText(cat, 30))
                                        .collect(Collectors.joining("\n"))))
                        .footer(createFooter("Select any category"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                .buttons(buttons)
                                .build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Category buttons message");
    }

    // ENHANCED CATEGORY LIST MESSAGE WITH PAGINATION (4+ categories)
    private void sendCategoryListMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        sendCategoryListMessageWithPage(version, phoneNumberId, recipientPhoneNumber, 1);
    }

    private void sendCategoryListMessageWithPage(String version, String phoneNumberId, String recipientPhoneNumber, int pageNumber) {
        List<String> allCategories = getAllCategoryNames();
        logger.info("Fetched all categories for pagination: {} categories, page: {}", allCategories.size(), pageNumber);

        if (allCategories.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No categories available at the moment.");
            return;
        }

        // Pagination logic
        int itemsPerPage = 7;
        int totalPages = (int) Math.ceil((double) allCategories.size() / itemsPerPage);
        int startIndex = (pageNumber - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allCategories.size());

        // Validate page number
        if (pageNumber < 1 || pageNumber > totalPages) {
            pageNumber = 1;
            startIndex = 0;
            endIndex = Math.min(itemsPerPage, allCategories.size());
        }

        List<String> pageCategories = allCategories.subList(startIndex, endIndex);
        List<WhatsAppInteractiveMessageRequestDto.Row> rows = new ArrayList<>();

        // Add category items
        for (int i = 0; i < pageCategories.size(); i++) {
            String categoryName = pageCategories.get(i);
            Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);

            int productCount = 0;
            if (categoryId != null) {
                productCount = productRepository.findProductNamesByCategoryId(categoryId).size();
            }

            String rowTitle = truncateRowTitle(categoryName);
            String rowDescription = truncateRowDescription(String.format("%d products available", productCount));

            rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                    .id(String.format("cat_page_%d_item_%d", pageNumber, startIndex + i + 1))
                    .title(rowTitle)
                    .description("🔢 " + rowDescription)
                    .build());
        }

        // Add navigation options
        if (totalPages > 1) {
            // Previous page option
            if (pageNumber > 1) {
                rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                        .id("prev_cat_page_" + (pageNumber - 1))
                        .title("⬅️ Previous Page")
                        .description(String.format("Go to page %d", pageNumber - 1))
                        .build());
            }

            // Next page option
            if (pageNumber < totalPages) {
                rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                        .id("next_cat_page_" + (pageNumber + 1))
                        .title("➡️ Next Page")
                        .description(String.format("Go to page %d", pageNumber + 1))
                        .build());
            }
        }

        String sectionTitle = truncateSectionTitle("🏪 Categories");

        WhatsAppInteractiveMessageRequestDto.Section section = WhatsAppInteractiveMessageRequestDto.Section.builder()
                .title(sectionTitle)
                .rows(rows)
                .build();

        String bodyText = String.format("📄 Page %d of %d (%d total categories)\n\nChoose a category to explore:",
                pageNumber, totalPages, allCategories.size());

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("list")
                        .header(createHeader("🛍️ WebStore"))
                        .body(createBody(bodyText))
                        .footer(createFooter("Tap to browse products"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                .button("Browse Categories")
                                .sections(List.of(section))
                                .build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Category list message with pagination");
    }

    // PRODUCT SELECTION - Auto-chooses Button vs List based on count
    @Override
    public void sendProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);
        if (categoryId == null) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Category not found: " + categoryName);
            return;
        }

        List<String> productNames = productRepository.findProductNamesByCategoryId(categoryId);
        logger.info("Fetched products for category {}: {}", categoryName, productNames);

        if (productNames.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No products found in " + categoryName + " category.");
            return;
        }

        if (productNames.size() <= 3) {
            sendProductButtonMessage(version, phoneNumberId, recipientPhoneNumber, categoryName, productNames);
        } else {
            sendProductListMessage(version, phoneNumberId, recipientPhoneNumber, categoryName);
        }
    }

    // PRODUCT BUTTONS (1-3 products) - Handle long product names
    private void sendProductButtonMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName, List<String> productNames) {
        StringBuilder productListText = new StringBuilder();
        List<WhatsAppInteractiveMessageRequestDto.Button> buttons = new ArrayList<>();

        int productCount = Math.min(productNames.size(), 3);
        for (int i = 0; i < productCount; i++) {
            String productName = productNames.get(i);
            Integer productId = productRepository.findProductIdByProductName(productName);

            if (productId != null) {
                String priceDisplay = getProductPriceDisplay(productId);

                String displayName = truncateText(productName, 30);
                productListText.append(String.format("%d. %s\n   💰 %s\n\n", i + 1, displayName, priceDisplay));

                String buttonTitle = truncateText(productName, 17) + "...";
                if (productName.length() <= 17) {
                    buttonTitle = productName;
                }
                buttons.add(createButton("view_product_" + productId, buttonTitle));
            }
        }

        String headerText = truncateText("🛍️ " + categoryName + " Products", 60);

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("button")
                        .header(createHeader(headerText))
                        .body(createBody("💰 Here are the products with prices:\n\n" + productListText.toString()))
                        .footer(createFooter("Tap to view details & add to cart"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                .buttons(buttons)
                                .build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Product buttons message");
    }

    // FIXED: Enhanced Product List Message with Pagination (4+ products)
    private void sendProductListMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        sendProductListMessageWithPage(version, phoneNumberId, recipientPhoneNumber, categoryName, 1);
    }

    private void sendProductListMessageWithPage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName, int pageNumber) {
        logger.info("=== PRODUCT PAGINATION DEBUG ===");
        logger.info("Category Name: {}", categoryName);
        logger.info("Requested Page: {}", pageNumber);

        Integer categoryId = categoryRepository.findCategoryIdByCategoryName(categoryName);
        if (categoryId == null) {
            logger.error("Category ID not found for category: {}", categoryName);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Category not found: " + categoryName);
            return;
        }

        logger.info("Found Category ID: {}", categoryId);

        List<String> allProducts = productRepository.findProductNamesByCategoryId(categoryId);
        logger.info("Total products fetched from database: {}", allProducts.size());
        logger.info("All products: {}", allProducts);

        if (allProducts.isEmpty()) {
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "No products found in " + categoryName + " category.");
            return;
        }

        // Pagination logic
        int itemsPerPage = 7;
        int totalPages = (int) Math.ceil((double) allProducts.size() / itemsPerPage);
        int startIndex = (pageNumber - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allProducts.size());

        logger.info("Pagination: totalPages={}, startIndex={}, endIndex={}", totalPages, startIndex, endIndex);

        // Validate page number
        if (pageNumber < 1 || pageNumber > totalPages) {
            logger.warn("Invalid page number {}. Resetting to page 1", pageNumber);
            pageNumber = 1;
            startIndex = 0;
            endIndex = Math.min(itemsPerPage, allProducts.size());
        }

        List<String> pageProducts = allProducts.subList(startIndex, endIndex);
        logger.info("Products for page {}: {}", pageNumber, pageProducts);

        List<WhatsAppInteractiveMessageRequestDto.Row> rows = new ArrayList<>();

        // Add product items - FIXED: Simplified ID structure
        for (int i = 0; i < pageProducts.size(); i++) {
            String productName = pageProducts.get(i);
            Integer productId = productRepository.findProductIdByProductName(productName);

            if (productId != null) {
                String priceDisplay = getProductPriceDisplay(productId);
                String rowTitle = truncateRowTitle(productName);
                String rowDescription = truncateRowDescription(priceDisplay + " • Tap to add to cart");

                // FIXED: Simpler ID structure - encode category name as Base64 to avoid parsing issues
                String encodedCategoryName = java.util.Base64.getEncoder().encodeToString(categoryName.getBytes());
                String rowId = String.format("prod_p%d_i%d_c%s", pageNumber, productId, encodedCategoryName);

                logger.info("Creating product row: ID={}, Title={}, ProductId={}", rowId, rowTitle, productId);

                rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                        .id(rowId)
                        .title(rowTitle)
                        .description("💰 " + rowDescription)
                        .build());
            } else {
                logger.warn("Product ID not found for product: {}", productName);
            }
        }

        // Add navigation options - FIXED: ID structure
        if (totalPages > 1) {
            String encodedCategoryName = java.util.Base64.getEncoder().encodeToString(categoryName.getBytes());

            // Previous page option
            if (pageNumber > 1) {
                String prevId = String.format("prev_prod_p%d_c%s", pageNumber - 1, encodedCategoryName);
                logger.info("Adding previous page button: {}", prevId);

                rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                        .id(prevId)
                        .title("⬅️ Previous Page")
                        .description(String.format("Go to page %d", pageNumber - 1))
                        .build());
            }

            // Next page option
            if (pageNumber < totalPages) {
                String nextId = String.format("next_prod_p%d_c%s", pageNumber + 1, encodedCategoryName);
                logger.info("Adding next page button: {}", nextId);

                rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                        .id(nextId)
                        .title("➡️ Next Page")
                        .description(String.format("Go to page %d", pageNumber + 1))
                        .build());
            }
        }

        // Back to categories option
        rows.add(WhatsAppInteractiveMessageRequestDto.Row.builder()
                .id("back_to_categories")
                .title("⬅️ Back to Categories")
                .description("Browse other product categories")
                .build());

        String shortCategoryName = categoryName.length() > 15 ? categoryName.substring(0, 15) : categoryName;
        String sectionTitle = truncateSectionTitle("🛒 " + shortCategoryName);

        WhatsAppInteractiveMessageRequestDto.Section section = WhatsAppInteractiveMessageRequestDto.Section.builder()
                .title(sectionTitle)
                .rows(rows)
                .build();

        String headerText = truncateText("🛍️ Shop " + categoryName, 60);
        String bodyText = String.format("📄 Page %d of %d (%d total products)\n\nChoose a product to add to cart:",
                pageNumber, totalPages, allProducts.size());

        logger.info("Sending product list with {} rows", rows.size());

        WhatsAppInteractiveMessageRequestDto requestBody = WhatsAppInteractiveMessageRequestDto.builder()
                .to(recipientPhoneNumber)
                .interactive(WhatsAppInteractiveMessageRequestDto.Interactive.builder()
                        .type("list")
                        .header(createHeader(headerText))
                        .body(createBody(bodyText))
                        .footer(createFooter("💡 Select to add to cart"))
                        .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                .button("View Products")
                                .sections(List.of(section))
                                .build())
                        .build())
                .build();

        sendInteractiveMessage(version, phoneNumberId, requestBody, "Product list message with pagination");
    }

    // SINGLE PRODUCT DETAILS
    @Override
    public void sendOneProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productRepository.findProductIdByProductName(productName);
            if (productId == null) {
                sendTextMessage(phoneNumberId, recipientPhoneNumber, "Product not found: " + productName);
                return;
            }

            ProductResponseDto product = productService.getProductById(productId);
            String priceInfo = getProductPriceDisplay(productId);

            String productDetails = String.format("📦 *%s*\n\n📝 Description: %s\n🏷️ Category: %s\n💰 Price: %s\n\nReady to purchase?",
                    product.getProductName(),
                    product.getProductDescription() != null ? product.getProductDescription() : "No description available",
                    product.getCategory() != null ? product.getCategory().getCategoryName() : "Unknown",
                    priceInfo);

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
                            .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                    .buttons(buttons)
                                    .build())
                            .build())
                    .build();

            sendInteractiveMessage(version, phoneNumberId, requestBody, "Product details message");

        } catch (Exception e) {
            logger.error("Failed to send product details: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Sorry, there was an error retrieving product details.");
        }
    }

    // PRODUCT PRICE DISPLAY
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
                            .action(WhatsAppInteractiveMessageRequestDto.Action.builder()
                                    .buttons(buttons)
                                    .build())
                            .build())
                    .build();

            sendInteractiveMessage(version, phoneNumberId, requestBody, "Product price message");

        } catch (Exception e) {
            logger.error("Failed to send product price: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, recipientPhoneNumber, "Sorry, there was an error retrieving price information.");
        }
    }

    // Get formatted price display
    private String getProductPriceDisplay(Integer productId) {
        List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
        if (productPrices.isEmpty()) {
            return "Price not available";
        }

        ProductPrice inrPrice = productPrices.stream()
                .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                .findFirst()
                .orElse(productPrices.get(0));

        BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
        return String.format("%s %.2f", inrPrice.getCurrency().getCurrencySymbol(), priceInRupees);
    }

    // Get all category names
    private List<String> getAllCategoryNames() {
        try {
            return categoryRepository.findAllCategoryNames();
        } catch (Exception e) {
            logger.error("Error fetching all category names, falling back to top 3: {}", e.getMessage());
            return categoryRepository.findTop3CategoryNames();
        }
    }

    // INTERACTIVE MESSAGE PROCESSING - Handle both button and list interactions
    private void processInteractiveMessage(String phoneNumberId, String from, WhatsAppWebhookRequestDto.Interactive interactive) {
        logger.info("Processing interactive message type: {}", interactive.getType());

        if ("button_reply".equals(interactive.getType())) {
            // Handle button clicks
            String buttonId = interactive.getButtonReply().getId();
            logger.info("Button clicked: {}", buttonId);
            handleButtonClick(phoneNumberId, from, buttonId);

        } else if ("list_reply".equals(interactive.getType())) {
            // Handle list selections
            String listId = interactive.getListReply().getId();
            logger.info("List item selected: {}", listId);
            handleListSelection(phoneNumberId, from, listId);
        }
    }

    // BUTTON CLICK HANDLER (existing logic)
    private void handleButtonClick(String phoneNumberId, String from, String buttonId) {
        if (buttonId.startsWith("cat_")) {
            handleCategorySelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("prod_")) {
            handleProductSelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("view_product_")) {
            handleProductView(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("price_")) {
            handlePriceSelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("add_cart_")) {
            handleAddToCart(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("checkout_")) {
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

    // Enhanced List Selection Handler with Better Product Pagination Support
    private void handleListSelection(String phoneNumberId, String from, String listId) {
        logger.info("=== LIST SELECTION DEBUG ===");
        logger.info("Handling list selection: {}", listId);

        if (listId.startsWith("cat_page_")) {
            // Handle paginated category selection
            handlePaginatedCategorySelection(phoneNumberId, from, listId);
        } else if (listId.startsWith("prod_p")) {
            // Handle paginated product selection - FIXED
            handlePaginatedProductSelection(phoneNumberId, from, listId);
        } else if (listId.startsWith("next_cat_page_")) {
            // Handle next category page
            handleCategoryPageNavigation(phoneNumberId, from, listId, "next");
        } else if (listId.startsWith("prev_cat_page_")) {
            // Handle previous category page
            handleCategoryPageNavigation(phoneNumberId, from, listId, "prev");
        } else if (listId.startsWith("next_prod_p")) {
            // Handle next product page - FIXED
            handleProductPageNavigation(phoneNumberId, from, listId, "next");
        } else if (listId.startsWith("prev_prod_p")) {
            // Handle previous product page - FIXED
            handleProductPageNavigation(phoneNumberId, from, listId, "prev");
        } else if (listId.startsWith("list_cat_")) {
            // Handle legacy category selection (for backward compatibility)
            handleCategoryListSelection(phoneNumberId, from, listId);
        } else if (listId.startsWith("list_product_")) {
            // Handle legacy product selection (for backward compatibility)
            handleProductListSelection(phoneNumberId, from, listId);
        } else if ("back_to_categories".equals(listId)) {
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        } else {
            logger.warn("Unrecognized list selection: {}", listId);
            sendTextMessage(phoneNumberId, from, "I didn't understand that selection. Please try again.");
        }
    }

    // PAGINATION HANDLERS

    // Handle paginated category selection
    private void handlePaginatedCategorySelection(String phoneNumberId, String from, String listId) {
        try {
            // Parse: cat_page_1_item_3
            String[] parts = listId.split("_");
            if (parts.length >= 5) {
                int pageNumber = Integer.parseInt(parts[2]);
                int itemNumber = Integer.parseInt(parts[4]);

                List<String> allCategories = getAllCategoryNames();
                int itemsPerPage = 7;
                int startIndex = (pageNumber - 1) * itemsPerPage;
                int actualIndex = startIndex + (itemNumber - startIndex - 1);

                if (actualIndex >= 0 && actualIndex < allCategories.size()) {
                    String selectedCategory = allCategories.get(actualIndex);
                    sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);

                    // Small delay then show products
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        logger.error("Sleep interrupted", e);
                    }

                    sendProductInteractiveMessage("v22.0", phoneNumberId, from, selectedCategory);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing paginated category selection: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.");
        }
    }

    // FIXED: Handle paginated product selection with better parsing
    private void handlePaginatedProductSelection(String phoneNumberId, String from, String listId) {
        logger.info("=== PRODUCT SELECTION DEBUG ===");
        logger.info("Handling paginated product selection: {}", listId);

        try {
            // Parse: prod_p1_i123_cRWxlY3Ryb25pY3M= (Base64 encoded category)
            if (listId.startsWith("prod_p")) {
                String[] parts = listId.split("_");
                logger.info("Split parts: {}", java.util.Arrays.toString(parts));

                if (parts.length >= 4) {
                    // Extract page number (prod_p1_i123_cRWxlY3Ryb25pY3M=)
                    int pageNumber = Integer.parseInt(parts[1].substring(1)); // Remove 'p' prefix

                    // Extract product ID (i123)
                    int productId = Integer.parseInt(parts[2].substring(1)); // Remove 'i' prefix

                    // Extract and decode category name (cRWxlY3Ryb25pY3M=)
                    String encodedCategory = parts[3].substring(1); // Remove 'c' prefix
                    String categoryName = new String(java.util.Base64.getDecoder().decode(encodedCategory));

                    logger.info("Parsed - Page: {}, ProductId: {}, Category: {}", pageNumber, productId, categoryName);

                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        String priceDisplay = getProductPriceDisplay(productId);
                        String addToCartMessage = String.format(
                                "🛒 *Added to Cart!*\n\n" +
                                        "📦 Product: %s\n" +
                                        "💰 Price: %s\n" +
                                        "🏷️ Category: %s\n\n" +
                                        "✅ Item added successfully!\n\n" +
                                        "Type 'categories' to continue shopping or 'cart' to view your cart.",
                                product.getProductName(),
                                priceDisplay,
                                categoryName
                        );
                        sendTextMessage(phoneNumberId, from, addToCartMessage);
                    } else {
                        logger.error("Product not found for ID: {}", productId);
                        sendTextMessage(phoneNumberId, from, "Product not found. Please try again.");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling paginated product selection: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error adding the product to cart.");
        }
    }

    // Handle category page navigation
    private void handleCategoryPageNavigation(String phoneNumberId, String from, String listId, String direction) {
        try {
            String pageStr = listId.replaceAll("(next_cat_page_|prev_cat_page_)", "");
            int pageNumber = Integer.parseInt(pageStr);

            sendCategoryListMessageWithPage("v22.0", phoneNumberId, from, pageNumber);
        } catch (Exception e) {
            logger.error("Error handling category page navigation: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem with page navigation.");
        }
    }

    // FIXED: Handle product page navigation with better parsing
    private void handleProductPageNavigation(String phoneNumberId, String from, String listId, String direction) {
        logger.info("=== PRODUCT NAVIGATION DEBUG ===");
        logger.info("Handling product page navigation: {} ({})", listId, direction);

        try {
            // Parse: next_prod_p2_cRWxlY3Ryb25pY3M= or prev_prod_p1_cRWxlY3Ryb25pY3M=
            String[] parts = listId.split("_");
            logger.info("Navigation split parts: {}", java.util.Arrays.toString(parts));

            if (parts.length >= 4) {
                // Extract page number
                int pageNumber = Integer.parseInt(parts[2].substring(1)); // Remove 'p' prefix

                // Extract and decode category name
                String encodedCategory = parts[3].substring(1); // Remove 'c' prefix
                String categoryName = new String(java.util.Base64.getDecoder().decode(encodedCategory));

                logger.info("Navigation parsed - Page: {}, Category: {}", pageNumber, categoryName);

                sendProductListMessageWithPage("v22.0", phoneNumberId, from, categoryName, pageNumber);
            }
        } catch (Exception e) {
            logger.error("Error handling product page navigation: {}", e.getMessage(), e);
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem with page navigation.");
        }
    }

    // LEGACY SUPPORT - Handle category selection from list (backward compatibility)
    private void handleCategoryListSelection(String phoneNumberId, String from, String listId) {
        String categoryIdStr = listId.replace("list_cat_", "");
        try {
            int categoryNumber = Integer.parseInt(categoryIdStr);
            List<String> allCategories = getAllCategoryNames();

            if (categoryNumber > 0 && categoryNumber <= allCategories.size()) {
                String selectedCategory = allCategories.get(categoryNumber - 1);
                sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);

                // Small delay then show products
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.error("Sleep interrupted", e);
                }

                sendProductInteractiveMessage("v22.0", phoneNumberId, from, selectedCategory);
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing category list selection: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was a problem processing your selection.");
        }
    }

    // LEGACY SUPPORT - Handle product selection from list (backward compatibility)
    private void handleProductListSelection(String phoneNumberId, String from, String listId) {
        String productIdStr = listId.replace("list_product_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);

            if (product != null) {
                String priceDisplay = getProductPriceDisplay(productId);
                String addToCartMessage = String.format(
                        "🛒 *Added to Cart!*\n\n" +
                                "📦 Product: %s\n" +
                                "💰 Price: %s\n" +
                                "🏷️ Category: %s\n\n" +
                                "✅ Item added successfully!\n\n" +
                                "Type 'categories' to continue shopping or 'cart' to view your cart.",
                        product.getProductName(),
                        priceDisplay,
                        product.getCategory() != null ? product.getCategory().getCategoryName() : "Unknown"
                );

                sendTextMessage(phoneNumberId, from, addToCartMessage);
            }
        } catch (Exception e) {
            logger.error("Error handling product list selection: {}", e.getMessage());
            sendTextMessage(phoneNumberId, from, "Sorry, there was an error adding the product to cart.");
        }
    }

    // INDIVIDUAL BUTTON HANDLERS

    @Override
    public void handleCategorySelection(String phoneNumberId, String from, String categoryId) {
        String categoryIdStr = categoryId.replace("cat_", "");

        if ("see_all".equals(categoryIdStr)) {
            sendCategoryListMessage("v22.0", phoneNumberId, from);
            return;
        }

        try {
            int categoryNumber = Integer.parseInt(categoryIdStr);
            List<String> categories = categoryRepository.findTop3CategoryNames();

            if (categoryNumber > 0 && categoryNumber <= categories.size()) {
                String selectedCategory = categories.get(categoryNumber - 1);
                sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);
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

    private void handleProductView(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("view_product_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);

            if (product != null) {
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
            String infoMessage = "🏪 *WebStore is a multi-category e-commerce platform supporting agricultural products, cooked food, and other items.\nWe offer a wide variety of products including\n\nLet me show you our categories!";
            sendTextMessage(phoneNumberId, from, infoMessage);
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        }
    }

    // TEXT MESSAGE PROCESSING
    private void processTextMessage(String phoneNumberId, String from, String messageText) {
        if (messageText.equalsIgnoreCase("categories") || messageText.equalsIgnoreCase("menu") || messageText.equalsIgnoreCase("start")) {
            sendCategoryInteractiveMessage("v22.0", phoneNumberId, from);
        } else if (messageText.equalsIgnoreCase("cart")) {
            sendTextMessage(phoneNumberId, from, "🛒 Your cart feature is coming soon! For now, type 'categories' to continue shopping.");
        } else {
            sendTextMessage(phoneNumberId, from, "Echo: " + messageText + "\n\nType 'categories' to browse products!");
        }
    }

    // UTILITY METHODS FOR WEBHOOK PROCESSING
    private boolean isValidWebhookData(WhatsAppWebhookRequestDto webhookData) {
        return webhookData.getEntry() != null && !webhookData.getEntry().isEmpty() &&
                webhookData.getEntry().get(0).getChanges() != null && !webhookData.getEntry().get(0).getChanges().isEmpty();
    }

    private WhatsAppWebhookRequestDto.Message extractMessage(WhatsAppWebhookRequestDto webhookData) {
        WhatsAppWebhookRequestDto.Value value = webhookData.getEntry().get(0).getChanges().get(0).getValue();
        return (value.getMessages() != null && !value.getMessages().isEmpty()) ? value.getMessages().get(0) : null;
    }

    // CORE MESSAGING METHODS
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
            logger.error("Failed to send {}: {}", messageType, e.getMessage());
            logger.error("Request body that failed: {}", requestBody);

            // If it's a list message that failed, try sending a fallback text message
            if (messageType.contains("list")) {
                try {
                    String fallbackMessage = "🔧 **Technical Issue**\n\nSorry, there was a problem displaying the list. Please type 'categories' to try again or contact support.";
                    sendTextMessage(phoneNumberId, requestBody.getTo(), fallbackMessage);
                } catch (Exception fallbackException) {
                    logger.error("Even fallback message failed: {}", fallbackException.getMessage());
                }
            }
        }
    }

    // MESSAGE BUILDER HELPER METHODS
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