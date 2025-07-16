package com.webstore.exception.cart;

public class CartProductHistoryConcurrentModificationException extends CartProductHistoryException {
    public CartProductHistoryConcurrentModificationException(Long historyId) {
        super("Concurrent modification detected for cart product history ID: " + historyId);
    }

    public CartProductHistoryConcurrentModificationException(String message) {
        super(message);
    }
}
