package com.webstore.exception.cart;

public class CartProductStatusInUseException extends RuntimeException {
    public CartProductStatusInUseException(String message) {
        super(message);
    }

    public CartProductStatusInUseException(Integer statusId) {
        super("Cannot delete cart status with id: " + statusId + " as it is currently in use");
    }
}
