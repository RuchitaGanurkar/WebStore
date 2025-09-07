package com.webstore.service.order;

import com.webstore.dto.request.order.OrderRequestDto;
import com.webstore.dto.response.order.OrderResponseDto;
import com.webstore.enums.order.OrderStatusType;
import jakarta.validation.Valid;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(@Valid OrderRequestDto requestDto);

    OrderResponseDto getOrderById(Long id);

    List<OrderResponseDto> getOrdersByCartId(Long cartId);

    List<OrderResponseDto> getOrdersByPhoneNumber(String phoneNumber);

    List<OrderResponseDto> getOrdersByStatus(OrderStatusType status);

    OrderResponseDto updateOrderStatus(Long orderId, OrderStatusType newStatus);

    OrderResponseDto updateOrder(Long orderId, @Valid OrderRequestDto requestDto);

    void deleteOrder(Long id);
}