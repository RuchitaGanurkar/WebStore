package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductStatusRequestDto;
import com.webstore.dto.request.cart.CartStatusRequestDto;
import com.webstore.dto.response.cart.CartProductStatusResponseDto;
import com.webstore.dto.response.cart.CartStatusResponseDto;
import com.webstore.service.cart.CartProductStatusService;
import com.webstore.service.cart.CartStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Validated
@RequestMapping("/api/cart_status/")
public class CartStatusController {

        private final CartStatusService cartStatusService;


        public CartStatusController(CartStatusService cartStatusService) {
            this.cartStatusService = cartStatusService;
        }

        @GetMapping("/{statusId}")
        public ResponseEntity<CartStatusResponseDto> getCartStatusById(@PathVariable Integer statusId) {
            CartStatusResponseDto responseDto = cartStatusService.getCartStatusById(statusId);
            return ResponseEntity.ok(responseDto);
        }

        @GetMapping
        public ResponseEntity<List<CartStatusResponseDto>> getAllCartStatuses() {
            List<CartStatusResponseDto> responseDto = cartStatusService.getAllCartStatuses();
            return ResponseEntity.ok(responseDto);
        }

        @PutMapping("/{statusId}")
        public ResponseEntity<CartStatusResponseDto> updateCartStatus(
                @PathVariable Integer statusId,
                @Valid @RequestBody CartStatusRequestDto requestDto) {
            CartStatusResponseDto responseDto = cartStatusService.updateCartStatus(statusId, requestDto);
            return ResponseEntity.ok(responseDto);
        }

        @DeleteMapping("/{statusId}")
        public ResponseEntity<Void> deleteCartStatus(@PathVariable Integer statusId) {
            cartStatusService.deleteCartStatus(statusId);
            return ResponseEntity.noContent().build();
        }
}
