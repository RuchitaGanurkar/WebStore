package com.webstore.exception.cart;

public class CartProductNotFoundException extends CartProductException {

  public CartProductNotFoundException() {
    super("Cart product not found");
  }

  public CartProductNotFoundException(Long id) {
    super("Cart product with ID " + id + " not found");
  }

  public CartProductNotFoundException(String message) {
    super(message);
  }
}
