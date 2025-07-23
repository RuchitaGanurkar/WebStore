package com.webstore.dto.request.cart;

import com.webstore.validation.cart.CartProductHistoryValidation;
import com.webstore.validation.cart.CartProductValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartProductRequestDto {

    @NotNull(groups = CartProductValidation.class, message = "Cart ID is required")
    @Positive(groups = CartProductValidation.class, message = "Cart ID must be positive")
    private Long cartId;

    @NotNull(groups = CartProductValidation.class, message = "Cart product status ID is required")
    @Positive(groups = CartProductValidation.class, message = "Status ID must be positive")
    private Integer statusId;

    @NotNull(groups = CartProductHistoryValidation.class, message = "Product ID is required")
    @Positive(groups = CartProductHistoryValidation.class, message = "Product ID must be positive")
    private Integer productId;

}
