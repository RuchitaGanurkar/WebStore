package com.webstore.exception;

public class ProductNotFoundException extends WhatsAppFlowException {
    public ProductNotFoundException(String name) {
        super("Product not found: " + name);
    }
}
