package com.webstore.implementation.order;

import com.webstore.dto.request.order.OrderStatusRequestDto;
import com.webstore.dto.response.order.OrderStatusResponseDto;
import com.webstore.entity.order.OrderStatus;
import com.webstore.enums.order.OrderStatusType;
import com.webstore.exception.order.OrderStatusNotFoundException;
import com.webstore.repository.order.OrderStatusRepository;
import com.webstore.service.order.OrderStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderStatusServiceImplementation implements OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    public OrderStatusServiceImplementation(OrderStatusRepository orderStatusRepository) {
        this.orderStatusRepository = orderStatusRepository;
    }

    @Override
    @Transactional
    public OrderStatusResponseDto createOrderStatus(OrderStatusRequestDto requestDto) {
        OrderStatus orderStatus = new OrderStatus();

        // ✅ Convert request String → Enum safely
        OrderStatusType statusType = OrderStatusType.valueOf(requestDto.getStatusName().toUpperCase());

        orderStatus.setStatusName(statusType);
        orderStatus.setDescription(requestDto.getDescription());

        OrderStatus saved = orderStatusRepository.save(orderStatus);
        return mapToDto(saved);
    }

    @Override
    public OrderStatusResponseDto getOrderStatusById(Integer id) {
        OrderStatus orderStatus = orderStatusRepository.findById(id)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + id));
        return mapToDto(orderStatus);
    }

    @Override
    public OrderStatusResponseDto getOrderStatusByName(OrderStatusType statusName) {
        // ✅ repo accepts enum now, no .name()
        OrderStatus orderStatus = orderStatusRepository.findByStatusName(statusName)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found: " + statusName));
        return mapToDto(orderStatus);
    }

    @Override
    public List<OrderStatusResponseDto> getAllOrderStatuses() {
        return orderStatusRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderStatusResponseDto updateOrderStatus(Integer statusId, OrderStatusRequestDto requestDto) {
        OrderStatus existing = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + statusId));

        if (requestDto.getStatusName() != null && !requestDto.getStatusName().trim().isEmpty()) {
            // ✅ Convert incoming string to enum
            OrderStatusType statusType = OrderStatusType.valueOf(requestDto.getStatusName().toUpperCase());
            existing.setStatusName(statusType);
        }

        if (requestDto.getDescription() != null) {
            existing.setDescription(requestDto.getDescription());
        }

        OrderStatus updated = orderStatusRepository.save(existing);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteOrderStatus(Integer id) {
        OrderStatus orderStatus = orderStatusRepository.findById(id)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + id));
        orderStatusRepository.delete(orderStatus);
    }

    // ✅ Helper method (convert Enum → String for DTOs)
    private OrderStatusResponseDto mapToDto(OrderStatus orderStatus) {
        OrderStatusResponseDto dto = new OrderStatusResponseDto();
        dto.setStatusId(orderStatus.getStatusId());

        // Expose enum as String to clients
        dto.setStatusName(orderStatus.getStatusName().name());

        dto.setDescription(orderStatus.getDescription());
        dto.setCreatedAt(orderStatus.getCreatedAt());
        dto.setUpdatedAt(orderStatus.getUpdatedAt());
        return dto;
    }
}
