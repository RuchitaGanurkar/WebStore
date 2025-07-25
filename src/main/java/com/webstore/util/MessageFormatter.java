package com.webstore.util;

import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.service.whatsapp.business.CartBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.flow.CartFlowService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MessageFormatter {

    private static final int MAX_SECTION_TITLE_LENGTH = 24;
    private static final int MAX_ROW_TITLE_LENGTH = 24;
    private static final int MAX_ROW_DESCRIPTION_LENGTH = 72;
    private static final int MAX_BUTTON_TITLE_LENGTH = 20;

    // ============ GENERAL FORMATTING METHODS ============

    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public String truncateSectionTitle(String title) {
        return truncateText(title, MAX_SECTION_TITLE_LENGTH);
    }

    public String truncateRowTitle(String title) {
        return truncateText(title, MAX_ROW_TITLE_LENGTH);
    }

    public String truncateRowDescription(String description) {
        return truncateText(description, MAX_ROW_DESCRIPTION_LENGTH);
    }

    public String truncateButtonTitle(String title) {
        return truncateText(title, MAX_BUTTON_TITLE_LENGTH);
    }

    // ============ PRODUCT FORMATTING METHODS ============

    public String formatProductDetails(String productName, String description, String categoryName, String price) {
        return String.format("📦 *%s*\n\n📝 Description: %s\n🏷️ Category: %s\n💰 Price: %s\n\nReady to purchase?",
                productName,
                description != null ? description : "No description available",
                categoryName != null ? categoryName : "Unknown",
                price);
    }

    public String formatAddToCartMessage(String productName, String price, String categoryName) {
        return String.format(
                "🛒 *Added to Cart!*\n\n" +
                        "📦 Product: %s\n" +
                        "💰 Price: %s\n" +
                        "🏷️ Category: %s\n\n" +
                        "✅ Item added successfully!\n\n" +
                        "Type 'categories' to continue shopping or 'cart' to view your cart.",
                productName, price, categoryName
        );
    }

    // ============ CART FORMATTING METHODS ============

    public String buildQuantitySelectionMessage(ProductResponseDto product,
                                                Integer availableQuantity, Integer maxOrderable, int selectedQuantity,
                                                ProductBusinessService productService) {
        BigDecimal unitPrice = productService.getProductPrice(product.getProductId());
        BigDecimal totalPrice = productService.calculateProductTotal(product.getProductId(), selectedQuantity);

        return String.format(
                "🛒 *Select Quantity*\n\n" +
                        "📦 Product: %s\n" +
                        "💰 Unit Price: ₹%.2f\n" +
                        "📦 Available: %d units\n" +
                        "🎯 Max per order: %d units\n\n" +
                        "🔢 Selected Quantity: *%d*\n" +
                        "💵 Total: ₹%.2f",
                product.getProductName(),
                unitPrice,
                availableQuantity,
                maxOrderable,
                selectedQuantity,
                totalPrice
        );
    }

    public String buildAddToCartSuccessMessage(ProductResponseDto product, Integer quantity, BigDecimal totalPrice) {
        return String.format(
                "✅ *Added to Cart Successfully!*\n\n" +
                        "📦 Product: %s\n" +
                        "🔢 Quantity: %d\n" +
                        "💰 Total: ₹%.2f\n\n" +
                        "What would you like to do next?",
                product.getProductName(),
                quantity,
                totalPrice
        );
    }

    public String buildPaginatedCartMessage(String phoneNumber,
                                            PaginationUtil.PaginationResult<CartProductWithHistory> paginationResult,
                                            CartBusinessService cartService,
                                            ProductBusinessService productService) {
        StringBuilder message = new StringBuilder();
        message.append("🛒 *Your Shopping Cart*\n\n");

        if (paginationResult.getTotalPages() > 1) {
            message.append(String.format("📄 Page %d of %d\n\n",
                    paginationResult.getCurrentPage(), paginationResult.getTotalPages()));
        }

        BigDecimal pageTotal = BigDecimal.ZERO;
        int itemNumber = ((paginationResult.getCurrentPage() - 1) * 5) + 1; // Assuming 5 items per page

        for (CartProductWithHistory cartProductWithHistory : paginationResult.getItems()) {
            try {
                Integer productId = cartProductWithHistory.getProductId();
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        Integer quantity = cartProductWithHistory.getCurrentQuantity();
                        BigDecimal unitPrice = productService.getProductPrice(productId);
                        BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                        message.append(String.format(
                                "%d. **%s**\n" +
                                        "   • Quantity: %d\n" +
                                        "   • Unit Price: ₹%.2f\n" +
                                        "   • Subtotal: ₹%.2f\n\n",
                                itemNumber++,
                                product.getProductName(),
                                quantity,
                                unitPrice,
                                subtotal
                        ));

                        pageTotal = pageTotal.add(subtotal);
                    }
                }
            } catch (Exception e) {
                // Log error and continue - could add logger here
            }
        }

        message.append("━━━━━━━━━━━━━━━━━━━━\n");

        if (paginationResult.getTotalPages() > 1) {
            message.append(String.format("📄 Page Total: ₹%.2f\n", pageTotal));
            BigDecimal grandTotal = cartService.getCartTotalAmount(phoneNumber);
            message.append(String.format("💰 **Grand Total: ₹%.2f**", grandTotal));
        } else {
            int totalItems = cartService.getCartItemCount(phoneNumber);
            message.append(String.format("🔢 Total Items: %d\n", totalItems));
            message.append(String.format("💰 **Total Amount: ₹%.2f**", pageTotal));
        }

        return message.toString();
    }

    public String buildCartItemQuantityMessage(ProductResponseDto product, Integer currentQuantity,
                                               Integer availableQuantity, Integer maxOrderable, BigDecimal unitPrice) {
        BigDecimal currentTotal = unitPrice.multiply(BigDecimal.valueOf(currentQuantity));

        return String.format(
                "✏️ *Edit Item Quantity*\n\n" +
                        "📦 Product: %s\n" +
                        "💰 Unit Price: ₹%.2f\n\n" +
                        "🔢 Current Quantity: *%d*\n" +
                        "💵 Current Total: ₹%.2f\n\n" +
                        "📦 Available: %d units\n" +
                        "🎯 Max per order: %d units\n\n" +
                        "What would you like to do?",
                product.getProductName(),
                unitPrice,
                currentQuantity,
                currentTotal,
                availableQuantity,
                maxOrderable
        );
    }

    public String buildCheckoutSummaryMessage(String phoneNumber, List<CartProductWithHistory> cartProductsWithHistory,
                                              CartBusinessService cartService, ProductBusinessService productService) {
        StringBuilder message = new StringBuilder();
        message.append("💳 *Checkout Summary*\n\n");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartProductWithHistory cartProductWithHistory : cartProductsWithHistory) {
            try {
                Integer productId = cartProductWithHistory.getProductId();
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        Integer quantity = cartProductWithHistory.getCurrentQuantity();
                        BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                        message.append(String.format("• %s (x%d) - ₹%.2f\n",
                                product.getProductName(), quantity, subtotal));

                        totalAmount = totalAmount.add(subtotal);
                    }
                }
            } catch (Exception e) {
                // Log error and continue
            }
        }

        message.append("\n━━━━━━━━━━━━━━━━━━━━\n");
        message.append(String.format("💰 **Total: ₹%.2f**", totalAmount));

        return message.toString();
    }

    public String buildOrderConfirmationMessage(String phoneNumber, List<CartProductWithHistory> cartProductsWithHistory,
                                                CartFlowService.UserSession session, CartBusinessService cartService,
                                                ProductBusinessService productService) {
        StringBuilder message = new StringBuilder();
        message.append("📋 *Order Confirmation*\n\n");

        // Order items
        message.append("📦 **Items:**\n");
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartProductWithHistory cartProductWithHistory : cartProductsWithHistory) {
            try {
                Integer productId = cartProductWithHistory.getProductId();
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        Integer quantity = cartProductWithHistory.getCurrentQuantity();
                        BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                        message.append(String.format("• %s (x%d) - ₹%.2f\n",
                                product.getProductName(), quantity, subtotal));

                        totalAmount = totalAmount.add(subtotal);
                    }
                }
            } catch (Exception e) {
                // Log error and continue
            }
        }

        // Delivery details
        message.append(String.format("\n📍 **Delivery Address:**\n%s\n", session.getDeliveryAddress()));

        // Payment method
        String paymentMethodText = "payment_cod".equals(session.getPaymentMethod())
                ? "💵 Cash on Delivery (COD)"
                : "📱 UPI Payment";
        message.append(String.format("\n💳 **Payment Method:**\n%s\n", paymentMethodText));

        // Total
        message.append("\n━━━━━━━━━━━━━━━━━━━━\n");
        message.append(String.format("💰 **Total Amount: ₹%.2f**\n", totalAmount));
        message.append("🚚 **Delivery:** 2-3 business days");

        return message.toString();
    }

    public String buildOrderSuccessMessage(String orderId, CartFlowService.UserSession session) {
        String paymentMethodText = "payment_cod".equals(session.getPaymentMethod())
                ? "Cash on Delivery"
                : "UPI Payment";

        return String.format(
                "🎉 **Thank you! Your order has been placed successfully.**\n\n" +
                        "📋 **Order #%s**\n" +
                        "💳 **Payment:** %s\n" +
                        "🚚 **Delivery:** 2-3 business days\n\n" +
                        "We'll send you updates about your order status. " +
                        "Thank you for shopping with WebStore! 😊",
                orderId,
                paymentMethodText
        );
    }

    // ============ CART UTILITY METHODS ============

    public String formatCartSummary(int totalItems, BigDecimal totalAmount) {
        if (totalItems == 0) {
            return "Your cart is empty";
        }

        return String.format("🛒 Cart: %d item%s | Total: ₹%.2f",
                totalItems,
                totalItems == 1 ? "" : "s",
                totalAmount);
    }

    public String formatCartHeader(int currentPage, int totalPages, int totalItems) {
        if (totalPages <= 1) {
            return String.format("🛒 Your Cart (%d item%s)",
                    totalItems,
                    totalItems == 1 ? "" : "s");
        } else {
            return String.format("🛒 Your Cart (Page %d of %d)", currentPage, totalPages);
        }
    }

    public String formatProductInCart(String productName, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        return String.format(
                "• **%s**\n" +
                        "  Qty: %d | Unit: ₹%.2f | Total: ₹%.2f",
                truncateText(productName, 40),
                quantity,
                unitPrice,
                subtotal
        );
    }

    public String formatPaginationInfo(int currentPage, int totalPages, int totalItems) {
        return String.format("Page %d of %d | %d total item%s",
                currentPage,
                totalPages,
                totalItems,
                totalItems == 1 ? "" : "s");
    }

    // ============ ERROR AND STATUS MESSAGES ============

    public String formatCartError(String operation, String reason) {
        return String.format("❌ *Error %s*\n\n%s\n\nPlease try again or contact support.",
                operation, reason);
    }

    public String formatCartSuccess(String operation, String details) {
        return String.format("✅ *%s Successful*\n\n%s", operation, details);
    }

    public String formatValidationError(List<String> errors) {
        StringBuilder message = new StringBuilder();
        message.append("❌ *Validation Issues:*\n\n");

        for (int i = 0; i < errors.size(); i++) {
            message.append(String.format("%d. %s\n", i + 1, errors.get(i)));
        }

        message.append("\nPlease review and fix these issues.");
        return message.toString();
    }

    // ============ CATEGORY AND PRODUCT FORMATTING (EXISTING) ============

    public String formatCategoryHeader(int currentPage, int totalPages) {
        if (totalPages <= 1) {
            return "🗂️ Browse Categories";
        } else {
            return String.format("🗂️ Categories (Page %d of %d)", currentPage, totalPages);
        }
    }

    public String formatProductHeader(String categoryName, int currentPage, int totalPages) {
        if (totalPages <= 1) {
            return String.format("📦 %s Products", categoryName);
        } else {
            return String.format("📦 %s (Page %d of %d)", categoryName, currentPage, totalPages);
        }
    }

    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Price not available";
        }
        return String.format("₹%.2f", price);
    }

    public String formatQuantityRange(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity == null && maxQuantity == null) {
            return "Any quantity";
        } else if (minQuantity == null) {
            return String.format("Max %d units", maxQuantity);
        } else if (maxQuantity == null) {
            return String.format("Min %d units", minQuantity);
        } else if (minQuantity.equals(maxQuantity)) {
            return String.format("Exactly %d units", minQuantity);
        } else {
            return String.format("%d-%d units", minQuantity, maxQuantity);
        }
    }

    // ============ UTILITY METHODS ============

    public String addEmojis(String text, String... emojis) {
        if (emojis.length == 0) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        for (String emoji : emojis) {
            result.append(emoji).append(" ");
        }
        result.append(text);

        return result.toString();
    }

    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 10) {
            return phoneNumber;
        }

        // Format as +91 XXXXX XXXXX
        if (phoneNumber.startsWith("+91")) {
            return phoneNumber;
        } else if (phoneNumber.startsWith("91")) {
            return "+" + phoneNumber;
        } else {
            return "+91" + phoneNumber;
        }
    }

    public String formatDateTime(String dateTime) {
        // Add date/time formatting logic as needed
        return dateTime;
    }

    public String createSeparator(int length) {
        return "━".repeat(Math.max(0, length));
    }

    public String createSeparator() {
        return createSeparator(20);
    }

    // ============ HELPER CLASS FOR CART PRODUCTS WITH HISTORY ============

    /**
     * Helper class to represent cart product with its history data
     * This should match the CartProductWithHistory class in CartBusinessService
     */
    public static class CartProductWithHistory {
        private Long cartProductId;
        private Long cartId;
        private Integer statusId;
        private Integer productId;        // From history
        private Integer currentQuantity;  // From history

        // Constructors
        public CartProductWithHistory() {}

        public CartProductWithHistory(Long cartProductId, Long cartId, Integer statusId,
                                      Integer productId, Integer currentQuantity) {
            this.cartProductId = cartProductId;
            this.cartId = cartId;
            this.statusId = statusId;
            this.productId = productId;
            this.currentQuantity = currentQuantity;
        }

        // Getters and setters
        public Long getCartProductId() { return cartProductId; }
        public void setCartProductId(Long cartProductId) { this.cartProductId = cartProductId; }

        public Long getCartId() { return cartId; }
        public void setCartId(Long cartId) { this.cartId = cartId; }

        public Integer getStatusId() { return statusId; }
        public void setStatusId(Integer statusId) { this.statusId = statusId; }

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public Integer getCurrentQuantity() { return currentQuantity; }
        public void setCurrentQuantity(Integer currentQuantity) { this.currentQuantity = currentQuantity; }
    }

    // ============ OVERLOADED METHODS FOR BACKWARD COMPATIBILITY ============

    /**
     * Overloaded method for buildQuantitySelectionMessage without ProductBusinessService
     * Will use placeholder values for price
     */
    public String buildQuantitySelectionMessage(ProductResponseDto product,
                                                Integer availableQuantity, Integer maxOrderable, int selectedQuantity) {
        BigDecimal unitPrice = BigDecimal.valueOf(100); // Placeholder
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(selectedQuantity));

        return String.format(
                "🛒 *Select Quantity*\n\n" +
                        "📦 Product: %s\n" +
                        "💰 Unit Price: ₹%.2f\n" +
                        "📦 Available: %d units\n" +
                        "🎯 Max per order: %d units\n\n" +
                        "🔢 Selected Quantity: *%d*\n" +
                        "💵 Total: ₹%.2f",
                product.getProductName(),
                unitPrice,
                availableQuantity,
                maxOrderable,
                selectedQuantity,
                totalPrice
        );
    }

    /**
     * Legacy method for backward compatibility - converts CartProductResponseDto to CartProductWithHistory
     * Note: This will only work if you implement a way to get productId and quantity from CartProductResponseDto
     */
    public String buildPaginatedCartMessage(String phoneNumber,
                                            PaginationUtil.PaginationResult<CartProductResponseDto> paginationResult,
                                            CartBusinessService cartService) {
        // Convert CartProductResponseDto to CartProductWithHistory
        // This is a placeholder implementation - you'll need to implement proper conversion
        List<CartProductWithHistory> cartProductsWithHistory = paginationResult.getItems().stream()
                .map(this::convertToCartProductWithHistory)
                .collect(java.util.stream.Collectors.toList());

        PaginationUtil.PaginationResult<CartProductWithHistory> convertedResult =
                new PaginationUtil.PaginationResult<>(
                        cartProductsWithHistory,
                        paginationResult.getCurrentPage(),
                        paginationResult.getTotalPages(),
                        paginationResult.getTotalItems()
                );

        // Use the proper method with ProductBusinessService - you'll need to inject it
        return buildPaginatedCartMessage(phoneNumber, convertedResult, cartService, null);
    }

    private CartProductWithHistory convertToCartProductWithHistory(CartProductResponseDto cartProduct) {
        // TODO: Implement proper conversion
        // This should get the latest history record for the cart product
        // and extract productId and quantity from there

        CartProductWithHistory result = new CartProductWithHistory();
        result.setCartProductId(cartProduct.getCartProductId());
        result.setCartId(cartProduct.getCartId());
        result.setStatusId(cartProduct.getStatusId());
        // These need to be retrieved from CartProductHistory:
        result.setProductId(1); // TODO: Get from latest history
        result.setCurrentQuantity(1); // TODO: Get from latest history

        return result;
    }
}