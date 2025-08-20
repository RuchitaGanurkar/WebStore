package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.service.cart.CartProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/cart-products")

public class CartProductController {

    private final CartProductService cartProductService;

    public CartProductController(CartProductService cartProductService) {
        this.cartProductService = cartProductService;
    }

    @PostMapping
    public ResponseEntity<CartProductResponseDto> create(@Valid @RequestBody CartProductRequestDto dto) {
        return ResponseEntity.ok(cartProductService.createCartProduct(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartProductResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cartProductService.getCartProductById(id));
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartProductResponseDto>> getByCartId(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartProductService.getCartProductsByCartId(cartId));
    }

    @GetMapping("/count/{cartId}")
    public ResponseEntity<Long> countActive(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartProductService.countActiveCartProducts(cartId));
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<List<CartProductResponseDto>> getByPhone(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(cartProductService.getActiveCartProductsByPhoneNumber(phoneNumber));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cartProductService.deleteCartProduct(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{cartProductId}")
    public ResponseEntity<CartProductResponseDto> updateCartProduct(
            @PathVariable Long cartProductId,
            @Valid @RequestBody CartProductRequestDto requestDto) {
        return ResponseEntity.ok(cartProductService.updateCartProduct(cartProductId, requestDto));
    }
}
