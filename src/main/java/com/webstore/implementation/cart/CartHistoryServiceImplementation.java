package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartHistoryRequestDto;
import com.webstore.dto.response.cart.CartHistoryResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartHistory;
import com.webstore.entity.cart.CartStatus;
import com.webstore.exception.cart.*;
import com.webstore.repository.cart.CartHistoryRepository;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.service.cart.CartHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartHistoryServiceImplementation implements CartHistoryService {

    private final CartHistoryRepository cartHistoryRepository;
    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;

    @Override
    public CartHistoryResponseDto createCartHistory(CartHistoryRequestDto requestDto) {
        try {
            Cart cart = cartRepository.findById(requestDto.getCartId())
                    .orElseThrow(() -> new CartHistoryValidationException(
                            "Cart with ID " + requestDto.getCartId() + " not found"));

            CartStatus oldStatus = cartStatusRepository.findById(requestDto.getOldStatusId())
                    .orElseThrow(() -> new CartHistoryValidationException(
                            "Old status with ID " + requestDto.getOldStatusId() + " not found"));

            CartStatus newStatus = cartStatusRepository.findById(requestDto.getNewStatusId())
                    .orElseThrow(() -> new CartHistoryValidationException(
                            "New status with ID " + requestDto.getNewStatusId() + " not found"));

            CartHistory history = new CartHistory();
            history.setCart(cart);
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);

            CartHistory saved = cartHistoryRepository.save(history);
            return mapToDto(saved);

        } catch (CartHistoryValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while creating cart history", ex);
        }
    }

    @Override
    public CartHistoryResponseDto getCartHistoryById(Long id) {
        try {
            CartHistory history = cartHistoryRepository.findById(id)
                    .orElseThrow(() -> new CartHistoryNotFoundException(
                            "CartHistory with ID " + id + " not found"));
            return mapToDto(history);
        } catch (CartHistoryNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart history by ID", ex);
        }
    }

    @Override
    public List<CartHistoryResponseDto> getCartHistoriesByCartId(Long cartId) {
        try {
            List<CartHistory> historyList = cartHistoryRepository.findByCartIdOrderByCreatedAtDesc(cartId);
            if (historyList.isEmpty()) {
                throw new EmptyCartHistoryException("No cart history found for cart ID: " + cartId);
            }
            return historyList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (EmptyCartHistoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart histories by cart ID", ex);
        }
    }

    @Override
    public List<CartHistoryResponseDto> getCartHistoriesByCreatedBy(String createdBy) {
        try {
            List<CartHistory> historyList = cartHistoryRepository.findByCreatedBy(createdBy);
            if (historyList.isEmpty()) {
                throw new EmptyCartHistoryException("No cart history found for createdBy: " + createdBy);
            }
            return historyList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (EmptyCartHistoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart histories by createdBy", ex);
        }
    }

    @Override
    public List<CartHistoryResponseDto> getCartHistoriesByUpdatedBy(String updatedBy) {
        try {
            List<CartHistory> historyList = cartHistoryRepository.findByUpdatedBy(updatedBy);
            if (historyList.isEmpty()) {
                throw new EmptyCartHistoryException("No cart history found for updatedBy: " + updatedBy);
            }
            return historyList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (EmptyCartHistoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart histories by updatedBy", ex);
        }
    }

    @Override
    public List<CartHistoryResponseDto> getCartHistoriesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<CartHistory> historyList = cartHistoryRepository.findByDateRange(startDate, endDate);
            if (historyList.isEmpty()) {
                throw new EmptyCartHistoryException("No cart history found between " + startDate + " and " + endDate);
            }
            return historyList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (EmptyCartHistoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart histories by date range", ex);
        }
    }

    @Override
    public List<CartHistoryResponseDto> getCartHistoriesByOldStatus(Integer oldStatusId) {
        try {
            List<CartHistory> historyList = cartHistoryRepository.findByOldStatusStatusId(oldStatusId);
            if (historyList.isEmpty()) {
                throw new EmptyCartHistoryException("No cart history found for oldStatusId: " + oldStatusId);
            }
            return historyList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (EmptyCartHistoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart histories by oldStatusId", ex);
        }
    }

    @Override
    public List<CartHistoryResponseDto> getCartHistoriesByNewStatus(Integer newStatusId) {
        try {
            List<CartHistory> historyList = cartHistoryRepository.findByNewStatusStatusId(newStatusId);
            if (historyList.isEmpty()) {
                throw new EmptyCartHistoryException("No cart history found for newStatusId: " + newStatusId);
            }
            return historyList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (EmptyCartHistoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CartHistoryDatabaseException("Error while fetching cart histories by newStatusId", ex);
        }
    }

    private CartHistoryResponseDto mapToDto(CartHistory entity) {
        CartHistoryResponseDto dto = new CartHistoryResponseDto();
        dto.setCartHistoryId(entity.getCartHistoryId());
        dto.setCartId(entity.getCart().getCartId());
        dto.setOldStatusId(entity.getOldStatus() != null ? entity.getOldStatus().getStatusId() : null);
        dto.setNewStatusId(entity.getNewStatus() != null ? entity.getNewStatus().getStatusId() : null);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }
}
