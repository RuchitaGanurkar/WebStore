package com.webstore.exception.cart;

public class CartStatusAlreadyExistsException extends CartStatusException {
    public CartStatusAlreadyExistsException(String message) {
        super(message);
    }

    public static CartStatusAlreadyExistsException forStatusName(String statusName) {
        return new CartStatusAlreadyExistsException("Cart status already exists with name: " + statusName);
    }
}