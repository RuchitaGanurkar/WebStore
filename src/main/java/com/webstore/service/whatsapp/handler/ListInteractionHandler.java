package com.webstore.service.whatsapp.handler;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.flow.NavigationService;
import com.webstore.service.whatsapp.flow.ProductFlowService;  // ✅ ADDED: Missing import
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListInteractionHandler implements InteractionHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(ListInteractionHandler.class);

    private final CategoryBusinessService categoryService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final CategoryFlowService categoryFlowService;
    private final NavigationService navigationService;
    private final ProductFlowService productFlowService;  // ✅ ADDED: Missing field
    private final MessageFormatter formatter;
    private final PaginationUtil paginationUtil;

    // ✅ FIXED: Added ProductFlowService to constructor
    public ListInteractionHandler(CategoryBusinessService categoryService,
                                  ProductBusinessService productService,
                                  WhatsAppMessageSender messageSender,
                                  CategoryFlowService categoryFlowService,
                                  NavigationService navigationService,
                                  ProductFlowService productFlowService,  // ✅ ADDED: Missing parameter
                                  MessageFormatter formatter,
                                  PaginationUtil paginationUtil) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.messageSender = messageSender;
        this.categoryFlowService = categoryFlowService;
        this.navigationService = navigationService;
        this.productFlowService = productFlowService;  // ✅ ADDED: Missing assignment
        this.formatter = formatter;
        this.paginationUtil = paginationUtil;
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        logger.info("=== LIST SELECTION DEBUG ===");
        logger.info("Handling list selection: {}", listId);

        if (listId.startsWith("cat_page_")) {
            handlePaginatedCategorySelection(phoneNumberId, from, listId);
        } else if (listId.startsWith("prod_p")) {
            handlePaginatedProductSelection(phoneNumberId, from, listId);
        } else if (listId.startsWith("next_cat_page_") || listId.startsWith("prev_cat_page_")) {
            navigationService.handleCategoryPageNavigation(phoneNumberId, from, listId);
        } else if (listId.startsWith("next_prod_p") || listId.startsWith("prev_prod_p")) {
            navigationService.handleProductPageNavigation(phoneNumberId, from, listId);
        } else if ("back_to_categories".equals(listId)) {
            categoryFlowService.sendCategorySelection("v22.0", phoneNumberId, from);
        } else {
            logger.warn("Unrecognized list selection: {}", listId);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "I didn't understand that selection. Please try again.");
        }
    }

    // ✅ CRITICAL FIX: This method was causing the infinite category loop
    private void handlePaginatedCategorySelection(String phoneNumberId, String from, String listId) {
        logger.info("=== CATEGORY SELECTION DEBUG ===");
        logger.info("Processing category selection: {}", listId);

        try {
            String[] parts = listId.split("_");
            logger.info("Split parts: {}", java.util.Arrays.toString(parts));

            if (parts.length >= 5) {
                int pageNumber = Integer.parseInt(parts[2]);
                int itemNumber = Integer.parseInt(parts[4]);
                logger.info("Parsed - Page: {}, Item: {}", pageNumber, itemNumber);

                List<String> allCategories = categoryService.getAllCategoryNames();
                logger.info("All categories: {}", allCategories);

                int itemsPerPage = 7;
                int startIndex = (pageNumber - 1) * itemsPerPage;
                int actualIndex = startIndex + (itemNumber - startIndex - 1);
                logger.info("Calculated actualIndex: {}", actualIndex);

                if (actualIndex >= 0 && actualIndex < allCategories.size()) {
                    String selectedCategory = allCategories.get(actualIndex);
                    logger.info("✅ Selected category: {}", selectedCategory);

                    messageSender.sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        logger.error("Sleep interrupted", e);
                    }

                    // ✅ CRITICAL FIX: Call ProductFlowService instead of CategoryFlowService!
                    logger.info("🔄 Calling ProductFlowService to show products for category: {}", selectedCategory);
                    productFlowService.sendProductSelection("v22.0", phoneNumberId, from, selectedCategory);
                    logger.info("✅ ProductFlowService call completed");

                } else {
                    logger.error("❌ ActualIndex {} out of bounds. Category count: {}", actualIndex, allCategories.size());
                    messageSender.sendTextMessage(phoneNumberId, from,
                            "Sorry, there was a problem processing your selection.");
                }
            } else {
                logger.error("❌ Invalid listId format. Expected 5+ parts, got: {}", parts.length);
                messageSender.sendTextMessage(phoneNumberId, from,
                        "Sorry, there was a problem processing your selection.");
            }
        } catch (Exception e) {
            logger.error("❌ Error in handlePaginatedCategorySelection: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was a problem processing your selection.");
        }
    }

    private void handlePaginatedProductSelection(String phoneNumberId, String from, String listId) {
        logger.info("=== PRODUCT SELECTION DEBUG ===");
        logger.info("Handling paginated product selection: {}", listId);

        try {
            if (listId.startsWith("prod_p")) {
                String[] parts = listId.split("_");
                logger.info("Split parts: {}", java.util.Arrays.toString(parts));

                if (parts.length >= 4) {
                    int pageNumber = Integer.parseInt(parts[1].substring(1)); // Remove 'p' prefix
                    int productId = Integer.parseInt(parts[2].substring(1)); // Remove 'i' prefix
                    String encodedCategory = parts[3].substring(1); // Remove 'c' prefix
                    String categoryName = paginationUtil.decodeFromBase64(encodedCategory);

                    logger.info("Parsed - Page: {}, ProductId: {}, Category: {}", pageNumber, productId, categoryName);

                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        String priceDisplay = productService.getProductPriceDisplay(productId);
                        String addToCartMessage = formatter.formatAddToCartMessage(
                                product.getProductName(), priceDisplay, categoryName);
                        messageSender.sendTextMessage(phoneNumberId, from, addToCartMessage);
                    } else {
                        logger.error("Product not found for ID: {}", productId);
                        messageSender.sendTextMessage(phoneNumberId, from, "Product not found. Please try again.");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling paginated product selection: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was an error adding the product to cart.");
        }
    }
}