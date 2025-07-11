package com.webstore.exception.cart;

public class CartStatusInUseException extends CartStatusException {
    public CartStatusInUseException(String message) {
        super(message);
    }

    public CartStatusInUseException(Integer statusId) {
        super("Cannot delete cart status with id: " + statusId + " as it is currently in use");
    }
}
