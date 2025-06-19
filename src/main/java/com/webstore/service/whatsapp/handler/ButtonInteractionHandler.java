package com.webstore.service.whatsapp.handler;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import com.webstore.util.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ButtonInteractionHandler implements InteractionHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(ButtonInteractionHandler.class);

    private final CategoryBusinessService categoryService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final CategoryFlowService categoryFlowService;
    private final ProductFlowService productFlowService;
    private final MessageFormatter formatter;

    public ButtonInteractionHandler(CategoryBusinessService categoryService,
                                    ProductBusinessService productService,
                                    WhatsAppMessageSender messageSender,
                                    CategoryFlowService categoryFlowService,
                                    ProductFlowService productFlowService,
                                    MessageFormatter formatter) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.messageSender = messageSender;
        this.categoryFlowService = categoryFlowService;
        this.productFlowService = productFlowService;
        this.formatter = formatter;
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling button click: {}", buttonId);

        if (buttonId.startsWith("cat_")) {
            handleCategorySelection(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("view_product_")) {
            handleProductView(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("add_cart_")) {
            handleAddToCart(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("checkout_")) {
            handleCheckout(phoneNumberId, from, buttonId);
        } else if (buttonId.startsWith("back_to_product_")) {
            handleBackToProduct(phoneNumberId, from, buttonId);
        } else if ("back_to_products".equals(buttonId)) {
            categoryFlowService.sendCategorySelection("v22.0", phoneNumberId, from);
        } else if (buttonId.startsWith("welcome_")) {
            handleWelcomeButtons(phoneNumberId, from, buttonId);
        } else {
            messageSender.sendTextMessage(phoneNumberId, from,
                    "I didn't understand that selection. Please try again.");
        }
    }

    private void handleCategorySelection(String phoneNumberId, String from, String categoryId) {
        String categoryIdStr = categoryId.replace("cat_", "");

        if ("see_all".equals(categoryIdStr)) {
            categoryFlowService.sendCategoryList("v22.0", phoneNumberId, from, 1);
            return;
        }

        try {
            int categoryNumber = Integer.parseInt(categoryIdStr);
            List<String> categories = categoryService.getTop3CategoryNames();

            if (categoryNumber > 0 && categoryNumber <= categories.size()) {
                String selectedCategory = categories.get(categoryNumber - 1);
                messageSender.sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);
                productFlowService.sendProductSelection("v22.0", phoneNumberId, from, selectedCategory);
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing category ID: {}", e.getMessage());
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was a problem processing your selection.");
        }
    }

    private void handleProductView(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("view_product_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);

            if (product != null) {
                productFlowService.sendProductDetails("v22.0", phoneNumberId, from, product.getProductName());
            } else {
                messageSender.sendTextMessage(phoneNumberId, from, "Product not found.");
            }
        } catch (Exception e) {
            logger.error("Error parsing product ID: {}", e.getMessage());
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was a problem processing your request.");
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
                messageSender.sendTextMessage(phoneNumberId, from, echoMessage);
            }
        } catch (Exception e) {
            logger.error("Error handling add to cart echo: {}", e.getMessage());
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was an error processing your Add to Cart selection.");
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
                messageSender.sendTextMessage(phoneNumberId, from, echoMessage);
            }
        } catch (Exception e) {
            logger.error("Error handling checkout echo: {}", e.getMessage());
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was an error processing your Checkout selection.");
        }
    }

    private void handleBackToProduct(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("back_to_product_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                productFlowService.sendProductDetails("v22.0", phoneNumberId, from, product.getProductName());
            }
        } catch (Exception e) {
            logger.error("Error handling back to product: {}", e.getMessage());
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was a problem processing your request.");
        }
    }

    private void handleWelcomeButtons(String phoneNumberId, String from, String buttonId) {
        if ("welcome_hi".equals(buttonId)) {
            messageSender.sendTextMessage(phoneNumberId, from, "Hi there! 👋 Welcome to our store!");
            categoryFlowService.sendCategorySelection("v22.0", phoneNumberId, from);
        } else if ("welcome_info".equals(buttonId)) {
            String infoMessage = "🏪 *WebStore is a multi-category e-commerce platform supporting agricultural products, cooked food, and other items.\nWe offer a wide variety of products including\n\nLet me show you our categories!";
            messageSender.sendTextMessage(phoneNumberId, from, infoMessage);
            categoryFlowService.sendCategorySelection("v22.0", phoneNumberId, from);
        }
    }
}