package com.webstore.exception.cart;

public class DuplicateCartProductHistoryException extends CartProductHistoryException {
    public DuplicateCartProductHistoryException(Long cartProductId, Integer productId) {
        super("Duplicate cart product history entry for cart product ID: " + cartProductId +
                " and product ID: " + productId);
    }

    public DuplicateCartProductHistoryException(String message) {
        super(message);
    }
}
