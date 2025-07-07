package com.webstore.exception;

import com.webstore.exception.product.WhatsAppFlowException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        LOGGER.error("Webstore: EntityNotFoundException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Webstore: The requested resource was not found.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error("Webstore: IllegalArgumentException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webstore: Invalid input provided. Please check your request.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        LOGGER.error("Webstore: ResponseStatusException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatusCode())
                .body("Webstore: " + ex.getReason());
    }

    // ✅ Custom WhatsApp flow-related exceptions
    @ExceptionHandler(WhatsAppFlowException.class)
    public ResponseEntity<String> handleWhatsAppFlowException(WhatsAppFlowException ex) {
        LOGGER.warn("Webstore: WhatsApp flow exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webstore: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        LOGGER.error("Webstore: Unexpected exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Webstore: Something went wrong. Please try again later.");
    }
}
