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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class CartGlobalExceptionHandler {

//    Cart Status Exception Started

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

    @ExceptionHandler(CartProductNotFoundException.class)
    public ResponseEntity<String> handleCartProductStatusNotFoundException(
            CartProductNotFoundException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Product Status Not Found - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


//    Cart Product Status Exception Started

    @ExceptionHandler(CartProductStatusNotFoundException.class)
    public ResponseEntity<String> handleCartProductStatusNotFoundException(
            CartProductStatusNotFoundException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Product Status Not Found - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCartProductStatusException.class)
    public ResponseEntity<String> handleInvalidCartProductStatusException(
            InvalidCartProductStatusException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartProductStatusAlreadyExistsException.class)
    public ResponseEntity<String> handleCartProductStatusAlreadyExistsException(
            CartProductStatusAlreadyExistsException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Already Exists - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CartProductStatusInUseException.class)
    public ResponseEntity<String> handleCartProductStatusInUseException(
            CartProductStatusInUseException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status In Use - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CartProductStatusValidationException.class)
    public ResponseEntity<String> handleCartProductStatusValidationException(
            CartProductStatusValidationException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Validation Error - Request ID: {} - Error: {}", requestId, ex.getMessage());

        String errorMessage = ex.getMessage();
        if (ex.getValidationErrors() != null && !ex.getValidationErrors().isEmpty()) {
            errorMessage += ": " + String.join(", ", ex.getValidationErrors());
        }

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartProductStatusDatabaseException.class)
    public ResponseEntity<String> handleCartProductStatusDatabaseException(
            CartProductStatusDatabaseException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Database Error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>("An error occurred while processing cart status data", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CartProductStatusException.class)
    public ResponseEntity<String> handleCartProductStatusException(
            CartProductStatusException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Cart Status Error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    Cart Product History Exception Started

    @ExceptionHandler(CartProductHistoryNotFoundException.class)
    public ResponseEntity<String> handleCartProductHistoryNotFound(
            CartProductHistoryNotFoundException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCartProductHistoryRequestException.class)
    public ResponseEntity <String> handleInvalidRequest(
            InvalidCartProductHistoryRequestException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity <String> handleInvalidQuantity(
            InvalidQuantityException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateCartProductHistoryException.class)
    public ResponseEntity <String> handleDuplicateHistory(
            DuplicateCartProductHistoryException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity <String> handleInvalidDateRange(
            InvalidDateRangeException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(CartProductHistoryLimitExceededException.class)
    public ResponseEntity <String> handleLimitExceeded(
            CartProductHistoryLimitExceededException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return  new ResponseEntity<>(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }


    @ExceptionHandler(CartProductHistoryValidationException.class)
    public ResponseEntity <String> handleValidationError(
            CartProductHistoryValidationException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartProductHistoryDataIntegrityException.class)
    public ResponseEntity <String> handleDataIntegrity(
            CartProductHistoryDataIntegrityException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CartProductHistoryConcurrentModificationException.class)
    public ResponseEntity <String> handleConcurrentModification(
            CartProductHistoryConcurrentModificationException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CartProductHistoryServiceUnavailableException.class)
    public ResponseEntity <String> handleServiceUnavailable(
            CartProductHistoryServiceUnavailableException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(CartProductHistoryOperationNotAllowedException.class)
    public ResponseEntity <String> handleOperationNotAllowed(
            CartProductHistoryOperationNotAllowedException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(EmptyCartProductHistoryException.class)
    public ResponseEntity <String> handleEmptyHistory(
            EmptyCartProductHistoryException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(CartProductHistoryDatabaseException.class)
    public ResponseEntity <String> handleDatabaseError(
            CartProductHistoryDatabaseException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("Invalid Cart Status - Request ID: {} - Error: {}", requestId, ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    General Exceptions Written Here


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