package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartHistoryRequestDto;
import com.webstore.dto.response.cart.CartHistoryResponseDto;
import com.webstore.service.cart.CartHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cart-histories")
@RequiredArgsConstructor
public class CartHistoryController {

    private final CartHistoryService cartHistoryService;

    @PostMapping
    public ResponseEntity<CartHistoryResponseDto> create(@Valid @RequestBody CartHistoryRequestDto dto) {
        return ResponseEntity.ok(cartHistoryService.createCartHistory(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartHistoryResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cartHistoryService.getCartHistoryById(id));
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartHistoryResponseDto>> getByCartId(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartHistoryService.getCartHistoriesByCartId(cartId));
    }

    @GetMapping("/created-by/{username}")
    public ResponseEntity<List<CartHistoryResponseDto>> getByCreatedBy(@PathVariable String username) {
        return ResponseEntity.ok(cartHistoryService.getCartHistoriesByCreatedBy(username));
    }

    @GetMapping("/updated-by/{username}")
    public ResponseEntity<List<CartHistoryResponseDto>> getByUpdatedBy(@PathVariable String username) {
        return ResponseEntity.ok(cartHistoryService.getCartHistoriesByUpdatedBy(username));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CartHistoryResponseDto>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(cartHistoryService.getCartHistoriesByDateRange(start, end));
    }
}