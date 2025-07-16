package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import com.webstore.service.cart.CartProductHistoryService;
import com.webstore.validation.cart.CartProductHistoryValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/cart_product_history/")
public class CartProductHistoryController {
    private final CartProductHistoryService cartProductHistoryService;


    public CartProductHistoryController(CartProductHistoryService cartProductHistoryService) {
        this.cartProductHistoryService = cartProductHistoryService;
    }

    @PostMapping
    public ResponseEntity<CartProductHistoryResponseDto> createCartProductHistory(
            @Valid @Validated(CartProductHistoryValidation.class) @RequestBody CartProductHistoryRequestDto dto) {
               CartProductHistoryResponseDto response = cartProductHistoryService.createCartProductHistory(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CartProductHistoryResponseDto>> getAllCartProductHistory() {

        List<CartProductHistoryResponseDto> histories = cartProductHistoryService.getAllCartProductHistory();
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartProductHistoryResponseDto> getCartProductHistoryById(
            @PathVariable @NotNull @Positive Long id) {

        CartProductHistoryResponseDto history = cartProductHistoryService.getCartProductHistoryById(id);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartProductHistoryResponseDto> updateCartProductHistory(
            @PathVariable @NotNull @Positive Long id,
            @Valid @Validated(CartProductHistoryValidation.class) @RequestBody CartProductHistoryRequestDto dto) {

        CartProductHistoryResponseDto response = cartProductHistoryService.updateCartProductHistory(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartProductHistory(@PathVariable @NotNull @Positive Long id) {

        cartProductHistoryService.deleteCartProductHistory(id);
        return ResponseEntity.noContent().build();
    }

}
