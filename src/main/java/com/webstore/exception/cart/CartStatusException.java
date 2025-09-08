package com.webstore.exception.cart;

/**
 * Thrown when there are issues related to cart status.
 */
public class CartStatusException extends CartException {

    public CartStatusException(String message) {
        super(message);
    }

    public CartStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
