package com.webstore.enums.cart;

public enum CartProductStatusType {
    ADDED("ADDED"),
    REMOVED("REMOVED");

    private final String value;

    CartProductStatusType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
