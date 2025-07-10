package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductStatusRequestDto;
import com.webstore.dto.response.cart.CartProductStatusResponseDto;
import com.webstore.service.cart.CartProductStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/cart_product_status/")
public class CartProductStatusController {

    private final CartProductStatusService cartProductStatusService;


    public CartProductStatusController(CartProductStatusService cartProductStatusService) {
        this.cartProductStatusService = cartProductStatusService;
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<CartProductStatusResponseDto> getCartProductStatusById(@PathVariable Integer statusId) {
        CartProductStatusResponseDto responseDto = cartProductStatusService.getCartProductStatusById(statusId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<CartProductStatusResponseDto>> getAllCartProductStatuses() {
        List<CartProductStatusResponseDto> responseDto = cartProductStatusService.getAllCartProductStatuses();
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<CartProductStatusResponseDto> updateCartProductStatus(
            @PathVariable Integer statusId,
            @Valid @RequestBody CartProductStatusRequestDto requestDto) {
        CartProductStatusResponseDto responseDto = cartProductStatusService.updateCartProductStatus(statusId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{statusId}")
    public ResponseEntity<Void> deleteCartProductStatus(@PathVariable Integer statusId) {
        cartProductStatusService.deleteCartProductStatus(statusId);
        return ResponseEntity.noContent().build();
    }

}
