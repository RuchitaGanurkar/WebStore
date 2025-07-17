package com.webstore.exception.cart;

public class CartProductHistoryServiceUnavailableException extends CartProductHistoryException {
    public CartProductHistoryServiceUnavailableException(String message) {
        super("Cart product history service unavailable: " + message);
    }

    public CartProductHistoryServiceUnavailableException(String message, Throwable cause) {
        super("Cart product history service unavailable: " + message, cause);
    }
}