package com.webstore.exception.product;

public class WhatsAppFlowException extends RuntimeException {
    public WhatsAppFlowException(String message) {
        super(message);
    }

    public WhatsAppFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
