package com.webstore.exception;

/**
 * Base exception for all WhatsApp flow-related issues.
 */
public class WhatsAppFlowException extends RuntimeException {
    public WhatsAppFlowException(String message) {
        super(message);
    }

    public WhatsAppFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
