package com.webstore.exception.cart;

public class CartProductStatusAlreadyExistsException extends CartProductException {
    public CartProductStatusAlreadyExistsException(String message) {
        super(message);
    }


    public static CartProductStatusAlreadyExistsException forStatusName(String statusName) {
        return new CartProductStatusAlreadyExistsException("Cart status already exists with name: " + statusName);
    }
}
