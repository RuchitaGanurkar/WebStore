package com.webstore.exception.cart;

public class CartProductNotFoundException extends RuntimeException {
    public CartProductNotFoundException(String message) {
        super(message);
    }

    public CartProductNotFoundException(Long cartId) {
        super("Cart product not found with ID: " + cartId);
    }

}