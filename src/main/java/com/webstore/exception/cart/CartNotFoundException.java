package com.webstore.exception.cart;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(String message) {
        super(message);
    }

    public CartNotFoundException(Long cartId) {
        super("Cart not found with ID: " + cartId);
    }

    public CartNotFoundException(String phoneNumber, boolean activeOnly) {
        super(activeOnly
                ? "Active cart not found for phone number: " + phoneNumber
                : "Cart not found for phone number: " + phoneNumber);
    }
}
