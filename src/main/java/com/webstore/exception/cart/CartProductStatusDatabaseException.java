package com.webstore.exception.cart;

public class CartProductStatusDatabaseException extends RuntimeException {
    public CartProductStatusDatabaseException(String message) {
        super(message);
    }


    public CartProductStatusDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
