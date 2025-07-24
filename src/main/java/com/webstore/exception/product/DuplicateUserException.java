package com.webstore.exception.product;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateUserException forUsername(String username) {
        return new DuplicateUserException("User already exists with username: " + username);
    }

    public static DuplicateUserException forEmail(String email) {
        return new DuplicateUserException("User already exists with email: " + email);
    }

    public static DuplicateUserException forPhoneNumber(String phoneNumber) {
        return new DuplicateUserException("User already exists with phone number: " + phoneNumber);
    }
}
