package com.webstore.exception.product;

public class CatalogueNotFoundException extends RuntimeException {
    public CatalogueNotFoundException(String message) {
        super(message);
    }
}
