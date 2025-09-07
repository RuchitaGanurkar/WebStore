package com.webstore.service.order;

import com.webstore.dto.request.order.OrderStatusRequestDto;
import com.webstore.dto.response.order.OrderStatusResponseDto;
import com.webstore.enums.order.OrderStatusType;
import jakarta.validation.Valid;

import java.util.List;

public interface OrderStatusService {

    OrderStatusResponseDto createOrderStatus(@Valid OrderStatusRequestDto requestDto);

    OrderStatusResponseDto getOrderStatusById(Integer id);

    OrderStatusResponseDto getOrderStatusByName(OrderStatusType statusName);

    List<OrderStatusResponseDto> getAllOrderStatuses();

    OrderStatusResponseDto updateOrderStatus(Integer statusId, @Valid OrderStatusRequestDto requestDto);

    void deleteOrderStatus(Integer id);
}
