package com.webstore.exception.cart;

public class EmptyCartHistoryException extends RuntimeException {
    public EmptyCartHistoryException(String message) {
        super(message);
    }
}
