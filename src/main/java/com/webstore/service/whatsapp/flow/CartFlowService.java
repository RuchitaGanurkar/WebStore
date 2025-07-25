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
import com.webstore.util.PaginationUtil;
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
    private static final int CART_ITEMS_PER_PAGE = 5;

    private final CartBusinessService cartService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;
    private final PaginationUtil paginationUtil;

    // Temporary storage for user sessions (in production, use Redis or database)
    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();

    public CartFlowService(CartBusinessService cartService,
                           ProductBusinessService productService,
                           WhatsAppMessageSender messageSender,
                           MessageBuilderService messageBuilder,
                           MessageFormatter formatter,
                           PaginationUtil paginationUtil) {
        this.cartService = cartService;
        this.productService = productService;
        this.messageSender = messageSender;
        this.messageBuilder = messageBuilder;
        this.formatter = formatter;
        this.paginationUtil = paginationUtil;
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
            if (product == null) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ Product not found. Please try again.");
                return;
            }

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
            if (product == null) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ Product not found. Please try again.");
                return;
            }

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
            if (product == null) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ Error retrieving product details. Please try again.");
                return;
            }

            BigDecimal totalPrice = productService.calculateProductTotal(productId, quantity);

            String successMessage = String.format(
                    "✅ *Added to Cart Successfully!*\n\n" +
                            "📦 Product: %s\n" +
                            "🔢 Quantity: %d\n" +
                            "💰 Total: ₹%.2f\n\n" +
                            "What would you like to do next?",
                    product.getProductName(),
                    quantity,
                    totalPrice
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
     * Display cart details with pagination
     */
    public void viewCart(String phoneNumberId, String from) {
        viewCart(phoneNumberId, from, 1);
    }

    public void viewCart(String phoneNumberId, String from, int pageNumber) {
        logger.info("Displaying cart page {} for user: {}", pageNumber, from);

        try {
            List<CartProductResponseDto> allCartProducts = cartService.getCartProducts(from);

            if (allCartProducts.isEmpty()) {
                showEmptyCart(phoneNumberId, from);
                return;
            }

            // Paginate cart products
            PaginationUtil.PaginationResult<CartProductResponseDto> paginationResult =
                    paginationUtil.paginate(allCartProducts, pageNumber, CART_ITEMS_PER_PAGE);

            String cartMessage = buildDetailedCartMessage(from, paginationResult);

            // Build action buttons
            List<WhatsAppRequestDto.Button> actionButtons = new ArrayList<>();
            actionButtons.add(messageBuilder.createButton("checkout_cart", "💳 Checkout"));

            if (paginationResult.getTotalPages() > 1) {
                actionButtons.add(messageBuilder.createButton("edit_cart_p" + pageNumber, "✏️ Edit Items"));
            } else {
                actionButtons.add(messageBuilder.createButton("edit_cart", "✏️ Edit Items"));
            }

            actionButtons.add(messageBuilder.createButton("clear_cart", "🗑️ Clear Cart"));

            // Add pagination buttons if needed
            if (paginationResult.getTotalPages() > 1) {
                if (paginationResult.hasPrevious()) {
                    actionButtons.add(messageBuilder.createButton("cart_prev_p" + (pageNumber - 1), "⬅️ Previous"));
                }
                if (paginationResult.hasNext()) {
                    actionButtons.add(messageBuilder.createButton("cart_next_p" + (pageNumber + 1), "➡️ Next"));
                }
            }

            // WhatsApp buttons are limited to 3, so use the first 3
            List<WhatsAppRequestDto.Button> displayButtons = actionButtons.subList(0, Math.min(3, actionButtons.size()));

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "🛒 Your Cart",
                    cartMessage,
                    paginationResult.getTotalPages() > 1 ?
                            String.format("Page %d of %d", paginationResult.getCurrentPage(), paginationResult.getTotalPages()) :
                            null,
                    displayButtons
            );

            messageSender.sendMessage(phoneNumberId, request, "Cart details message");

        } catch (Exception e) {
            logger.error("Error displaying cart for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error loading cart details. Please try again.");
        }
    }

    /**
     * Show edit cart options with pagination
     */
    public void showEditCartOptions(String phoneNumberId, String from, int pageNumber) {
        logger.info("Showing edit cart options page {} for user: {}", pageNumber, from);

        try {
            List<CartProductResponseDto> allCartProducts = cartService.getCartProducts(from);

            if (allCartProducts.isEmpty()) {
                viewCart(phoneNumberId, from);
                return;
            }

            PaginationUtil.PaginationResult<CartProductResponseDto> paginationResult =
                    paginationUtil.paginate(allCartProducts, pageNumber, CART_ITEMS_PER_PAGE);

            String headerText = String.format("✏️ Edit Cart (Page %d of %d)",
                    paginationResult.getCurrentPage(), paginationResult.getTotalPages());

            StringBuilder bodyText = new StringBuilder();
            bodyText.append("Select an item to modify:\n\n");

            List<WhatsAppRequestDto.Section> sections = new ArrayList<>();
            List<WhatsAppRequestDto.Row> editRows = new ArrayList<>();

            for (CartProductResponseDto cartProduct : paginationResult.getItems()) {
                try {
                    // Get product details using the cart product data
                    Integer productId = getProductIdFromCartProduct(cartProduct);
                    if (productId != null) {
                        ProductResponseDto product = productService.getProductById(productId);
                        if (product != null) {
                            Integer quantity = getQuantityFromCartProduct(cartProduct);
                            BigDecimal unitPrice = productService.getProductPrice(productId);

                            String itemTitle = formatter.truncateRowTitle(product.getProductName());
                            String itemDescription = String.format("Qty: %d | Price: ₹%.2f each",
                                    quantity, unitPrice);

                            editRows.add(messageBuilder.createRow(
                                    "edit_item_" + productId,
                                    itemTitle,
                                    itemDescription
                            ));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing cart product for edit: {}", cartProduct.getCartProductId(), e);
                }
            }

            if (!editRows.isEmpty()) {
                sections.add(messageBuilder.createSection("Cart Items", editRows));
            }

            // Add navigation and action rows
            List<WhatsAppRequestDto.Row> actionRows = new ArrayList<>();

            if (paginationResult.hasPrevious()) {
                actionRows.add(messageBuilder.createRow("edit_cart_prev_p" + (pageNumber - 1),
                        "⬅️ Previous Page", "View previous cart items"));
            }
            if (paginationResult.hasNext()) {
                actionRows.add(messageBuilder.createRow("edit_cart_next_p" + (pageNumber + 1),
                        "➡️ Next Page", "View more cart items"));
            }

            actionRows.add(messageBuilder.createRow("clear_all_cart", "🗑️ Clear All Items", "Remove all items from cart"));
            actionRows.add(messageBuilder.createRow("back_to_cart", "⬅️ Back to Cart", "Return to cart view"));

            if (!actionRows.isEmpty()) {
                sections.add(messageBuilder.createSection("Actions", actionRows));
            }

            WhatsAppRequestDto request = messageBuilder.buildListMessage(
                    from,
                    headerText,
                    bodyText.toString(),
                    null,
                    "Select Option",
                    sections
            );

            messageSender.sendMessage(phoneNumberId, request, "Edit cart options message");

        } catch (Exception e) {
            logger.error("Error showing edit cart options for user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error loading edit options. Please try again.");
        }
    }

    /**
     * Show quantity adjustment options for a specific cart item
     */
    public void showItemQuantityOptions(String phoneNumberId, String from, Integer productId) {
        logger.info("Showing quantity options for product {} for user {}", productId, from);

        try {
            ProductResponseDto product = productService.getProductById(productId);
            if (product == null) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ Product not found.");
                return;
            }

            Integer currentQuantity = cartService.getProductQuantityInCart(from, productId);

            if (currentQuantity == 0) {
                messageSender.sendTextMessage(phoneNumberId, from,
                        "❌ This item is not in your cart.");
                return;
            }

            Integer availableQuantity = productService.getAvailableQuantity(productId);
            Integer maxOrderable = productService.getMaxOrderableQuantity(productId);
            BigDecimal unitPrice = productService.getProductPrice(productId);

            String message = buildCartItemQuantityMessage(product, currentQuantity, availableQuantity, maxOrderable, unitPrice);

            List<WhatsAppRequestDto.Button> buttons = new ArrayList<>();

            if (currentQuantity > 1) {
                buttons.add(messageBuilder.createButton("cart_qty_dec_" + productId, "➖ Decrease"));
            }
            if (currentQuantity < maxOrderable && currentQuantity < availableQuantity) {
                buttons.add(messageBuilder.createButton("cart_qty_inc_" + productId, "➕ Increase"));
            }

            buttons.add(messageBuilder.createButton("cart_remove_" + productId, "🗑️ Remove"));

            // Limit to 3 buttons for WhatsApp
            if (buttons.size() > 3) {
                buttons = buttons.subList(0, 3);
            }

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "✏️ Edit Item Quantity",
                    message,
                    "Choose an action:",
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Item quantity options");

        } catch (Exception e) {
            logger.error("Error showing item quantity options for product {}", productId, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error loading item options. Please try again.");
        }
    }

    /**
     * Handle cart item quantity changes
     */
    public void handleCartQuantityChange(String phoneNumberId, String from, Integer productId, String action) {
        logger.info("Handling quantity change for product {} with action {} for user {}", productId, action, from);

        try {
            CartResponseDto updatedCart;
            String actionMessage;

            switch (action) {
                case "increase":
                    updatedCart = cartService.increaseProductQuantity(from, productId);
                    actionMessage = "✅ Quantity increased successfully!";
                    break;
                case "decrease":
                    updatedCart = cartService.decreaseProductQuantity(from, productId);
                    actionMessage = "✅ Quantity decreased successfully!";
                    break;
                case "remove":
                    updatedCart = cartService.removeProductFromCart(from, productId);
                    actionMessage = "✅ Item removed from cart!";
                    break;
                default:
                    messageSender.sendTextMessage(phoneNumberId, from, "❌ Invalid action.");
                    return;
            }

            // Show updated cart summary
            String cartSummary = cartService.getCartSummary(from);
            String fullMessage = actionMessage + "\n\n" + cartSummary;

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("view_cart", "🛒 View Cart"),
                    messageBuilder.createButton("continue_shopping", "🛍️ Continue Shopping"),
                    messageBuilder.createButton("checkout_cart", "💳 Checkout")
            );

            WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                    from,
                    "✅ Cart Updated",
                    fullMessage,
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, request, "Cart quantity change result");

        } catch (Exception e) {
            logger.error("Error handling quantity change for product {}: {}", productId, e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Error updating quantity: " + e.getMessage());
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
                showEmptyCart(phoneNumberId, from);
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

    /**
     * Handle cart page navigation
     */
    public void handleCartPageNavigation(String phoneNumberId, String from, String navigationId) {
        logger.info("Handling cart page navigation: {} for user: {}", navigationId, from);

        try {
            if (navigationId.startsWith("cart_next_p")) {
                int pageNumber = Integer.parseInt(navigationId.substring("cart_next_p".length()));
                viewCart(phoneNumberId, from, pageNumber);
            } else if (navigationId.startsWith("cart_prev_p")) {
                int pageNumber = Integer.parseInt(navigationId.substring("cart_prev_p".length()));
                viewCart(phoneNumberId, from, pageNumber);
            } else if (navigationId.startsWith("edit_cart_p")) {
                int pageNumber = Integer.parseInt(navigationId.substring("edit_cart_p".length()));
                showEditCartOptions(phoneNumberId, from, pageNumber);
            } else if (navigationId.startsWith("edit_cart_next_p")) {
                int pageNumber = Integer.parseInt(navigationId.substring("edit_cart_next_p".length()));
                showEditCartOptions(phoneNumberId, from, pageNumber);
            } else if (navigationId.startsWith("edit_cart_prev_p")) {
                int pageNumber = Integer.parseInt(navigationId.substring("edit_cart_prev_p".length()));
                showEditCartOptions(phoneNumberId, from, pageNumber);
            }
        } catch (Exception e) {
            logger.error("Error handling cart page navigation: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, from, "⚠️ Unable to navigate cart pages.");
        }
    }

    // ============ HELPER METHODS ============

    private void showEmptyCart(String phoneNumberId, String from) {
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
    }

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

        return buttons.subList(0, Math.min(3, buttons.size())); // Limit to 3 buttons for WhatsApp
    }

    private String buildDetailedCartMessage(String phoneNumber, PaginationUtil.PaginationResult<CartProductResponseDto> paginationResult) {
        StringBuilder message = new StringBuilder();
        message.append("🛒 *Your Shopping Cart*\n\n");

        if (paginationResult.getTotalPages() > 1) {
            message.append(String.format("📄 Page %d of %d\n\n",
                    paginationResult.getCurrentPage(), paginationResult.getTotalPages()));
        }

        BigDecimal pageTotal = BigDecimal.ZERO;
        int itemNumber = ((paginationResult.getCurrentPage() - 1) * CART_ITEMS_PER_PAGE) + 1;

        for (CartProductResponseDto cartProduct : paginationResult.getItems()) {
            try {
                Integer productId = getProductIdFromCartProduct(cartProduct);
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        Integer quantity = getQuantityFromCartProduct(cartProduct);
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
                logger.error("Error processing cart product: {}", cartProduct.getCartProductId(), e);
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

    private String buildCartItemQuantityMessage(ProductResponseDto product, Integer currentQuantity,
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

    private String buildCheckoutSummaryMessage(String phoneNumber, List<CartProductResponseDto> cartProducts) {
        StringBuilder message = new StringBuilder();
        message.append("💳 *Checkout Summary*\n\n");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartProductResponseDto cartProduct : cartProducts) {
            try {
                Integer productId = getProductIdFromCartProduct(cartProduct);
                if (productId != null) {
                    ProductResponseDto product = productService.getProductById(productId);
                    if (product != null) {
                        Integer quantity = getQuantityFromCartProduct(cartProduct);
                        BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                        message.append(String.format("• %s (x%d) - ₹%.2f\n",
                                product.getProductName(), quantity, subtotal));

                        totalAmount = totalAmount.add(subtotal);
                    }
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
                    if (product != null) {
                        Integer quantity = getQuantityFromCartProduct(cartProduct);
                        BigDecimal subtotal = productService.calculateProductTotal(productId, quantity);

                        message.append(String.format("• %s (x%d) - ₹%.2f\n",
                                product.getProductName(), quantity, subtotal));

                        totalAmount = totalAmount.add(subtotal);
                    }
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
     * Helper method to extract product ID from cart product response DTO
     * This needs to be implemented based on your CartProductResponseDto structure
     */
    private Integer getProductIdFromCartProduct(CartProductResponseDto cartProduct) {
        // TODO: Implement this based on your CartProductResponseDto structure
        // This might be something like:
        // return cartProduct.getProductId();

        // For now, return null to avoid compilation errors
        // You need to implement this based on how your DTO is structured
        logger.warn("getProductIdFromCartProduct needs to be implemented based on your DTO structure");
        return null; // Replace with actual implementation
    }

    /**
     * Helper method to extract quantity from cart product response DTO
     */
    private Integer getQuantityFromCartProduct(CartProductResponseDto cartProduct) {
        // TODO: Implement this based on your CartProductResponseDto structure
        // This might be something like:
        // return cartProduct.getQuantity();

        // For now, return default quantity
        logger.warn("getQuantityFromCartProduct needs to be implemented based on your DTO structure");
        return 1; // Replace with actual implementation
    }

    // Inner class for user session management
    public static class UserSession {
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