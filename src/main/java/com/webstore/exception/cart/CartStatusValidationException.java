package com.webstore.exception.cart;

import java.util.List;

public class CartStatusValidationException extends CartStatusException {
    private List<String> validationErrors;

    public CartStatusValidationException(String message) {
        super(message);
    }

    public CartStatusValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
