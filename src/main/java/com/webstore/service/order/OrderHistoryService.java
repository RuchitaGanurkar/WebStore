package com.webstore.service.order;

import com.webstore.dto.request.order.OrderHistoryRequestDto;
import com.webstore.dto.response.order.OrderHistoryResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface OrderHistoryService {

    OrderHistoryResponseDto createOrderHistory(@Valid OrderHistoryRequestDto requestDto);

    OrderHistoryResponseDto createOrderHistory(Long orderId, Integer oldStatusId, Integer newStatusId);

    OrderHistoryResponseDto getOrderHistoryById(Long id);

    List<OrderHistoryResponseDto> getHistoryByOrderId(Long orderId);

    void deleteOrderHistory(Long id);
}