package com.webstore.exception.cart.handler;

import com.webstore.exception.cart.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class CartGlobalExceptionHandler {

    @ExceptionHandler(CartStatusNotFoundException.class)
    public ResponseEntity<String> handleCartStatusNotFoundException(
            CartStatusNotFoundException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Not Found - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCartStatusException.class)
    public ResponseEntity<String> handleInvalidCartStatusException(
            InvalidCartStatusException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartStatusAlreadyExistsException.class)
    public ResponseEntity<String> handleCartStatusAlreadyExistsException(
            CartStatusAlreadyExistsException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Already Exists - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CartStatusInUseException.class)
    public ResponseEntity<String> handleCartStatusInUseException(
            CartStatusInUseException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status In Use - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CartStatusValidationException.class)
    public ResponseEntity<String> handleCartStatusValidationException(
            CartStatusValidationException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Validation Error - Request ID: {} - Error: {}", requestId, ex.getMessage());

        String errorMessage = ex.getMessage();
        if (ex.getValidationErrors() != null && !ex.getValidationErrors().isEmpty()) {
            errorMessage += ": " + String.join(", ", ex.getValidationErrors());
        }

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartStatusDatabaseException.class)
    public ResponseEntity<String> handleCartStatusDatabaseException(
            CartStatusDatabaseException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Database Error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>("An error occurred while processing cart status data", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CartStatusException.class)
    public ResponseEntity<String> handleCartStatusException(
            CartStatusException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Validation Error - Request ID: {} - Error: {}", requestId, ex.getMessage());

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = "Validation failed: " + String.join(", ", validationErrors);

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Data Integrity Violation - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>("The operation violates data integrity constraints", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Data Access Error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>("An error occurred while accessing the database", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Unexpected Error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>("An unexpected error occurred. Please contact support if the problem persists.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}