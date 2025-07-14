package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;

import java.util.List;

public interface CartService {

    /**
     * Create a new cart based on user phone number and catalogue category.
     */
    CartResponseDto createCart(CartRequestDto cartRequestDto);

    /**
     * Fetch the currently active cart for a specific user.
     */
    CartResponseDto getActiveCartByPhoneNumber(Long phoneNumber);

    /**
     * Retrieve a cart by its unique cart ID.
     */
    CartResponseDto getCartById(Long cartId);

    /**
     * Get a list of all carts filtered by status name (e.g., ACTIVE, CHECKED_OUT).
     */
    List<CartResponseDto> getCartsByStatus(String statusName);

    /**
     * Update the status of a given cart.
     */
    CartResponseDto updateCartStatus(Long cartId, Integer statusId);

    /**
     * Archive (soft-delete) a cart by setting its status to ARCHIVED or removing it logically.
     */
    String archiveCart(Long cartId);
}
