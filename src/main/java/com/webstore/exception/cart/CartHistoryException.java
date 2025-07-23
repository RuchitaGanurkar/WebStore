package com.webstore.exception.cart;

public abstract class CartHistoryException extends RuntimeException {

  public CartHistoryException(String message) {
    super(message);
  }

  public CartHistoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
