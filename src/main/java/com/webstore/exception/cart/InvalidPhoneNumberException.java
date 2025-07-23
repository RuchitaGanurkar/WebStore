package com.webstore.exception.cart;

public class InvalidPhoneNumberException extends RuntimeException {

    public InvalidPhoneNumberException() {
        super("Invalid phone number");
    }

    public InvalidPhoneNumberException(String phoneNumber) {
        super("Invalid phone number: " + phoneNumber);
    }

    public InvalidPhoneNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
