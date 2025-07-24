package com.webstore.exception.cart;

import com.webstore.validation.cart.CartProductHistoryValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InvalidProductIdException extends RuntimeException {
    public InvalidProductIdException(@NotNull(groups = CartProductHistoryValidation.class, message = "Product ID is required") @Positive(groups = CartProductHistoryValidation.class, message = "Product ID must be positive") Integer message) {
        super(String.valueOf(message));
    }
}
