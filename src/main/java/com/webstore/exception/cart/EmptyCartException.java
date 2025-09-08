package com.webstore.exception.cart;

public class EmptyCartException extends CartException {
    public EmptyCartException(String message) {
        super(message);
    }

    public EmptyCartException(Long cartId) {
        super("Cart with ID " + cartId + " is empty and cannot be converted to an order.");
    }
}
