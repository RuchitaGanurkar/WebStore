package com.webstore.exception.cart;

/**
 * Base class for all cart-related exceptions.
 */
public class CartException extends RuntimeException {

  public CartException(String message) {
    super(message);
  }

  public CartException(String message, Throwable cause) {
    super(message, cause);
  }
}
