package com.webstore.exception.cart;

import java.time.LocalDateTime;

public class InvalidDateRangeException extends CartProductHistoryException {
    public InvalidDateRangeException(LocalDateTime startDate, LocalDateTime endDate) {
        super("Invalid date range: start date " + startDate + " must be before end date " + endDate);
    }

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
