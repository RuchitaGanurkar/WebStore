package com.webstore.exception.product;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }


    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Integer userId) {
        super("User not found with ID: " + userId);
    }

    public static UserNotFoundException forId(Integer userId) {
        return new UserNotFoundException("User not found with ID: " + userId);
    }

    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User not found with username: " + username);
    }

    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }
}
