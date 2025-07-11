package com.webstore.exception.cart;

public class InvalidCartStatusException  extends CartStatusException {
    public InvalidCartStatusException(String message) {
        super(message);
    }

    public InvalidCartStatusException(String field, String value) {
        super("Invalid cart status: " + field + " with value: " + value);
    }
}
