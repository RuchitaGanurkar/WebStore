package com.webstore.exception.cart;

import java.util.List;

public class CartProductHistoryValidationException extends CartProductHistoryException {
    private final List<String> validationErrors;

    public CartProductHistoryValidationException(List<String> validationErrors) {
        super("Validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public CartProductHistoryValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}

