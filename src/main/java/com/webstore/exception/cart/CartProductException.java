package com.webstore.exception.cart;

public class CartProductException extends RuntimeException {

  public CartProductException(String message) {
    super(message);
  }

  public CartProductException(String message, Throwable cause) {
    super(message, cause);
  }

  public CartProductException(Throwable cause) {
    super("An error occurred with the cart product", cause);
  }
}
