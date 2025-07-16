package com.webstore.exception.cart;

public class InvalidCartProductHistoryRequestException extends CartProductHistoryException {
    public InvalidCartProductHistoryRequestException(String message) {
        super("Invalid cart product history request: " + message);
    }

    public InvalidCartProductHistoryRequestException(String field, Object value) {
        super("Invalid value for field '" + field + "': " + value);
    }
}

