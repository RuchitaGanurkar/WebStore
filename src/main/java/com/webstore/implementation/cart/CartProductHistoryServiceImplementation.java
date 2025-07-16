package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductHistory;
import com.webstore.exception.cart.*;
import com.webstore.repository.cart.CartProductHistoryRepository;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.service.cart.CartProductHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartProductHistoryServiceImplementation implements CartProductHistoryService {
    private final CartProductHistoryRepository cartProductHistoryRepository;
    private final CartProductRepository cartProductRepository;

    private static final int MAX_HISTORY_RECORDS_PER_CART_PRODUCT = 1000;
    private static final String PHONE_NUMBER_REGEX = "^[+]?[1-9]\\d{1,14}$";

    @Override
    @Transactional
    public CartProductHistoryResponseDto createCartProductHistory(CartProductHistoryRequestDto dto) {
        log.info("Creating cart product history for cart product ID: {}", dto.getCartProductId());

        try {
            // Validate input
            validateCartProductHistoryRequest(dto);

            // Check if cart product exists
            CartProduct cartProduct = cartProductRepository.findById(dto.getCartProductId())
                    .orElseThrow(() -> new CartProductNotFoundException(dto.getCartProductId()));

            // Check for existing history records limit
            checkHistoryLimit(dto.getCartProductId());

            // Validate quantity changes
            validateQuantityChange(dto.getOldQuantity(), dto.getNewQuantity());

            // Check for duplicate history within a time window (optional business rule)
            checkForDuplicateHistory(dto);

            CartProductHistory history = new CartProductHistory();
            history.setCartProduct(cartProduct);
            history.setProductId(dto.getProductId());
            history.setOldQuantity(dto.getOldQuantity());
            history.setNewQuantity(dto.getNewQuantity());
            history.setCreatedAt(LocalDateTime.now());
            history.setUpdatedAt(LocalDateTime.now());

            CartProductHistory saved = cartProductHistoryRepository.save(history);
            log.info("Cart product history created with ID: {}", saved.getCartProductHistoryId());

            return convertToDto(saved);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating cart product history", e);
            throw new CartProductHistoryDataIntegrityException("Failed to create cart product history due to data integrity violation", e);
        } catch (Exception e) {
            log.error("Unexpected error while creating cart product history", e);
            throw new CartProductHistoryServiceUnavailableException("Failed to create cart product history", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartProductHistoryResponseDto> getAllCartProductHistory() {
        log.info("Fetching all cart product history records");

        try {
            List<CartProductHistory> histories = cartProductHistoryRepository.findAll();

            if (histories.isEmpty()) {
                throw new EmptyCartProductHistoryException("No cart product history records found");
            }

            return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            if (e instanceof CartProductHistoryException) {
                throw e;
            }
            log.error("Unexpected error while fetching all cart product history", e);
            throw new CartProductHistoryDatabaseException("Failed to fetch cart product history records", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartProductHistoryResponseDto getCartProductHistoryById(Long id) {
        log.info("Fetching cart product history with ID: {}", id);

        try {
            if (id == null || id <= 0) {
                throw new InvalidCartProductHistoryRequestException("Cart product history ID must be positive");
            }

            CartProductHistory history = cartProductHistoryRepository.findById(id)
                    .orElseThrow(() -> new CartProductHistoryNotFoundException(id));

            return convertToDto(history);

        } catch (Exception e) {
            if (e instanceof CartProductHistoryException) {
                throw e;
            }
            log.error("Unexpected error while fetching cart product history with ID: {}", id, e);
            throw new CartProductHistoryDatabaseException("Failed to fetch cart product history", e);
        }
    }

    @Override
    @Transactional
    public CartProductHistoryResponseDto updateCartProductHistory(Long id, CartProductHistoryRequestDto dto) {
        log.info("Updating cart product history with ID: {}", id);

        try {
            if (id == null || id <= 0) {
                throw new InvalidCartProductHistoryRequestException("Cart product history ID must be positive");
            }

            // Validate input
            validateCartProductHistoryRequest(dto);

            CartProductHistory history = cartProductHistoryRepository.findById(id)
                    .orElseThrow(() -> new CartProductHistoryNotFoundException(id));

            CartProduct cartProduct = cartProductRepository.findById(dto.getCartProductId())
                    .orElseThrow(() -> new CartProductNotFoundException(dto.getCartProductId()));

            // Validate quantity changes
            validateQuantityChange(dto.getOldQuantity(), dto.getNewQuantity());

            // Check if update is allowed (business rule)
            validateUpdateAllowed(history);

            history.setCartProduct(cartProduct);
            history.setProductId(dto.getProductId());
            history.setOldQuantity(dto.getOldQuantity());
            history.setNewQuantity(dto.getNewQuantity());
            history.setUpdatedAt(LocalDateTime.now());

            CartProductHistory updated = cartProductHistoryRepository.save(history);
            log.info("Cart product history with ID: {} updated successfully", id);

            return convertToDto(updated);

        } catch (OptimisticLockingFailureException e) {
            log.error("Concurrent modification detected for cart product history ID: {}", id, e);
            throw new CartProductHistoryConcurrentModificationException(id);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating cart product history", e);
            throw new CartProductHistoryDataIntegrityException("Failed to update cart product history due to data integrity violation", e);
        } catch (Exception e) {
            if (e instanceof CartProductHistoryException) {
                throw e;
            }
            log.error("Unexpected error while updating cart product history with ID: {}", id, e);
            throw new CartProductHistoryServiceUnavailableException("Failed to update cart product history", e);
        }
    }

    @Override
    @Transactional
    public void deleteCartProductHistory(Long id) {
        log.info("Deleting cart product history with ID: {}", id);

        try {
            if (id == null || id <= 0) {
                throw new InvalidCartProductHistoryRequestException("Cart product history ID must be positive");
            }

            CartProductHistory history = cartProductHistoryRepository.findById(id)
                    .orElseThrow(() -> new CartProductHistoryNotFoundException(id));

            // Check if deletion is allowed (business rule)
            validateDeleteAllowed(history);

            cartProductHistoryRepository.delete(history);
            log.info("Cart product history with ID: {} has been deleted", id);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while deleting cart product history", e);
            throw new CartProductHistoryDataIntegrityException("Failed to delete cart product history due to data integrity violation", e);
        } catch (Exception e) {
            if (e instanceof CartProductHistoryException) {
                throw e;
            }
            log.error("Unexpected error while deleting cart product history with ID: {}", id, e);
            throw new CartProductHistoryServiceUnavailableException("Failed to delete cart product history", e);
        }
    }

    // Additional method to get history by cart product ID
    public List<CartProductHistoryResponseDto> getHistoryByCartProductId(Long cartProductId) {
        log.info("Fetching cart product history for cart product ID: {}", cartProductId);

        try {
            if (cartProductId == null || cartProductId <= 0) {
                throw new InvalidCartProductHistoryRequestException("Cart product ID must be positive");
            }

            List<CartProductHistory> histories = cartProductHistoryRepository
                    .findByCartProductIdOrderByCreatedAtDesc(cartProductId);

            if (histories.isEmpty()) {
                throw new EmptyCartProductHistoryException("No history found for cart product ID: " + cartProductId);
            }

            return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            if (e instanceof CartProductHistoryException) {
                throw e;
            }
            log.error("Unexpected error while fetching history for cart product ID: {}", cartProductId, e);
            throw new CartProductHistoryDatabaseException("Failed to fetch cart product history", e);
        }
    }

    // Additional method to get history by phone number
    public List<CartProductHistoryResponseDto> getHistoryByPhoneNumber(String phoneNumber) {
        log.info("Fetching cart product history for phone number: {}", phoneNumber);

        try {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new InvalidPhoneNumberException("Phone number cannot be null or empty");
            }

            if (!phoneNumber.matches(PHONE_NUMBER_REGEX)) {
                throw new InvalidPhoneNumberException(phoneNumber);
            }

            List<CartProductHistory> histories = cartProductHistoryRepository
                    .findByPhoneNumberOrderByCreatedAtDesc(phoneNumber);

            if (histories.isEmpty()) {
                throw new EmptyCartProductHistoryException("No history found for phone number: " + phoneNumber);
            }

            return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            if (e instanceof CartProductHistoryException) {
                throw e;
            }
            log.error("Unexpected error while fetching history for phone number: {}", phoneNumber, e);
            throw new CartProductHistoryDatabaseException("Failed to fetch cart product history", e);
        }
    }

    private void validateCartProductHistoryRequest(CartProductHistoryRequestDto dto) {
        if (dto == null) {
            throw new InvalidCartProductHistoryRequestException("Request cannot be null");
        }

        if (dto.getCartProductId() == null || dto.getCartProductId() <= 0) {
            throw new InvalidCartProductHistoryRequestException("cartProductId", dto.getCartProductId());
        }

        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new InvalidProductIdException(dto.getProductId());
        }

        if (dto.getOldQuantity() == null || dto.getOldQuantity() < 0) {
            throw new InvalidQuantityException("Old quantity cannot be null or negative");
        }

        if (dto.getNewQuantity() == null || dto.getNewQuantity() < 0) {
            throw new InvalidQuantityException("New quantity cannot be null or negative");
        }
    }

    private void validateQuantityChange(Integer oldQuantity, Integer newQuantity) {
        if (Objects.equals(oldQuantity, newQuantity)) {
            throw new InvalidQuantityException("Old quantity and new quantity cannot be the same");
        }

        // Business rule: quantity change cannot exceed certain limit
        int maxQuantityChange = 100;
        if (Math.abs(newQuantity - oldQuantity) > maxQuantityChange) {
            throw new InvalidQuantityException("Quantity change exceeds maximum allowed limit of " + maxQuantityChange);
        }
    }

    private void checkHistoryLimit(Long cartProductId) {
        List<CartProductHistory> existingHistories = cartProductHistoryRepository
                .findByCartProductCartProductId(cartProductId);

        if (existingHistories.size() >= MAX_HISTORY_RECORDS_PER_CART_PRODUCT) {
            throw new CartProductHistoryLimitExceededException(MAX_HISTORY_RECORDS_PER_CART_PRODUCT);
        }
    }

    private void checkForDuplicateHistory(CartProductHistoryRequestDto dto) {
        // Check for duplicate history records within the last 5 minutes (business rule)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<CartProductHistory> recentHistories = cartProductHistoryRepository
                .findByProductIdAndDateRange(dto.getProductId(), fiveMinutesAgo, LocalDateTime.now());

        boolean duplicateExists = recentHistories.stream()
                .anyMatch(h -> h.getCartProduct().getCartProductId().equals(dto.getCartProductId()) &&
                        h.getOldQuantity().equals(dto.getOldQuantity()) &&
                        h.getNewQuantity().equals(dto.getNewQuantity()));

        if (duplicateExists) {
            throw new DuplicateCartProductHistoryException(dto.getCartProductId(), dto.getProductId());
        }
    }

    private void validateUpdateAllowed(CartProductHistory history) {
        // Business rule: cannot update history older than 24 hours
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        if (history.getCreatedAt().isBefore(twentyFourHoursAgo)) {
            throw new CartProductHistoryOperationNotAllowedException("update",
                    "Cannot update history records older than 24 hours");
        }
    }

    private void validateDeleteAllowed(CartProductHistory history) {
        // Business rule: cannot delete history older than 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        if (history.getCreatedAt().isBefore(sevenDaysAgo)) {
            throw new CartProductHistoryOperationNotAllowedException("delete",
                    "Cannot delete history records older than 7 days");
        }
    }

    private CartProductHistoryResponseDto convertToDto(CartProductHistory history) {
        if (history == null) {
            throw new CartProductHistoryDataIntegrityException("Cannot convert null history to DTO");
        }

        CartProductHistoryResponseDto dto = new CartProductHistoryResponseDto();
        dto.setCartProductHistoryId(history.getCartProductHistoryId());
        dto.setCartProductId(history.getCartProduct().getCartProductId());
        dto.setProductId(history.getProductId());
        dto.setOldQuantity(history.getOldQuantity());
        dto.setNewQuantity(history.getNewQuantity());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setCreatedBy(history.getCreatedBy());
        dto.setUpdatedAt(history.getUpdatedAt());
        dto.setUpdatedBy(history.getUpdatedBy());
        return dto;
    }
}