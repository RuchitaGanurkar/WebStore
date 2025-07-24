package com.webstore.exception.cart;

public class CartHistoryDatabaseException extends RuntimeException {
    public CartHistoryDatabaseException(String message, Exception ex) {
        super(message);
    }
}
