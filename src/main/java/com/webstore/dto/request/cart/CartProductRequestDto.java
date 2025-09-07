package com.webstore.dto.request.cart;

import com.webstore.validation.cart.CartProductValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartProductRequestDto {

    @NotNull(groups = CartProductValidation.class, message = "Cart ID is required")
    @Positive(groups = CartProductValidation.class, message = "Cart ID must be positive")
    private Long cartId;

    @NotNull(groups = CartProductValidation.class, message = "Product ID is required")
    @Positive(groups = CartProductValidation.class, message = "Product ID must be positive")
    private Long productId;

    @NotNull(groups = CartProductValidation.class, message = "Status ID is required")
    @Positive(groups = CartProductValidation.class, message = "Status ID must be positive")
    private Integer statusId;

    @NotNull(groups = CartProductValidation.class, message = "Quantity is required")
    @Positive(groups = CartProductValidation.class, message = "Quantity must be positive")
    private Integer quantity;

    // NEW: Added order_id foreign key
    @Positive(groups = CartProductValidation.class, message = "Order ID must be positive")
    private Long orderId; // Can be null initially, set when order is created
}