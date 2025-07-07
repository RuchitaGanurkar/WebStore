package com.webstore.exception.product;

public class ProductNotFoundException extends WhatsAppFlowException {
    public ProductNotFoundException(String name) {
        super("Product not found: " + name);
    }
}
