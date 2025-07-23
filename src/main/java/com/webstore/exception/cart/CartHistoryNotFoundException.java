package com.webstore.exception.cart;

public class CartHistoryNotFoundException extends CartHistoryException {

  public CartHistoryNotFoundException(Long id) {
    super("Cart history not found with ID: " + id);
  }

  public CartHistoryNotFoundException(String message) {
    super(message);
  }
}
