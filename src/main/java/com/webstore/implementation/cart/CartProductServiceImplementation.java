package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.repository.cart.CartProductStatusRepository;
import com.webstore.repository.cart.CartRepository;
import com.webstore.service.cart.CartProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartProductServiceImplementation implements CartProductService {

    private static final Logger logger = LoggerFactory.getLogger(CartProductServiceImplementation.class);

    private final CartProductRepository cartProductRepository;
    private final CartProductStatusRepository cartProductStatusRepository;
    private final CartRepository cartRepository;

    public CartProductServiceImplementation(CartProductRepository cartProductRepository,
                                  CartProductStatusRepository cartProductStatusRepository,
                                  CartRepository cartRepository) {
        this.cartProductRepository = cartProductRepository;
        this.cartProductStatusRepository = cartProductStatusRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public CartProductResponseDto createCartProduct(CartProductRequestDto requestDto) {
        logger.info("Creating cart product for cart ID: {}", requestDto.getCartId());

        try {
            // Validate cart exists
            Cart cart = cartRepository.findById(requestDto.getCartId())
                    .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + requestDto.getCartId()));

            // Validate status exists
            CartProductStatus status = cartProductStatusRepository.findById(requestDto.getStatusId())
                    .orElseThrow(() -> new RuntimeException("Cart product status not found with ID: " + requestDto.getStatusId()));

            // Create CartProduct entity
            CartProduct cartProduct = new CartProduct();
            cartProduct.setCart(cart);
            cartProduct.setStatus(status);

            // Save to database
            CartProduct savedCartProduct = cartProductRepository.save(cartProduct);

            // Convert to DTO and return
            return convertToResponseDto(savedCartProduct);

        } catch (Exception e) {
            logger.error("Error creating cart product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create cart product: " + e.getMessage(), e);
        }
    }

    @Override
    public CartProductResponseDto getCartProductById(Long id) {
        logger.debug("Getting cart product by ID: {}", id);

        try {
            CartProduct cartProduct = cartProductRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cart product not found with ID: " + id));

            return convertToResponseDto(cartProduct);

        } catch (Exception e) {
            logger.error("Error getting cart product by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get cart product: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CartProductResponseDto> getCartProductsByCartId(Long cartId) {
        logger.debug("Getting cart products for cart ID: {}", cartId);

        try {
            List<CartProduct> cartProducts = cartProductRepository.findByCartCartId(cartId);

            return cartProducts.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting cart products for cart ID {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to get cart products: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CartProductResponseDto> getActiveCartProductsByPhoneNumber(String phoneNumber) {
        logger.debug("Getting active cart products for phone number: {}", phoneNumber);

        try {
            // Get active status
            CartProductStatus activeStatus = cartProductStatusRepository
                    .findByStatusName(String.valueOf(CartProductStatusType.ADDED))
                    .orElseThrow(() -> new RuntimeException("Active cart product status not found"));

            // Find cart products by phone number and active status
            List<CartProduct> cartProducts = cartProductRepository
                    .findByCartPhoneNumberAndStatus(phoneNumber, activeStatus);

            return cartProducts.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting active cart products for phone number {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to get active cart products: " + e.getMessage(), e);
        }
    }

    @Override
    public Long countActiveCartProducts(Long cartId) {
        logger.debug("Counting active cart products for cart ID: {}", cartId);

        try {
            // Get active status
            CartProductStatus activeStatus = cartProductStatusRepository
                    .findByStatusName(String.valueOf(CartProductStatusType.ADDED))
                    .orElseThrow(() -> new RuntimeException("Active cart product status not found"));

            return cartProductRepository.countByCartCartIdAndStatus(cartId, activeStatus);

        } catch (Exception e) {
            logger.error("Error counting active cart products for cart ID {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to count active cart products: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCartProduct(Long id) {
        logger.info("Deleting cart product with ID: {}", id);

        try {
            CartProduct cartProduct = cartProductRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cart product not found with ID: " + id));

            cartProductRepository.delete(cartProduct);
            logger.info("Cart product deleted successfully: {}", id);

        } catch (Exception e) {
            logger.error("Error deleting cart product with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart product: " + e.getMessage(), e);
        }
    }

    // ============ ADDITIONAL UTILITY METHODS ============

    /**
     * Update cart product status
     */
    public CartProductResponseDto updateCartProductStatus(Long cartProductId, Integer statusId) {
        logger.info("Updating cart product {} status to {}", cartProductId, statusId);

        try {
            CartProduct cartProduct = cartProductRepository.findById(cartProductId)
                    .orElseThrow(() -> new RuntimeException("Cart product not found with ID: " + cartProductId));

            CartProductStatus newStatus = cartProductStatusRepository.findById(statusId)
                    .orElseThrow(() -> new RuntimeException("Cart product status not found with ID: " + statusId));

            cartProduct.setStatus(newStatus);
            CartProduct updatedCartProduct = cartProductRepository.save(cartProduct);

            return convertToResponseDto(updatedCartProduct);

        } catch (Exception e) {
            logger.error("Error updating cart product status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update cart product status: " + e.getMessage(), e);
        }
    }

    /**
     * Get cart products by cart ID and status
     */
    public List<CartProductResponseDto> getCartProductsByCartIdAndStatus(Long cartId, CartProductStatusType statusType) {
        logger.debug("Getting cart products for cart ID: {} with status: {}", cartId, statusType);

        try {
            CartProductStatus status = cartProductStatusRepository.findByStatusName(String.valueOf(statusType))
                    .orElseThrow(() -> new RuntimeException("Cart product status not found: " + statusType));

            List<CartProduct> cartProducts = cartProductRepository.findByCartCartIdAndStatus(cartId, status);

            return cartProducts.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting cart products by cart ID and status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get cart products by status: " + e.getMessage(), e);
        }
    }

    /**
     * Soft delete cart product by setting status to inactive
     */
    public CartProductResponseDto softDeleteCartProduct(Long cartProductId) {
        logger.info("Soft deleting cart product with ID: {}", cartProductId);

        try {
            CartProductStatus inactiveStatus = cartProductStatusRepository
                    .findByStatusName(String.valueOf(CartProductStatusType.REMOVED))
                    .orElseThrow(() -> new RuntimeException("Inactive cart product status not found"));

            return updateCartProductStatus(cartProductId, inactiveStatus.getStatusId());

        } catch (Exception e) {
            logger.error("Error soft deleting cart product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to soft delete cart product: " + e.getMessage(), e);
        }
    }

    /**
     * Check if cart product exists and is active
     */
    public boolean isCartProductActive(Long cartProductId) {
        try {
            CartProduct cartProduct = cartProductRepository.findById(cartProductId).orElse(null);
            if (cartProduct == null) {
                return false;
            }

            CartProductStatus activeStatus = cartProductStatusRepository
                    .findByStatusName(String.valueOf(CartProductStatusType.REMOVED))
                    .orElse(null);

            return activeStatus != null && cartProduct.getStatus().getStatusId().equals(activeStatus.getStatusId());

        } catch (Exception e) {
            logger.error("Error checking if cart product is active: {}", e.getMessage(), e);
            return false;
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Convert CartProduct entity to CartProductResponseDto
     */
    private CartProductResponseDto convertToResponseDto(CartProduct cartProduct) {
        CartProductResponseDto dto = new CartProductResponseDto();
        dto.setCartProductId(cartProduct.getCartProductId());
        dto.setCartId(cartProduct.getCart().getCartId());
        dto.setStatusId(cartProduct.getStatus().getStatusId());
        dto.setCreatedAt(cartProduct.getCreatedAt());
        dto.setUpdatedAt(cartProduct.getUpdatedAt());

        return dto;
    }
}