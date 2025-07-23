package com.webstore.exception.cart;

public class InvalidProductIdException extends RuntimeException {

    public InvalidProductIdException(Integer productId) {
        super("Invalid product ID: " + productId);
    }

    public InvalidProductIdException(String message) {
        super(message);
    }

    public InvalidProductIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
