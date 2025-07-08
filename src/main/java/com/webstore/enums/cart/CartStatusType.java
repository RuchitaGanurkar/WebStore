package com.webstore.enums.cart;

public enum CartStatusType {
      ACTIVE("ACTIVE"),
      CHECKED_OUT("CHECKED_OUT"),
      PAID("PAID"),
      ARCHIVED("ARCHIVED");

      private final String value;

      CartStatusType(String value) {
            this.value = value;
      }

      public String getValue() {
            return value;
      }
}
