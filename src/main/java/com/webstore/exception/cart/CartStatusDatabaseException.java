package com.webstore.exception.cart;

public class CartStatusDatabaseException extends CartStatusException {
    public CartStatusDatabaseException(String message) {
        super(message);
    }

    public CartStatusDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
