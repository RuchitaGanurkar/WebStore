package com.webstore.exception.product;

public class CategoryNotFoundException extends WhatsAppFlowException {
    public CategoryNotFoundException(String name) {
        super("Category not found: " + name);
    }
}
