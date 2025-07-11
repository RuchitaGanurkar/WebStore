package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartStatusRequestDto;
import com.webstore.dto.response.cart.CartStatusResponseDto;
import com.webstore.entity.cart.CartStatus;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.exception.cart.*;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.service.cart.CartStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartStatusServiceImplementation implements CartStatusService {

    private final CartStatusRepository cartStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public CartStatusResponseDto getCartStatusById(Integer statusId) {
        log.debug("Fetching cart status with id: {}", statusId);

        if (statusId == null || statusId <= 0) {
            throw new InvalidCartStatusException("statusId", String.valueOf(statusId));
        }

        try {
            CartStatus cartStatus = cartStatusRepository.findById(statusId)
                    .orElseThrow(() -> new CartStatusNotFoundException(statusId));

            log.debug("Successfully fetched cart status with id: {}", statusId);
            return mapToResponseDto(cartStatus);
        } catch (DataAccessException ex) {
            log.error("Database error while fetching cart status with id: {}", statusId, ex);
            throw new CartStatusDatabaseException("Failed to fetch cart status with id: " + statusId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartStatusResponseDto> getAllCartStatuses() {
        log.debug("Fetching all cart statuses");

        try {
            List<CartStatus> statuses = cartStatusRepository.findAll();
            log.debug("Successfully fetched {} cart statuses", statuses.size());

            return statuses.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching all cart statuses", ex);
            throw new CartStatusDatabaseException("Failed to fetch cart statuses", ex);
        }
    }

    @Override
    public CartStatusResponseDto createCartStatus(CartStatusRequestDto requestDto) {
        log.debug("Creating new cart status with name: {}", requestDto.getStatusName());

        validateCartStatusRequest(requestDto);

        try {
             CartStatusType statusType;
            try {
                statusType = CartStatusType.valueOf(requestDto.getStatusName().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new InvalidCartStatusException("statusName", requestDto.getStatusName());
            }

            if (cartStatusRepository.existsByStatusName(statusType)) {
                throw CartStatusAlreadyExistsException.forStatusName(requestDto.getStatusName());
            }
            CartStatus cartStatus = new CartStatus();
            cartStatus.setStatusName(statusType);
            cartStatus.setCreatedAt(LocalDateTime.now());
            cartStatus.setUpdatedAt(LocalDateTime.now());

            CartStatus savedStatus = cartStatusRepository.save(cartStatus);
            log.debug("Successfully created cart status with id: {}", savedStatus.getStatusId());

            return mapToResponseDto(savedStatus);

        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while creating cart status", ex);
            throw CartStatusAlreadyExistsException.forStatusName(requestDto.getStatusName());
        } catch (DataAccessException ex) {
            log.error("Database error while creating cart status", ex);
            throw new CartStatusDatabaseException("Failed to create cart status", ex);
        }
    }

    @Override
    public CartStatusResponseDto updateCartStatus(Integer statusId, CartStatusRequestDto requestDto) {
        log.debug("Updating cart status with id: {} to name: {}", statusId, requestDto.getStatusName());

        if (statusId == null || statusId <= 0) {
            throw new InvalidCartStatusException("statusId", String.valueOf(statusId));
        }

        validateCartStatusRequest(requestDto);

        try {
            CartStatus cartStatus = cartStatusRepository.findById(statusId)
                    .orElseThrow(() -> new CartStatusNotFoundException(statusId));

            CartStatusType statusType;
            try {
                statusType = CartStatusType.valueOf(requestDto.getStatusName().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new InvalidCartStatusException("statusName", requestDto.getStatusName());
            }

            if (cartStatusRepository.existsByStatusNameAndStatusIdNot(statusType, statusId)) {
                throw CartStatusAlreadyExistsException.forStatusName(requestDto.getStatusName());
            }

            cartStatus.setStatusName(statusType);
            cartStatus.setUpdatedAt(LocalDateTime.now());

            CartStatus updatedStatus = cartStatusRepository.save(cartStatus);
            log.debug("Successfully updated cart status with id: {}", statusId);

            return mapToResponseDto(updatedStatus);

        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while updating cart status with id: {}", statusId, ex);
            throw CartStatusAlreadyExistsException.forStatusName(requestDto.getStatusName());
        } catch (DataAccessException ex) {
            log.error("Database error while updating cart status with id: {}", statusId, ex);
            throw new CartStatusDatabaseException("Failed to update cart status with id: " + statusId, ex);
        }
    }

    @Override
    public void deleteCartStatus(Integer statusId) {
        log.debug("Deleting cart status with id: {}", statusId);

        if (statusId == null || statusId <= 0) {
            throw new InvalidCartStatusException("statusId", String.valueOf(statusId));
        }

        try {
            if (!cartStatusRepository.existsById(statusId)) {
                throw new CartStatusNotFoundException(statusId);
            }

            if (isCartStatusInUse(statusId)) {
                throw new CartStatusInUseException(statusId);
            }

            cartStatusRepository.deleteById(statusId);
            log.debug("Successfully deleted cart status with id: {}", statusId);

        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while deleting cart status with id: {}", statusId, ex);
            throw new CartStatusInUseException(statusId);
        } catch (DataAccessException ex) {
            log.error("Database error while deleting cart status with id: {}", statusId, ex);
            throw new CartStatusDatabaseException("Failed to delete cart status with id: " + statusId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartStatusResponseDto getCartStatusByName(String statusName) {
        log.debug("Fetching cart status with name: {}", statusName);

        if (statusName == null || statusName.trim().isEmpty()) {
            throw new InvalidCartStatusException("statusName", statusName);
        }

        try {
            CartStatusType statusType;
            try {
                statusType = CartStatusType.valueOf(statusName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new InvalidCartStatusException("statusName", statusName);
            }

            CartStatus cartStatus = cartStatusRepository.findByStatusName(statusType)
                    .orElseThrow(() -> new CartStatusNotFoundException("Cart status not found with name: " + statusName));

            log.debug("Successfully fetched cart status with name: {}", statusName);
            return mapToResponseDto(cartStatus);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching cart status with name: {}", statusName, ex);
            throw new CartStatusDatabaseException("Failed to fetch cart status with name: " + statusName, ex);
        }
    }

    private void validateCartStatusRequest(CartStatusRequestDto requestDto) {
        List<String> validationErrors = new ArrayList<>();

        if (requestDto == null) {
            throw new CartStatusValidationException("Cart status request cannot be null");
        }

        if (requestDto.getStatusName() == null || requestDto.getStatusName().trim().isEmpty()) {
            validationErrors.add("Status name is required");
        } else {
            // Validate that the status name is a valid enum value
            try {
                CartStatusType.valueOf(requestDto.getStatusName().toUpperCase());
            } catch (IllegalArgumentException ex) {
                List<String> validValues = Arrays.stream(CartStatusType.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());
                validationErrors.add("Invalid status name. Valid values are: " + String.join(", ", validValues));
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new CartStatusValidationException("Validation failed for cart status request", validationErrors);
        }
    }

    private boolean isCartStatusInUse(Integer statusId) {
        try {
             return cartStatusRepository.countCartsUsingStatus(statusId) > 0;
        } catch (Exception ex) {
            log.warn("Could not check if cart status is in use, assuming it's not in use", ex);
            return false;
        }
    }

    private CartStatusResponseDto mapToResponseDto(CartStatus cartStatus) {
        if (cartStatus == null) {
            return null;
        }

        CartStatusResponseDto responseDto = new CartStatusResponseDto();
        responseDto.setStatusId(cartStatus.getStatusId());
        responseDto.setStatusName(cartStatus.getStatusName().name());
        responseDto.setCreatedAt(cartStatus.getCreatedAt());
        responseDto.setUpdatedAt(cartStatus.getUpdatedAt());
        return responseDto;
    }
}