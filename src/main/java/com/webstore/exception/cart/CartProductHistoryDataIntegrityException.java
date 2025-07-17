package com.webstore.exception.cart;

public class CartProductHistoryDataIntegrityException extends CartProductHistoryException {
    public CartProductHistoryDataIntegrityException(String message) {
        super("Data integrity violation: " + message);
    }

    public CartProductHistoryDataIntegrityException(String message, Throwable cause) {
        super("Data integrity violation: " + message, cause);
    }
}

