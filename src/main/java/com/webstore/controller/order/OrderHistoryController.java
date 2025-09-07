package com.webstore.controller.order;

import com.webstore.dto.request.order.OrderHistoryRequestDto;
import com.webstore.dto.response.order.OrderHistoryResponseDto;
import com.webstore.service.order.OrderHistoryService;
import com.webstore.validation.order.OrderHistoryValidation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-history")
@Validated
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @PostMapping
    public ResponseEntity<OrderHistoryResponseDto> createOrderHistory(
            @Validated(OrderHistoryValidation.class) @RequestBody OrderHistoryRequestDto requestDto) {
        OrderHistoryResponseDto response = orderHistoryService.createOrderHistory(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderHistoryResponseDto> getOrderHistoryById(@PathVariable Long id) {
        OrderHistoryResponseDto response = orderHistoryService.getOrderHistoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderHistoryResponseDto>> getHistoryByOrderId(@PathVariable Long orderId) {
        List<OrderHistoryResponseDto> response = orderHistoryService.getHistoryByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderHistory(@PathVariable Long id) {
        orderHistoryService.deleteOrderHistory(id);
        return ResponseEntity.noContent().build();
    }
}