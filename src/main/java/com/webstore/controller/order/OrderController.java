package com.webstore.controller.order;

import com.webstore.dto.request.order.OrderRequestDto;
import com.webstore.dto.response.order.OrderResponseDto;
import com.webstore.enums.order.OrderStatusType;
import com.webstore.service.order.OrderService;
import com.webstore.validation.order.OrderValidation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Validated(OrderValidation.class) @RequestBody OrderRequestDto requestDto) {
        OrderResponseDto response = orderService.createOrder(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByCartId(@PathVariable Long cartId) {
        List<OrderResponseDto> response = orderService.getOrdersByCartId(cartId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByPhoneNumber(@PathVariable String phoneNumber) {
        List<OrderResponseDto> response = orderService.getOrdersByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatus(@PathVariable OrderStatusType status) {
        List<OrderResponseDto> response = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @Validated(OrderValidation.class) @RequestBody OrderRequestDto requestDto) {
        OrderResponseDto response = orderService.updateOrder(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatusType newStatus) {
        OrderResponseDto response = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checkout cart - Convert cart to order
     * This is the critical endpoint for E2E testing: Cart → Order conversion
     */
    @PostMapping("/checkout/{cartId}")
    public ResponseEntity<OrderResponseDto> checkoutCart(@PathVariable Long cartId) {
        OrderResponseDto response = orderService.checkoutCart(cartId);
        return ResponseEntity.ok(response);
    }
}