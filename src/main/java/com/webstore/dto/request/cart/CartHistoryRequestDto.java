package com.webstore.dto.request.cart;

import com.webstore.validation.cart.CartHistoryValidation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartHistoryRequestDto {

    @NotNull(groups = CartHistoryValidation.class, message = "Cart ID is required")
    @Positive(groups = CartHistoryValidation.class, message = "Cart ID must be positive")
    private Long cartId;
}
