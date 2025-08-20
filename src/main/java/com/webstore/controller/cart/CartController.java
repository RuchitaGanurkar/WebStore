package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.service.cart.CartService;
import com.webstore.validation.cart.CartValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    /**
     * Create a new cart
     */
    @PostMapping
    public CartResponseDto createCart(
            @Validated(CartValidation.class) @RequestBody CartRequestDto cartRequestDto) {
        return cartService.createCart(cartRequestDto);
    }

    /**
     * Get the active cart for a user by phone number
     */
    @GetMapping("/user/{phoneNumber}")
    public CartResponseDto getActiveCartByPhoneNumber(
            @PathVariable("phoneNumber") @Min(value = 1000000000L, message = "Phone number must be at least 10 digits")
            Long phoneNumber) {
        return cartService.getActiveCartByPhoneNumber(phoneNumber);
    }

    /**
     * Get cart details by cart ID
     */
    @GetMapping("/{cartId}")
    public CartResponseDto getCartById(@PathVariable("cartId") Long cartId) {
        return cartService.getCartById(cartId);
    }

    // Get all carts by status
    @GetMapping("/status")
    public List<CartResponseDto> getCartsByStatus(@RequestParam("status") String statusName) {
        return cartService.getCartsByStatus(statusName);
    }

    /**
     * Update cart status
     */
    @PutMapping("/{cartId}/status")
    public CartResponseDto updateCartStatus(
            @PathVariable("cartId") Long cartId,
            @RequestBody CartRequestDto cartRequestDto) {
        return cartService.updateCartStatus(cartId, cartRequestDto.getStatusId());
    }

    /**
     * Archive/Delete a cart
     */
    @DeleteMapping("/{cartId}")
    public String archiveCart(@PathVariable("cartId") Long cartId) {
        return cartService.archiveCart(cartId);
    }

    /**
     * PATCH - Remove a product from a cart by IDs.
     */
    @PatchMapping("/{cartId}/products/{productId}")
    public CartResponseDto removeProductFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        return cartService.removeProductFromCart(cartId, productId);
    }

    // Get all carts (no filter)
    @GetMapping("/all")
    public List<CartResponseDto> getAllCarts() {
        return cartService.getAllCarts();
    }
}
