package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartStatus;
import com.webstore.entity.product.Catalogue;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.product.CatalogueNotFoundException;
import com.webstore.exception.cart.CartStatusNotFoundException;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.repository.product.CatalogueRepository;
import com.webstore.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImplementation implements CartService {

    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;
    private final CatalogueRepository catalogueRepository;

    @Override
    public CartResponseDto createCart(CartRequestDto cartRequestDto) {
        CartStatus status = getStatusById(cartRequestDto.getStatusId());

        // FIXED: Handle Catalogue entity properly
        Catalogue catalogue;
        if (cartRequestDto.getCatalogueId() != null) {
            catalogue = catalogueRepository.findById(cartRequestDto.getCatalogueId())
                    .orElseThrow(() -> new CatalogueNotFoundException(
                            "Catalogue not found with ID: " + cartRequestDto.getCatalogueId()));
        } else {
            throw new CatalogueNotFoundException("Catalogue ID cannot be null");
        }

        Cart cart = new Cart();
        cart.setPhoneNumber(cartRequestDto.getPhoneNumber().toString());
        cart.setCatalogue(catalogue); // FIXED: Set the actual Catalogue entity
        cart.setStatus(status);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    public CartResponseDto getActiveCartByPhoneNumber(Long phoneNumber) {
        return cartRepository.findActiveCartByPhoneNumber(phoneNumber.toString())
                .map(this::mapToDto)
                .orElseThrow(() ->
                        new CartNotFoundException("Active cart not found for phone number: " + phoneNumber));
    }

    @Override
    public CartResponseDto getCartById(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with ID: " + cartId));
        return mapToDto(cart);
    }

    @Override
    public List<CartResponseDto> getCartsByStatus(String statusName) {
        CartStatusType statusEnum = parseStatusEnum(statusName);

        List<Cart> carts = cartRepository.findByStatusStatusName(statusEnum);
        return carts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CartResponseDto updateCartStatus(Long cartId, Integer statusId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with ID: " + cartId));

        CartStatus newStatus = getStatusById(statusId);
        cart.setStatus(newStatus);
        cart.setUpdatedAt(LocalDateTime.now());

        return mapToDto(cartRepository.save(cart));
    }

    @Override
    public String archiveCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with ID: " + cartId));

        CartStatus archivedStatus = cartStatusRepository.findByStatusName(CartStatusType.ARCHIVED)
                .orElseThrow(() -> new CartStatusNotFoundException("Status 'ARCHIVED' not found"));

        cart.setStatus(archivedStatus);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return String.format("Cart archived successfully, cart_id: %d", cartId);
    }

    // ADDED: New method to get carts by catalogue ID
    public List<CartResponseDto> getCartsByCatalogueId(Integer catalogueId) {
        List<Cart> carts = cartRepository.findByCatalogueCatalogueId(catalogueId);
        return carts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper methods

    private CartStatus getStatusById(Integer statusId) {
        return cartStatusRepository.findById(statusId)
                .orElseThrow(() -> new CartStatusNotFoundException("Cart status not found with ID: " + statusId));
    }

    private CartStatusType parseStatusEnum(String statusName) {
        try {
            return CartStatusType.valueOf(statusName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CartStatusNotFoundException("Invalid cart status: " + statusName);
        }
    }

    private CartResponseDto mapToDto(Cart cart) {
        CartResponseDto dto = new CartResponseDto();
        dto.setCartId(cart.getCartId());
        dto.setPhoneNumber(Long.parseLong(cart.getPhoneNumber()));

        // FIXED: Properly access catalogueId from the Catalogue entity
        if (cart.getCatalogue() != null) {
            dto.setCatalogueId(cart.getCatalogue().getCatalogueId());
        }

        // FIXED: Properly access statusId from the CartStatus entity
        if (cart.getStatus() != null) {
            dto.setStatusId(cart.getStatus().getStatusId());
        }

        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }
}