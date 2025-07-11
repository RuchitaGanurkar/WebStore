package com.webstore.exception.cart;

public class CartStatusException extends RuntimeException {

    public CartStatusException(String message) {
        super(message);
    }

    public CartStatusException(String message, Throwable cause) {
        super(message, cause);
    }

}
