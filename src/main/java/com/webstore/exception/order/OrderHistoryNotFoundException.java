package com.webstore.exception.order;

public class OrderHistoryNotFoundException extends RuntimeException {

    public OrderHistoryNotFoundException(String message) {
        super(message);
    }

    public OrderHistoryNotFoundException(Long historyId) {
        super("Order history not found with ID: " + historyId);
    }

    public OrderHistoryNotFoundException(Long orderId, String statusName) {
        super("Order history not found for Order ID: " + orderId + " with status: " + statusName);
    }
}
