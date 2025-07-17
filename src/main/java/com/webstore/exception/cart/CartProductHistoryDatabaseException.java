package com.webstore.exception.cart;

public class CartProductHistoryDatabaseException extends CartProductHistoryException {
    public CartProductHistoryDatabaseException(String message) {
        super("Database error: " + message);
    }

    public CartProductHistoryDatabaseException(String message, Throwable cause) {
        super("Database error: " + message, cause);
    }
}