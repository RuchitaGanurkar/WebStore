package com.webstore.exception.cart;

public class EmptyCartProductHistoryException extends CartProductHistoryException {
    public EmptyCartProductHistoryException(String message) {
        super(message);
    }

    public EmptyCartProductHistoryException() {
        super("No cart product history records found");
    }
}