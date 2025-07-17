package com.webstore.exception.cart;

public class CartProductHistoryLimitExceededException extends CartProductHistoryException {
    public CartProductHistoryLimitExceededException(int limit) {
        super("Cart product history limit exceeded. Maximum allowed: " + limit);
    }

    public CartProductHistoryLimitExceededException(String message) {
        super(message);
    }
}