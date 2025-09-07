package com.webstore.controller.order;

import com.webstore.dto.request.order.OrderStatusRequestDto;
import com.webstore.dto.response.order.OrderStatusResponseDto;
import com.webstore.enums.order.OrderStatusType;
import com.webstore.service.order.OrderStatusService;
import com.webstore.validation.order.OrderStatusValidation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-status")
@Validated
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    public OrderStatusController(OrderStatusService orderStatusService) {
        this.orderStatusService = orderStatusService;
    }

    @PostMapping
    public ResponseEntity<OrderStatusResponseDto> createOrderStatus(
            @Validated(OrderStatusValidation.class) @RequestBody OrderStatusRequestDto requestDto) {
        OrderStatusResponseDto response = orderStatusService.createOrderStatus(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderStatusResponseDto> getOrderStatusById(@PathVariable Integer id) {
        OrderStatusResponseDto response = orderStatusService.getOrderStatusById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{statusName}")
    public ResponseEntity<OrderStatusResponseDto> getOrderStatusByName(@PathVariable OrderStatusType statusName) {
        OrderStatusResponseDto response = orderStatusService.getOrderStatusByName(statusName);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderStatusResponseDto>> getAllOrderStatuses() {
        List<OrderStatusResponseDto> response = orderStatusService.getAllOrderStatuses();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderStatusResponseDto> updateOrderStatus(
            @PathVariable Integer id,
            @Validated(OrderStatusValidation.class) @RequestBody OrderStatusRequestDto requestDto) {
        OrderStatusResponseDto response = orderStatusService.updateOrderStatus(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderStatus(@PathVariable Integer id) {
        orderStatusService.deleteOrderStatus(id);
        return ResponseEntity.noContent().build();
    }
}