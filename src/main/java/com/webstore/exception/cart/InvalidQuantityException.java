package com.webstore.exception.cart;

public class InvalidQuantityException extends CartProductHistoryException {
    public InvalidQuantityException(String message) {
        super("Invalid quantity: " + message);
    }

    public InvalidQuantityException(Integer oldQuantity, Integer newQuantity) {
        super("Invalid quantity change from " + oldQuantity + " to " + newQuantity);
    }
}