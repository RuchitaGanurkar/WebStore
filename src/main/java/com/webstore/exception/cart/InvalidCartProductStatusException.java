package com.webstore.exception.cart;

public class InvalidCartProductStatusException extends RuntimeException {
    public InvalidCartProductStatusException(String message) {
        super(message);
    }

    public InvalidCartProductStatusException(String field, String value) {
        super("Invalid cart status: " + field + " with value: " + value);
    }
}
