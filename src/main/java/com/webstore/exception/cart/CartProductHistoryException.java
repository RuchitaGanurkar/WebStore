package com.webstore.exception.cart;

public abstract class CartProductHistoryException extends RuntimeException {
    protected CartProductHistoryException(String message) {
        super(message);
    }

    protected CartProductHistoryException(String message, Throwable cause) {
        super(message, cause);
    }
}