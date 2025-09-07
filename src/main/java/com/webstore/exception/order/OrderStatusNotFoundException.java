package com.webstore.exception.order;

public class OrderStatusNotFoundException extends RuntimeException {

    public OrderStatusNotFoundException(String message) {
        super(message);
    }

    public OrderStatusNotFoundException(Long statusId) {
        super("Order status not found with ID: " + statusId);
    }

    public OrderStatusNotFoundException(Enum<?> statusEnum) {
        super("Order status not found: " + statusEnum.name());
    }
}
