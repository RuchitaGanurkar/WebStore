package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductHistory;
import com.webstore.repository.cart.CartProductHistoryRepository;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.service.cart.CartProductHistoryService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartProductHistoryServiceImplementation implements CartProductHistoryService {

    private final CartProductHistoryRepository cartProductHistoryRepository;
    private final CartProductRepository cartProductRepository;

    @Override
    @Transactional
    public CartProductHistoryResponseDto createCartProductHistory(CartProductHistoryRequestDto dto) {
        log.info("Creating cart product history for cart product ID: {}", dto != null ? dto.getCartProductId() : null);

        // Validate input
        validateCartProductHistoryRequest(dto);

        // Check if cart product exists
        CartProduct cartProduct = cartProductRepository.findById(dto.getCartProductId())
                .orElseThrow(() -> new RuntimeException("Cart product not found with ID: " + dto.getCartProductId()));

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
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartProductHistoryResponseDto> getAllCartProductHistory() {
        log.info("Fetching all cart product history records");

        List<CartProductHistory> histories = cartProductHistoryRepository.findAll();

        return histories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CartProductHistoryResponseDto getCartProductHistoryById(Long id) {
        log.info("Fetching cart product history with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("History ID cannot be null");
        }

        CartProductHistory history = cartProductHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart product history not found with ID: " + id));

        return convertToDto(history);
    }

    @Override
    public List<CartProductHistoryResponseDto> getHistoryByCartProductId(Long cartProductId) {
        return List.of();
    }

    @Override
    public CartProductHistoryResponseDto getLatestHistoryByCartProductId(Long cartProductId) {
        return null;
    }

    @Override
    public List<CartProductHistoryResponseDto> getHistoryByProductId(Integer productId) {
        return List.of();
    }

    @Override
    @Transactional
    public CartProductHistoryResponseDto updateCartProductHistory(Long id, CartProductHistoryRequestDto dto) {
        log.info("Updating cart product history with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("History ID cannot be null");
        }

        // Validate input
        validateCartProductHistoryRequest(dto);

        CartProductHistory history = cartProductHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart product history not found with ID: " + id));

        CartProduct cartProduct = cartProductRepository.findById(dto.getCartProductId())
                .orElseThrow(() -> new RuntimeException("Cart product not found with ID: " + dto.getCartProductId()));

        history.setCartProduct(cartProduct);
        history.setProductId(dto.getProductId());
        history.setOldQuantity(dto.getOldQuantity());
        history.setNewQuantity(dto.getNewQuantity());
        history.setUpdatedAt(LocalDateTime.now());

        CartProductHistory updated = cartProductHistoryRepository.save(history);
        log.info("Cart product history with ID: {} updated successfully", id);

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteCartProductHistory(Long id) {
        log.info("Deleting cart product history with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("History ID cannot be null");
        }

        CartProductHistory history = cartProductHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart product history not found with ID: " + id));

        cartProductHistoryRepository.delete(history);
        log.info("Cart product history with ID: {} has been deleted", id);
    }

    private void validateCartProductHistoryRequest(CartProductHistoryRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CartProductHistoryRequestDto cannot be null");
        }

        if (dto.getCartProductId() == null) {
            throw new IllegalArgumentException("Cart product ID cannot be null");
        }

        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        if (dto.getOldQuantity() == null) {
            throw new IllegalArgumentException("Old quantity cannot be null");
        }

        if (dto.getNewQuantity() == null) {
            throw new IllegalArgumentException("New quantity cannot be null");
        }

        if (dto.getOldQuantity() < 0 || dto.getNewQuantity() < 0) {
            throw new IllegalArgumentException("Quantities cannot be negative");
        }
    }

    private CartProductHistoryResponseDto convertToDto(CartProductHistory history) {
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