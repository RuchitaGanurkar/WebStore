package com.webstore.exception.cart;

public class CartProductHistoryNotFoundException extends CartProductHistoryException {
    public CartProductHistoryNotFoundException(Long historyId) {
        super("Cart product history not found with ID: " + historyId);
    }

    public CartProductHistoryNotFoundException(String message) {
        super(message);
    }
}