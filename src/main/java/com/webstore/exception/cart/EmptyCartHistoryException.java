package com.webstore.exception.cart;

public class EmptyCartHistoryException extends CartHistoryException {

  public EmptyCartHistoryException(String message) {
    super(message);
  }
}
