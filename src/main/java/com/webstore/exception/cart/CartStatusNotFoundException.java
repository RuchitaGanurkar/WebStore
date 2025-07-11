package com.webstore.exception.cart;

public class CartStatusNotFoundException extends CartStatusException {
    public CartStatusNotFoundException(String message) {
        super(message);
    }

    public CartStatusNotFoundException(Integer statusId) {
        super("Cart status not found with id: " + statusId);
    }

}
