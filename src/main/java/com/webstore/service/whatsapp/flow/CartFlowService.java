package com.webstore.service.whatsapp.flow;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.business.CartBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartFlowService {

    private static final Logger logger = LoggerFactory.getLogger(CartFlowService.class);

    private final CartBusinessService cartService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;

    // Temporary storage for user sessions (in production, use Redis or database)
    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();

    public CartFlowService(CartBusinessService cartService,
                           ProductBusinessService productService,
                           WhatsAppMessageSender messageSender,
                           MessageBuilderService messageBuilder,
                           MessageFormatter formatter) {
        this.cartService = cartService;
        this.productService = productService;
        this.messageSender = messageSender;
        this.messageBuilder = messageBuilder;
        this.formatter = formatter;
    }

    /**
     * Show quantity selection for a product
     */
    public void showQuantitySelection(String phoneNumberId, String from, Integer productId) {
        logger.info("Showing quantity selection for product ID: {} to user: {}", productId, from);

        try {
            // Validate product first
            ProductBusinessService.ProductValidationResult validation =
                    productService.validateProductForCart(productId, 1);

            if (!validation.isValid()) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ " + validation.getMessage());
                return;
            }

            ProductResponseDto product = productService.getProductById(productId);
            Integer availableQuantity = productService.getAvailableQuantity(productId);
            Integer maxOrderable = productService.getMaxOrderableQuantity(productId);

            if (availableQuantity == null || availableQuantity <= 0) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ Sorry, this product is currently out of stock.");
                return;
            }

            // Store product selection in user session
            UserSession session = userSessions.computeIfAbsent(from, k -> new UserSession());
            session.setSelectedProductId(productId);
            session.setSelectedQuantity(1); // Default quantity

            String quantityMessage = buildQuantitySelectionMessage(product, availableQuantity, maxOrderable, 1);
            List<WhatsAppRequestDto.Button> buttons = buildQuantityButtons(productId, 1, maxOrderable);

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "🛒 Select Quantity",
                    quantityMessage,
                    "Choose quantity or adjust using buttons below:",
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Quantity selection message");

        } catch (Exception e) {
            logger.error("Error showing quantity selection for product ID: {}", productId, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error loading product details. Please try again.");
        }
    }

    /**
     * Handle quantity adjustment (increase/decrease)
     */
    public void adjustQuantity(String phoneNumberId, String from, Integer productId,
                               String action, Integer currentQuantity) {
        logger.info("Adjusting quantity for product ID: {} by user: {}, action: {}",
                productId, from, action);

        try {
            ProductResponseDto product = productService.getProductById(productId);
            Integer availableQuantity = productService.getAvailableQuantity(productId);
            Integer maxOrderable = productService.getMaxOrderableQuantity(productId);

            int newQuantity = currentQuantity;
            if ("increase".equals(action) && currentQuantity < maxOrderable) {
                newQuantity = currentQuantity + 1;
            } else if ("decrease".equals(action) && currentQuantity > 1) {
                newQuantity = currentQuantity - 1;
            }

            // Validate new quantity
            ProductBusinessService.ProductValidationResult validation =
                    productService.validateProductForCart(productId, newQuantity);

            if (!validation.isValid()) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ " + validation.getMessage());
                return;
            }

            // Update session
            UserSession session = userSessions.get(from);
            if (session != null) {
                session.setSelectedQuantity(newQuantity);
            }

            String quantityMessage = buildQuantitySelectionMessage(product, availableQuantity, maxOrderable, newQuantity);
            List<WhatsAppRequestDto.Button> buttons = buildQuantityButtons(productId, newQuantity, maxOrderable);

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "🛒 Select Quantity",
                    quantityMessage,
                    "Choose quantity or adjust using buttons below:",
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Quantity adjustment message");

        } catch (Exception e) {
            logger.error("Error adjusting quantity for product ID: {}", productId, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error adjusting quantity. Please try again.");
        }
    }

    /**
     * Add product to cart with selected quantity
     */
    public void addToCart(String phoneNumberId, String from, Integer productId) {
        logger.info("Adding product ID: {} to cart for user: {}", productId, from);

        try {
            UserSession session = userSessions.get(from);
            int quantity = (session != null && session.getSelectedQuantity() != null)
                    ? session.getSelectedQuantity() : 1;

            // Final validation before adding to cart
            ProductBusinessService.ProductValidationResult validation =
                    productService.validateProductForCart(productId, quantity);

            if (!validation.isValid()) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ " + validation.getMessage());
                return;
            }

            // Add to cart
            CartResponseDto cartResponse = cartService.addProductToCart(from, productId, quantity);

            ProductResponseDto product = productService.getProductById(productId);
            BigDecimal totalPrice = productService.calculateProductTotal(productId, quantity);

            String successMessage = String.format(
                    "✅ *Added to Cart Successfully!*\n\n" +
                            "📦 Product: %s\n" +
                            "🔢 Quantity: %d\n" +
                            "💰 Total: %s\n\n" +
                            "What would you like to do next?",
                    product.getProductName(),
                    quantity,
                    productService.getFormattedProductPrice(productId).replace(productService.getProductPrice(productId).toString(), totalPrice.toString())
            );

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("view_cart", "🛒 View Cart"),
                    messageBuilder.createButton("continue_shopping", "🛍️ Continue Shopping"),
                    messageBuilder.createButton("checkout_now", "💳 Checkout Now")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "✅ Product Added",
                    successMessage,
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Add to cart success message");

            // Clear session
            if (session != null) {
                session.clear();
            }

        } catch (Exception e) {
            logger.error("Error adding product to cart for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error adding product to cart. Please try again.");
        }
    }

    /**
     * Display cart details in a well-crafted format
     */
    public void viewCart(String phoneNumberId, String from) {
        logger.info("Displaying cart for user: {}", from);

        try {
            List<CartProductResponseDto> cartProducts = cartService.getCartProducts(from);

            if (cartProducts.isEmpty()) {
                String emptyCartMessage = "🛒 *Your Cart is Empty*\n\n" +
                        "Start shopping to add items to your cart!";

                List<WhatsAppRequestDto.Button> buttons = List.of(
                        messageBuilder.createButton("browse_categories", "🗂️ Browse Categories"),
                        messageBuilder.createButton("view_offers", "🎯 View Offers")
                );

                WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                        from,
                        "🛒 Your Cart",
                        emptyCartMessage,
                        null,
                        buttons
                );

                messageSender.sendMessage(phoneNumberId, request, "Empty cart message");
                return;
            }

            String cartMessage = buildDetailedCartMessage(from, cartProducts);
            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("checkout_cart", "💳 Checkout"),
                    messageBuilder.createButton("edit_cart", "✏️ Edit Cart"),
                    messageBuilder.createButton("continue_shopping", "🛍️ Continue Shopping")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "🛒 Your Cart",
                    cartMessage,
                    "Choose your next action:",
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Cart details message");

        } catch (Exception e) {
            logger.error("Error displaying cart for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error loading cart details. Please try again.");
        }
    }

    /**
     * Show edit cart options
     */
    public void showEditCartOptions(String phoneNumberId, String from) {
        logger.info("Showing edit cart options for user: {}", from);

        try {
            List<CartProductResponseDto> cartProducts = cartService.getCartProducts(from);

            if (cartProducts.isEmpty()) {
                viewCart(phoneNumberId, from);
                return;
            }

            StringBuilder message = new StringBuilder();
            message.append("✏️ *Edit Your Cart*\n\n");
            message.append("Select an item to modify:\n\n");

            List<WhatsAppRequestDto.Button> buttons = new ArrayList<>();

            int index = 1;
            for (CartProductResponseDto cartProduct : cartProducts) {
                // Get the actual product ID from cart product
                Integer productId = getProductIdFromCartProduct(cartProduct);
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    Integer quantity = cartService.getProductQuantityInCart(from, productId);

                    message.append(String.format("%d. %s (Qty: %d)\n",
                            index++, product.getProductName(), quantity));

                    buttons.add(messageBuilder.createButton(
                            "edit_item_" + productId,
                            "Edit " + product.getProductName().substring(0, Math.min(product.getProductName().length(), 10)) + "..."
                    ));
                }
            }

            buttons.add(messageBuilder.createButton("clear_cart", "🗑️ Clear Cart"));
            buttons.add(messageBuilder.createButton("back_to_cart", "⬅️ Back to Cart"));

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "✏️ Edit Cart",
                    message.toString(),
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Edit cart options message");

        } catch (Exception e) {
            logger.error("Error showing edit cart options for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error loading edit options. Please try again.");
        }
    }

    /**
     * Start checkout process
     */
    public void startCheckout(String phoneNumberId, String from) {
        logger.info("Starting checkout process for user: {}", from);

        try {
            List<CartProductResponseDto> cartProducts = cartService.getCartProducts(from);

            if (cartProducts.isEmpty()) {
                viewCart(phoneNumberId, from);
                return;
            }

            // Validate all cart items before checkout
            List<String> validationErrors = cartService.validateCartItems(from);
            if (!validationErrors.isEmpty()) {
                String errorMessage = "❌ *Cart Validation Issues:*\n\n" +
                        String.join("\n", validationErrors) +
                        "\n\nPlease review your cart before checkout.";

                messageSender.sendTextMessage(phoneNumberId, from, errorMessage);
                viewCart(phoneNumberId, from);
                return;
            }

            // Store checkout session
            UserSession session = userSessions.computeIfAbsent(from, k -> new UserSession());
            session.setCheckoutInProgress(true);

            String checkoutMessage = buildCheckoutSummaryMessage(from, cartProducts);

            String addressPrompt = checkoutMessage +
                    "\n\n📍 *Please provide your delivery address:*\n" +
                    "Include your full address with landmark, city, and pincode.";

            messageSender.sendTextMessage(phoneNumberId, from, addressPrompt);

        } catch (Exception e) {
            logger.error("Error starting checkout for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error starting checkout. Please try again.");
        }
    }

    /**
     * Handle address input and show payment options
     */
    public void handleAddressInput(String phoneNumberId, String from, String address) {
        logger.info("Processing address input for user: {}", from);

        try {
            UserSession session = userSessions.get(from);
            if (session == null || !session.isCheckoutInProgress()) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ No active checkout session. Please start checkout again.");
                return;
            }

            // Validate and store address
            if (address.trim().length() < 20) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ Please provide a complete address with at least 20 characters including landmark, city, and pincode.");
                return;
            }

            session.setDeliveryAddress(address.trim());

            String paymentMessage = "💳 *Select Payment Method*\n\n" +
                    "📍 Delivery Address:\n" + address + "\n\n" +
                    "Choose your preferred payment method:";

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("payment_cod", "💵 Cash on Delivery (COD)"),
                    messageBuilder.createButton("payment_upi", "📱 UPI Payment"),
                    messageBuilder.createButton("edit_address", "✏️ Edit Address")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "💳 Payment Method",
                    paymentMessage,
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Payment method selection");

        } catch (Exception e) {
            logger.error("Error processing address for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error processing address. Please try again.");
        }
    }

    /**
     * Handle payment method selection and complete order
     */
    public void selectPaymentMethod(String phoneNumberId, String from, String paymentMethod) {
        logger.info("Processing payment method selection for user: {}, method: {}", from, paymentMethod);

        try {
            UserSession session = userSessions.get(from);
            if (session == null || !session.isCheckoutInProgress()) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ No active checkout session. Please start checkout again.");
                return;
            }

            session.setPaymentMethod(paymentMethod);

            List<CartProductResponseDto> cartProducts = cartService.getCartProducts(from);
            String orderConfirmation = buildOrderConfirmationMessage(from, cartProducts, session);

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("confirm_order", "✅ Confirm Order"),
                    messageBuilder.createButton("cancel_order", "❌ Cancel Order"),
                    messageBuilder.createButton("edit_details", "✏️ Edit Details")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "📋 Order Confirmation",
                    orderConfirmation,
                    "Please review your order details:",
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Order confirmation");

        } catch (Exception e) {
            logger.error("Error processing payment method for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error processing payment method. Please try again.");
        }
    }

    /**
     * Submit and confirm the order
     */
    public void submitOrder(String phoneNumberId, String from) {
        logger.info("Submitting order for user: {}", from);

        try {
            UserSession session = userSessions.get(from);
            if (session == null || !session.isCheckoutInProgress()) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ No active checkout session. Please start checkout again.");
                return;
            }

            // Final validation before order creation
            List<String> validationErrors = cartService.validateCartItems(from);
            if (!validationErrors.isEmpty()) {
                String errorMessage = "❌ *Order cannot be placed:*\n\n" +
                        String.join("\n", validationErrors);

                messageSender.sendTextMessage(phoneNumberId, from, errorMessage);
                return;
            }

            // Create order in the system
            String orderId = cartService.createOrderFromCart(from, session.getDeliveryAddress(),
                    session.getPaymentMethod());

            // Clear cart and session
            cartService.clearCart(from);
            userSessions.remove(from);

            String successMessage = buildOrderSuccessMessage(orderId, session);

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("track_order_" + orderId, "📦 Track Order"),
                    messageBuilder.createButton("shop_again", "🛍️ Shop Again"),
                    messageBuilder.createButton("view_orders", "📋 View All Orders")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "✅ Order Placed Successfully!",
                    successMessage,
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Order success message");

        } catch (Exception e) {
            logger.error("Error submitting order for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error submitting order. Please try again or contact support.");
        }
    }

    /**
     * Clear cart
     */
    public void clearCart(String phoneNumberId, String from) {
        logger.info("Clearing cart for user: {}", from);

        try {
            String result = cartService.clearCart(from);

            String clearMessage = "🗑️ *Cart Cleared*\n\n" +
                    "Your cart has been emptied successfully.\n" +
                    "Ready to start fresh shopping!";

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("browse_categories", "🗂️ Browse Categories"),
                    messageBuilder.createButton("view_offers", "🎯 View Offers")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "🗑️ Cart Cleared",
                    clearMessage,
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Cart cleared message");

        } catch (Exception e) {
            logger.error("Error clearing cart for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error clearing cart. Please try again.");
        }
    }

    // ============ HELPER METHODS ============

    private String buildQuantitySelectionMessage(ProductResponseDto product,
                                                 Integer availableQuantity, Integer maxOrderable, int selectedQuantity) {
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

    private List<WhatsAppRequestDto.Button> buildQuantityButtons(Integer productId,
                                                                 int currentQuantity, int maxQuantity) {
        List<WhatsAppRequestDto.Button> buttons = new ArrayList<>();

        // Decrease button (disabled if quantity is 1)
        if (currentQuantity > 1) {
            buttons.add(messageBuilder.createButton(
                    "qty_decrease_" + productId + "_" + currentQuantity, "➖ Decrease"
            ));
        }

        // Increase button (disabled if at max quantity)
        if (currentQuantity < maxQuantity) {
            buttons.add(messageBuilder.createButton(
                    "qty_increase_" + productId + "_" + currentQuantity, "➕ Increase"
            ));
        }

        // Add to cart button
        buttons.add(messageBuilder.createButton(
                "add_to_cart_" + productId, "🛒 Add to Cart"
        ));

        // Back button
        buttons.add(messageBuilder.createButton("back_to_product", "⬅️ Back"));

        return buttons;
    }

    private String buildDetailedCartMessage(String phoneNumber, List<CartProductResponseDto> cartProducts) {
        StringBuilder message = new StringBuilder();
        message.append("🛒 *Your Shopping Cart*\n\n");

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;
        int itemNumber = 1;

        for (CartProductResponseDto cartProduct : cartProducts) {
            try {
                Integer productId = getProductIdFromCartProduct(cartProduct);
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    Integer quantity = cartService.getProductQuantityInCart(phoneNumber, productId);
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

                    totalAmount = totalAmount.add(subtotal);
                    totalItems += quantity;
                }
            } catch (Exception e) {
                logger.error("Error processing cart product: {}", cartProduct.getCartProductId(), e);
            }
        }

        message.append("━━━━━━━━━━━━━━━━━━━━\n");
        message.append(String.format("🔢 Total Items: %d\n", totalItems));
        message.append(String.format("💰 **Total Amount: ₹%.2f**", totalAmount));

        return message.toString();
    }

    private String buildCheckoutSummaryMessage(String phoneNumber, List<CartProductResponseDto> cartProducts) {
        StringBuilder message = new StringBuilder();
        message.append("💳 *Checkout Summary*\n\n");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartProductResponseDto cartProduct : cartProducts) {
            try {
                Integer productId = getProductIdFromCartProduct(cartProduct);
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    Integer quantity = cartService.getProductQuantityInCart(phoneNumber, productId);
                    BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                    message.append(String.format("• %s (x%d) - ₹%.2f\n",
                            product.getProductName(), quantity, subtotal));

                    totalAmount = totalAmount.add(subtotal);
                }
            } catch (Exception e) {
                logger.error("Error processing cart product for checkout: {}", cartProduct.getCartProductId(), e);
            }
        }

        message.append("\n━━━━━━━━━━━━━━━━━━━━\n");
        message.append(String.format("💰 **Total: ₹%.2f**", totalAmount));

        return message.toString();
    }

    private String buildOrderConfirmationMessage(String phoneNumber, List<CartProductResponseDto> cartProducts, UserSession session) {
        StringBuilder message = new StringBuilder();
        message.append("📋 *Order Confirmation*\n\n");

        // Order items
        message.append("📦 **Items:**\n");
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartProductResponseDto cartProduct : cartProducts) {
            try {
                Integer productId = getProductIdFromCartProduct(cartProduct);
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    Integer quantity = cartService.getProductQuantityInCart(phoneNumber, productId);
                    BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                    message.append(String.format("• %s (x%d) - ₹%.2f\n",
                            product.getProductName(), quantity, subtotal));

                    totalAmount = totalAmount.add(subtotal);
                }
            } catch (Exception e) {
                logger.error("Error processing cart product for confirmation: {}", cartProduct.getCartProductId(), e);
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

    private String buildOrderSuccessMessage(String orderId, UserSession session) {
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

    /**
     * Helper method to extract product ID from cart product
     * This assumes you have a way to get the product ID from cart product
     * You may need to modify this based on your cart_product table structure
     */
    private Integer getProductIdFromCartProduct(CartProductResponseDto cartProduct) {
        // TODO: Implement this based on your cart_product table structure
        // If you store product_id in cart_product table, retrieve it from there
        // For now, this is a placeholder that needs to be implemented

        // This might be something like:
        // return cartProductRepository.findProductIdByCartProductId(cartProduct.getCartProductId());

        // Or if you have a direct relationship:
        // return cartProduct.getProductId(); // if this field exists

        logger.warn("getProductIdFromCartProduct needs to be implemented based on your table structure");
        return null; // Placeholder - implement based on your schema
    }

    // Inner class for user session management
    private static class UserSession {
        private Integer selectedProductId;
        private Integer selectedQuantity;
        private boolean checkoutInProgress;
        private String deliveryAddress;
        private String paymentMethod;

        // Getters and setters
        public Integer getSelectedProductId() { return selectedProductId; }
        public void setSelectedProductId(Integer selectedProductId) { this.selectedProductId = selectedProductId; }

        public Integer getSelectedQuantity() { return selectedQuantity; }
        public void setSelectedQuantity(Integer selectedQuantity) { this.selectedQuantity = selectedQuantity; }

        public boolean isCheckoutInProgress() { return checkoutInProgress; }
        public void setCheckoutInProgress(boolean checkoutInProgress) { this.checkoutInProgress = checkoutInProgress; }

        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public void clear() {
            this.selectedProductId = null;
            this.selectedQuantity = null;
            this.checkoutInProgress = false;
            this.deliveryAddress = null;
            this.paymentMethod = null;
        }
    }
}