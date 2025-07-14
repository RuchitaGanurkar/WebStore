package com.webstore.exception.cart;

import java.util.List;

public class CartProductStatusValidationException extends RuntimeException {

    private List<String> validationErrors;


    public CartProductStatusValidationException(String message) {
        super(message);
    }


    public CartProductStatusValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
