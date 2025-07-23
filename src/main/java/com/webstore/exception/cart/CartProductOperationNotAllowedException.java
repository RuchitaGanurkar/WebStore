package com.webstore.exception.cart;

public class CartProductOperationNotAllowedException extends CartProductException {
    public CartProductOperationNotAllowedException(String message) {
        super(message);
    }
}
