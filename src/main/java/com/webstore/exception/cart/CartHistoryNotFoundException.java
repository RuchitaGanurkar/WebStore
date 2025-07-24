package com.webstore.exception.cart;

public class CartHistoryNotFoundException extends RuntimeException {
    public CartHistoryNotFoundException(String message) {
        super(message);
    }
}
