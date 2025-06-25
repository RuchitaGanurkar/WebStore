package com.webstore.exception;

public class CategoryNotFoundException extends WhatsAppFlowException {
    public CategoryNotFoundException(String name) {
        super("Category not found: " + name);
    }
}
