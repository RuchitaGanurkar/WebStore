package com.webstore.exception;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException() {
    super("Requested resource was not found");
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
