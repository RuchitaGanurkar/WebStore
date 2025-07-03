package com.webstore.enums.whatsapp;

public enum MessageType {
    TEXT("text"),
    INTERACTIVE_BUTTON("interactive"),
    INTERACTIVE_LIST("interactive"),
    TEMPLATE("template");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
