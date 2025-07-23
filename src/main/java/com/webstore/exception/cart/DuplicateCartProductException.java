package com.webstore.exception.cart;

public class DuplicateCartProductException extends CartProductException {
    public DuplicateCartProductException(String message) {
        super(message);
    }
}
