package com.webstore.exception.cart;

public class CartHistoryDatabaseException extends CartHistoryException {

    public CartHistoryDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartHistoryDatabaseException(String message) {
        super(message);
    }
}
