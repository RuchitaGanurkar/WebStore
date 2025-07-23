package com.webstore.dto.request.cart;

import com.webstore.validation.cart.CartProductHistoryValidation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartProductHistoryRequestDto {

    @NotNull(groups = CartProductHistoryValidation.class, message = "Cart product ID is required")
    @Positive(groups = CartProductHistoryValidation.class, message = "Cart product ID must be positive")
    private Long cartProductId;

    @NotNull(message = "Product ID is required", groups = CartProductHistoryValidation.class)
    @Positive(message = "Product ID must be positive", groups = CartProductHistoryValidation.class)
    private Integer productId;

    @NotNull(groups = CartProductHistoryValidation.class, message = "Old quantity is required")
    @Min(value = 0, groups = CartProductHistoryValidation.class, message = "Old quantity must be non-negative")
    private Integer oldQuantity;

    @NotNull(groups = CartProductHistoryValidation.class, message = "New quantity is required")
    @Min(value = 0, groups = CartProductHistoryValidation.class, message = "New quantity must be non-negative")
    private Integer newQuantity;

}
