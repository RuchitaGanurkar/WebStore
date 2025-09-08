package com.webstore.exception.cart;

/**
 * Thrown when a cart cannot be found by ID or phone number.
 */
public class CartNotFoundException extends CartException {

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
