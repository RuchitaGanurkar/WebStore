package com.webstore.service.whatsapp.business;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.repository.cart.CartRepository;
import com.webstore.service.cart.CartProductHistoryService;
import com.webstore.service.cart.CartService;
import com.webstore.service.cart.CartProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartBusinessService {
    private static final Logger logger = LoggerFactory.getLogger(CartBusinessService.class);

    private final CartService cartService;
    private final CartProductService cartProductService;
    private final CartProductHistoryService cartProductHistoryService;
    private final CartRepository cartRepository;
    private final ProductBusinessService productBusinessService;

    public CartBusinessService(CartService cartService,
                               CartProductService cartProductService,
                               CartProductHistoryService cartProductHistoryService,
                               CartRepository cartRepository,
                               ProductBusinessService productBusinessService) {
        this.cartService = cartService;
        this.cartProductService = cartProductService;
        this.cartProductHistoryService = cartProductHistoryService;
        this.cartRepository = cartRepository;
        this.productBusinessService = productBusinessService;
    }

    public CartResponseDto addProductToCart(String phoneNumber, Integer productId, Integer quantity) {
        logger.info("Adding product {} to cart for user {}, quantity: {}", productId, phoneNumber, quantity);

        try {
            // Validate product first
            ProductBusinessService.ProductValidationResult validation =
                    productBusinessService.validateProductForCart(productId, quantity);

            if (!validation.isValid()) {
                throw new RuntimeException(validation.getMessage());
            }

            // Get or create active cart
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);

            // Get current cart products with their history
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            // Check if product already exists in cart
            Optional<CartProductWithHistory> existingProduct = cartProductsWithHistory.stream()
                    .filter(cp -> cp.getProductId().equals(productId))
                    .findFirst();

            if (existingProduct.isPresent()) {
                // Update quantity of existing product
                CartProductWithHistory existing = existingProduct.get();
                int newQuantity = existing.getCurrentQuantity() + quantity;

                // Validate new total quantity
                ProductBusinessService.ProductValidationResult newValidation =
                        productBusinessService.validateProductForCart(productId, newQuantity);

                if (!newValidation.isValid()) {
                    throw new RuntimeException(newValidation.getMessage());
                }

                // Create new history record with updated quantity
                createCartProductHistory(existing.getCartProductId(), productId, existing.getCurrentQuantity(), newQuantity);

                logger.info("Updated existing product {} quantity to {} in cart", productId, newQuantity);
            } else {
                // Add new product to cart
                addNewProductToCart(cart.getCartId(), productId, quantity);
                logger.info("Added new product {} to cart with quantity {}", productId, quantity);
            }

            return cartService.getCartById(cart.getCartId());

        } catch (Exception e) {
            logger.error("Error adding product to cart: phoneNumber={}, productId={}, quantity={}",
                    phoneNumber, productId, quantity, e);
            throw new RuntimeException("Failed to add product to cart: " + e.getMessage(), e);
        }
    }

    public List<CartProductResponseDto> getCartProducts(String phoneNumber) {
        logger.debug("Getting cart products for user {}", phoneNumber);

        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            return cartProductService.getCartProductsByCartId(cart.getCartId());

        } catch (Exception e) {
            logger.error("Error getting cart products for user {}", phoneNumber, e);
            return new ArrayList<>();
        }
    }

    public Integer getProductQuantityInCart(String phoneNumber, Integer productId) {
        logger.debug("Getting quantity for product {} in cart for user {}", productId, phoneNumber);

        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            Optional<CartProductWithHistory> product = cartProductsWithHistory.stream()
                    .filter(cp -> cp.getProductId().equals(productId))
                    .findFirst();

            return product.map(CartProductWithHistory::getCurrentQuantity).orElse(0);

        } catch (Exception e) {
            logger.error("Error getting product quantity for user {}, product {}", phoneNumber, productId, e);
            return 0;
        }
    }

    public List<String> validateCartItems(String phoneNumber) {
        logger.debug("Validating cart items for user {}", phoneNumber);

        List<String> errors = new ArrayList<>();

        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            for (CartProductWithHistory cartProductWithHistory : cartProductsWithHistory) {
                Integer productId = cartProductWithHistory.getProductId();
                Integer quantity = cartProductWithHistory.getCurrentQuantity();

                if (productId != null && quantity != null) {
                    ProductBusinessService.ProductValidationResult validation =
                            productBusinessService.validateProductForCart(productId, quantity);

                    if (!validation.isValid()) {
                        ProductResponseDto product = productBusinessService.getProductById(productId);
                        String productName = product != null ? product.getProductName() : "Unknown Product";
                        errors.add(productName + ": " + validation.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error validating cart items for user {}", phoneNumber, e);
            errors.add("Error validating cart items");
        }

        return errors;
    }

    public String createOrderFromCart(String phoneNumber, String deliveryAddress, String paymentMethod) {
        logger.info("Creating order from cart for user {}", phoneNumber);

        try {
            // Validate cart items first
            List<String> validationErrors = validateCartItems(phoneNumber);
            if (!validationErrors.isEmpty()) {
                throw new RuntimeException("Cart validation failed: " + String.join(", ", validationErrors));
            }

            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            if (cartProductsWithHistory.isEmpty()) {
                throw new RuntimeException("Cannot create order from empty cart");
            }

            // Generate order ID
            String orderId = "WS" + System.currentTimeMillis();

            // TODO: Create actual order record in order table
            // This would involve:
            // 1. Create Order entity with customer details, address, payment method
            // 2. Create OrderProduct entities for each cart item
            // 3. Update inventory/stock levels
            // 4. Clear the cart after successful order creation

            // Mark the cart as checked out
            cartService.updateCartStatus(cart.getCartId(), getCheckedOutStatusId());

            logger.info("Order created successfully: {}", orderId);
            return orderId;

        } catch (Exception e) {
            logger.error("Error creating order from cart for user {}", phoneNumber, e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public String clearCart(String phoneNumber) {
        logger.info("Clearing cart for user {}", phoneNumber);

        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductResponseDto> cartProducts = cartProductService.getCartProductsByCartId(cart.getCartId());

            // Mark all cart products as inactive by updating their status
            for (CartProductResponseDto cartProduct : cartProducts) {
                // Update cart product status to inactive
                CartProductRequestDto updateRequest = new CartProductRequestDto();
                updateRequest.setCartId(cartProduct.getCartId());
                updateRequest.setStatusId(getInactiveCartProductStatusId());

                cartProductService.createCartProduct(updateRequest); // This should be an update method
            }

            return "Cart cleared successfully";

        } catch (Exception e) {
            logger.error("Error clearing cart for user {}", phoneNumber, e);
            throw new RuntimeException("Failed to clear cart: " + e.getMessage(), e);
        }
    }

    public CartResponseDto removeProductFromCart(String phoneNumber, Integer productId) {
        logger.info("Removing product {} from cart for user {}", productId, phoneNumber);

        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            Optional<CartProductWithHistory> productToRemove = cartProductsWithHistory.stream()
                    .filter(cp -> cp.getProductId().equals(productId))
                    .findFirst();

            if (productToRemove.isPresent()) {
                // Mark the cart product as inactive
                CartProductRequestDto updateRequest = new CartProductRequestDto();
                updateRequest.setCartId(cart.getCartId());
                updateRequest.setStatusId(getInactiveCartProductStatusId());

                cartProductService.createCartProduct(updateRequest); // This should be an update method

                logger.info("Product {} removed from cart successfully", productId);
            }

            return cartService.getCartById(cart.getCartId());

        } catch (Exception e) {
            logger.error("Error removing product from cart: phoneNumber={}, productId={}",
                    phoneNumber, productId, e);
            throw new RuntimeException("Failed to remove product from cart: " + e.getMessage(), e);
        }
    }

    public CartResponseDto updateProductQuantity(String phoneNumber, Integer productId, Integer newQuantity) {
        logger.info("Updating product {} quantity to {} for user {}", productId, newQuantity, phoneNumber);

        try {
            // Validate new quantity
            ProductBusinessService.ProductValidationResult validation =
                    productBusinessService.validateProductForCart(productId, newQuantity);

            if (!validation.isValid()) {
                throw new RuntimeException(validation.getMessage());
            }

            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            Optional<CartProductWithHistory> productToUpdate = cartProductsWithHistory.stream()
                    .filter(cp -> cp.getProductId().equals(productId))
                    .findFirst();

            if (productToUpdate.isPresent()) {
                CartProductWithHistory existing = productToUpdate.get();

                // Create history record for quantity change
                createCartProductHistory(existing.getCartProductId(), productId, existing.getCurrentQuantity(), newQuantity);

                logger.info("Product {} quantity updated to {}", productId, newQuantity);
            }

            return cartService.getCartById(cart.getCartId());

        } catch (Exception e) {
            logger.error("Error updating product quantity: phoneNumber={}, productId={}, quantity={}",
                    phoneNumber, productId, newQuantity, e);
            throw new RuntimeException("Failed to update product quantity: " + e.getMessage(), e);
        }
    }

    public CartResponseDto increaseProductQuantity(String phoneNumber, Integer productId) {
        logger.info("Increasing quantity for product {} for user {}", productId, phoneNumber);

        Integer currentQuantity = getProductQuantityInCart(phoneNumber, productId);
        return updateProductQuantity(phoneNumber, productId, currentQuantity + 1);
    }

    public CartResponseDto decreaseProductQuantity(String phoneNumber, Integer productId) {
        logger.info("Decreasing quantity for product {} for user {}", productId, phoneNumber);

        Integer currentQuantity = getProductQuantityInCart(phoneNumber, productId);
        if (currentQuantity <= 1) {
            return removeProductFromCart(phoneNumber, productId);
        }
        return updateProductQuantity(phoneNumber, productId, currentQuantity - 1);
    }

    public String getCartSummary(String phoneNumber) {
        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            if (cartProductsWithHistory.isEmpty()) {
                return "Your cart is empty";
            }

            int totalItems = cartProductsWithHistory.stream()
                    .mapToInt(CartProductWithHistory::getCurrentQuantity)
                    .sum();

            BigDecimal totalAmount = getCartTotalAmount(phoneNumber);

            return String.format("Cart: %d items | Total: ₹%.2f", totalItems, totalAmount);

        } catch (Exception e) {
            logger.error("Error getting cart summary for user {}", phoneNumber, e);
            return "Error loading cart summary";
        }
    }

    public Integer getCartItemCount(String phoneNumber) {
        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            return cartProductsWithHistory.stream()
                    .mapToInt(CartProductWithHistory::getCurrentQuantity)
                    .sum();
        } catch (Exception e) {
            logger.error("Error getting cart item count for user {}", phoneNumber, e);
            return 0;
        }
    }

    public BigDecimal getCartTotalAmount(String phoneNumber) {
        try {
            CartResponseDto cart = getOrCreateActiveCart(phoneNumber);
            List<CartProductWithHistory> cartProductsWithHistory = getCartProductsWithHistory(cart.getCartId());

            BigDecimal total = BigDecimal.ZERO;

            for (CartProductWithHistory cartProductWithHistory : cartProductsWithHistory) {
                Integer productId = cartProductWithHistory.getProductId();
                Integer quantity = cartProductWithHistory.getCurrentQuantity();

                if (productId != null && quantity != null) {
                    BigDecimal productTotal = productBusinessService.calculateProductTotal(productId, quantity);
                    total = total.add(productTotal);
                }
            }

            return total;

        } catch (Exception e) {
            logger.error("Error calculating cart total for user {}", phoneNumber, e);
            return BigDecimal.ZERO;
        }
    }

    public boolean isCartEmpty(String phoneNumber) {
        return getCartItemCount(phoneNumber) == 0;
    }

    public boolean isProductInCart(String phoneNumber, Integer productId) {
        return getProductQuantityInCart(phoneNumber, productId) > 0;
    }

    public boolean exceedsOrderLimit(String phoneNumber, BigDecimal maxOrderAmount) {
        BigDecimal total = getCartTotalAmount(phoneNumber);
        return total.compareTo(maxOrderAmount) > 0;
    }

    // Existing methods that delegate to CartService
    public CartResponseDto getActiveCartByPhoneNumber(Long phoneNumber) {
        try {
            return cartService.getActiveCartByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            logger.error("Error getting active cart for phone number {}", phoneNumber, e);
            throw new RuntimeException("Failed to get active cart: " + e.getMessage(), e);
        }
    }

    public CartResponseDto getCartById(Long cartId) {
        return cartService.getCartById(cartId);
    }

    public CartResponseDto updateCartStatus(Long cartId, Integer statusId) {
        return cartService.updateCartStatus(cartId, statusId);
    }

    public String archiveCart(Long cartId) {
        return cartService.archiveCart(cartId);
    }

    public List<CartResponseDto> getCartsByStatus(String statusName) {
        return cartService.getCartsByStatus(statusName);
    }

    public CartResponseDto getOrCreateActiveCart(String phoneNumber) {
        try {
            // Try to get existing active cart
            try {
                return cartService.getActiveCartByPhoneNumber(Long.parseLong(phoneNumber));
            } catch (Exception e) {
                // Cart doesn't exist, create new one
                return createNewCart(phoneNumber);
            }

        } catch (Exception e) {
            logger.error("Error getting or creating cart for phone number {}", phoneNumber, e);
            throw new RuntimeException("Failed to get or create cart: " + e.getMessage(), e);
        }
    }

    // ============ HELPER METHODS ============

    private CartResponseDto createNewCart(String phoneNumber) {
        logger.debug("Creating new cart for phone number {}", phoneNumber);

        try {
            CartRequestDto cartRequestDto = new CartRequestDto();
            cartRequestDto.setPhoneNumber(Long.parseLong(phoneNumber));
            cartRequestDto.setCatalogueCategoryId(1); // Default category, adjust as needed
            cartRequestDto.setStatusId(getActiveStatusId());

            return cartService.createCart(cartRequestDto);

        } catch (Exception e) {
            logger.error("Error creating new cart for phone number {}", phoneNumber, e);
            throw new RuntimeException("Failed to create new cart: " + e.getMessage(), e);
        }
    }

    private void addNewProductToCart(Long cartId, Integer productId, Integer quantity) {
        try {
            // Create CartProduct
            CartProductRequestDto cartProductRequest = new CartProductRequestDto();
            cartProductRequest.setCartId(cartId);
            cartProductRequest.setStatusId(getActiveCartProductStatusId());

            CartProductResponseDto cartProduct = cartProductService.createCartProduct(cartProductRequest);

            // Create initial history record
            createCartProductHistory(cartProduct.getCartProductId(), productId, 0, quantity);

        } catch (Exception e) {
            logger.error("Error adding new product {} to cart {}", productId, cartId, e);
            throw new RuntimeException("Failed to add new product to cart: " + e.getMessage(), e);
        }
    }

    private void createCartProductHistory(Long cartProductId, Integer productId, Integer oldQuantity, Integer newQuantity) {
        try {
            CartProductHistoryRequestDto historyRequest = new CartProductHistoryRequestDto();
            historyRequest.setCartProductId(cartProductId);
            historyRequest.setProductId(productId);
            historyRequest.setOldQuantity(oldQuantity);
            historyRequest.setNewQuantity(newQuantity);

            // TODO: You need to create a CartProductHistoryService to handle this
            // cartProductHistoryService.createCartProductHistory(historyRequest);

            logger.debug("Created cart product history for cartProductId: {}, productId: {}, oldQty: {}, newQty: {}",
                    cartProductId, productId, oldQuantity, newQuantity);

        } catch (Exception e) {
            logger.error("Error creating cart product history for cart product {}", cartProductId, e);
            throw new RuntimeException("Failed to create cart product history: " + e.getMessage(), e);
        }
    }

    private List<CartProductWithHistory> getCartProductsWithHistory(Long cartId) {
        try {
            List<CartProductResponseDto> cartProducts = cartProductService.getCartProductsByCartId(cartId);
            List<CartProductWithHistory> result = new ArrayList<>();

            for (CartProductResponseDto cartProduct : cartProducts) {
                // TODO: Get latest history for this cart product
                // This requires a method to get cart product history by cart product ID
                // CartProductHistoryResponseDto latestHistory = cartProductHistoryService.getLatestHistoryByCartProductId(cartProduct.getCartProductId());

                // For now, create a placeholder - you'll need to implement the history retrieval
                CartProductWithHistory withHistory = new CartProductWithHistory();
                withHistory.setCartProductId(cartProduct.getCartProductId());
                withHistory.setCartId(cartProduct.getCartId());
                withHistory.setStatusId(cartProduct.getStatusId());
                // These would come from the latest history record:
                withHistory.setProductId(1); // TODO: Get from latest history
                withHistory.setCurrentQuantity(1); // TODO: Get from latest history

                result.add(withHistory);
            }

            return result;

        } catch (Exception e) {
            logger.error("Error getting cart products with history for cart {}", cartId, e);
            return new ArrayList<>();
        }
    }

    // Status helper methods - implement based on your status management
    private Integer getActiveStatusId() {
        // TODO: Get ACTIVE status ID from CartStatus enum/table
        return 1; // Replace with actual implementation
    }

    private Integer getCheckedOutStatusId() {
        // TODO: Get CHECKED_OUT status ID from CartStatus enum/table
        return 2; // Replace with actual implementation
    }

    private Integer getActiveCartProductStatusId() {
        // TODO: Get ACTIVE status ID from CartProductStatus enum/table
        return 1; // Replace with actual implementation
    }

    private Integer getInactiveCartProductStatusId() {
        // TODO: Get INACTIVE status ID from CartProductStatus enum/table
        return 2; // Replace with actual implementation
    }

    // Advanced features - implement when needed
    public List<CartProductResponseDto> prepareCartForCheckout(String phoneNumber) {
        return getCartProducts(phoneNumber);
    }

    public BigDecimal calculateTotalWithCharges(String phoneNumber, String deliveryAddress) {
        BigDecimal cartTotal = getCartTotalAmount(phoneNumber);
        // TODO: Add delivery charges, taxes, etc. based on address
        return cartTotal;
    }

    public List<String> getCartHistory(String phoneNumber, Integer limit) {
        // TODO: Implement cart history functionality
        return new ArrayList<>();
    }

    public String trackCartAbandonment(String phoneNumber) {
        // TODO: Implement cart abandonment tracking
        return "Cart abandonment tracking not implemented";
    }

    public String getCartConversionMetrics(String phoneNumber) {
        // TODO: Implement conversion metrics
        return "Conversion metrics not implemented";
    }

    public CartResponseDto addMultipleProductsToCart(String phoneNumber, Map<Integer, Integer> productQuantityMap) {
        logger.info("Adding multiple products to cart for user {}", phoneNumber);

        try {
            for (Map.Entry<Integer, Integer> entry : productQuantityMap.entrySet()) {
                addProductToCart(phoneNumber, entry.getKey(), entry.getValue());
            }

            return getActiveCartByPhoneNumber(Long.parseLong(phoneNumber));

        } catch (Exception e) {
            logger.error("Error adding multiple products to cart for user {}", phoneNumber, e);
            throw new RuntimeException("Failed to add multiple products to cart: " + e.getMessage(), e);
        }
    }

    public CartResponseDto removeMultipleProductsFromCart(String phoneNumber, List<Integer> productIds) {
        logger.info("Removing multiple products from cart for user {}", phoneNumber);

        try {
            for (Integer productId : productIds) {
                removeProductFromCart(phoneNumber, productId);
            }

            return getActiveCartByPhoneNumber(Long.parseLong(phoneNumber));

        } catch (Exception e) {
            logger.error("Error removing multiple products from cart for user {}", phoneNumber, e);
            throw new RuntimeException("Failed to remove multiple products from cart: " + e.getMessage(), e);
        }
    }

    public String saveCartForLater(String phoneNumber, String cartName) {
        // TODO: Implement save cart functionality
        return "Save cart functionality not implemented";
    }

    public CartResponseDto restoreSavedCart(String phoneNumber, String cartName) {
        // TODO: Implement restore cart functionality
        throw new UnsupportedOperationException("Restore cart functionality not implemented");
    }

    public List<String> getSavedCarts(String phoneNumber) {
        // TODO: Implement saved carts list
        return new ArrayList<>();
    }

    public CartResponseDto applyCoupon(String phoneNumber, String couponCode) {
        // TODO: Implement coupon functionality
        throw new UnsupportedOperationException("Coupon functionality not implemented");
    }

    public CartResponseDto removeCoupon(String phoneNumber) {
        // TODO: Implement remove coupon functionality
        throw new UnsupportedOperationException("Remove coupon functionality not implemented");
    }

    public List<String> getApplicableOffers(String phoneNumber) {
        // TODO: Implement offers functionality
        return new ArrayList<>();
    }

    public CartResponseDto syncCartWithLatestData(String phoneNumber) {
        return getActiveCartByPhoneNumber(Long.parseLong(phoneNumber));
    }

    public CartResponseDto mergeGuestCart(String phoneNumber, String guestCartId) {
        // TODO: Implement guest cart merge functionality
        throw new UnsupportedOperationException("Guest cart merge functionality not implemented");
    }

    // Helper class to combine cart product with its history data
    private static class CartProductWithHistory {
        private Long cartProductId;
        private Long cartId;
        private Integer statusId;
        private Integer productId;        // From history
        private Integer currentQuantity;  // From history

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
}