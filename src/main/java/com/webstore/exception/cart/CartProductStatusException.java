package com.webstore.exception.cart;

public class CartProductStatusException extends RuntimeException {
    public CartProductStatusException(String message) {
        super(message);
    }


    public CartProductStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
