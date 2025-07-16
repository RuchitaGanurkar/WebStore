package com.webstore.exception.cart;

public class CartProductHistoryOperationNotAllowedException extends CartProductHistoryException {
    public CartProductHistoryOperationNotAllowedException(String operation) {
        super("Operation not allowed: " + operation);
    }

    public CartProductHistoryOperationNotAllowedException(String operation, String reason) {
        super("Operation '" + operation + "' not allowed: " + reason);
    }
}