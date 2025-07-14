package com.webstore.exception.cart;

public class CartProductStatusNotFoundException extends RuntimeException {
    public CartProductStatusNotFoundException(String message) {
        super(message);
    }


    public CartProductStatusNotFoundException(Integer statusId) {
        super("Cart status not found with id: " + statusId);
    }

}
