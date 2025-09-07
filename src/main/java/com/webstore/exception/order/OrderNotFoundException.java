package com.webstore.exception.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }

    public OrderNotFoundException(String fieldName, String value) {
        super("Order not found with " + fieldName + ": " + value);
    }
}
