package com.webstore.implementation.order;

import com.webstore.dto.request.order.OrderHistoryRequestDto;
import com.webstore.dto.response.order.OrderHistoryResponseDto;
import com.webstore.entity.order.Order;
import com.webstore.entity.order.OrderHistory;
import com.webstore.entity.order.OrderStatus;
import com.webstore.exception.order.OrderHistoryNotFoundException;
import com.webstore.exception.order.OrderNotFoundException;
import com.webstore.exception.order.OrderStatusNotFoundException;
import com.webstore.repository.order.OrderHistoryRepository;
import com.webstore.repository.order.OrderRepository;
import com.webstore.repository.order.OrderStatusRepository;
import com.webstore.service.order.OrderHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderHistoryServiceImplementation implements OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    public OrderHistoryServiceImplementation(OrderHistoryRepository orderHistoryRepository,
                                             OrderRepository orderRepository,
                                             OrderStatusRepository orderStatusRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
    }

    @Override
    @Transactional
    public OrderHistoryResponseDto createOrderHistory(OrderHistoryRequestDto requestDto) {
        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + requestDto.getOrderId()));

        OrderStatus oldStatus = null;
        if (requestDto.getOldStatusId() != null) {
            oldStatus = orderStatusRepository.findById(requestDto.getOldStatusId())
                    .orElseThrow(() -> new OrderStatusNotFoundException("Old status not found with ID: " + requestDto.getOldStatusId()));
        }

        OrderStatus newStatus = orderStatusRepository.findById(requestDto.getNewStatusId())
                .orElseThrow(() -> new OrderStatusNotFoundException("New status not found with ID: " + requestDto.getNewStatusId()));

        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);

        OrderHistory saved = orderHistoryRepository.save(history);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderHistoryResponseDto createOrderHistory(Long orderId, Integer oldStatusId, Integer newStatusId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        OrderStatus oldStatus = null;
        if (oldStatusId != null) {
            oldStatus = orderStatusRepository.findById(oldStatusId)
                    .orElseThrow(() -> new OrderStatusNotFoundException("Old status not found with ID: " + oldStatusId));
        }

        OrderStatus newStatus = orderStatusRepository.findById(newStatusId)
                .orElseThrow(() -> new OrderStatusNotFoundException("New status not found with ID: " + newStatusId));

        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setCreatedBy("system");
        history.setUpdatedBy("system");

        OrderHistory saved = orderHistoryRepository.save(history);
        return mapToDto(saved);
    }

    @Override
    public OrderHistoryResponseDto getOrderHistoryById(Long id) {
        OrderHistory history = orderHistoryRepository.findById(id)
                .orElseThrow(() -> new OrderHistoryNotFoundException("Order history not found with ID: " + id));
        return mapToDto(history);
    }

    @Override
    public List<OrderHistoryResponseDto> getHistoryByOrderId(Long orderId) {
        return orderHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOrderHistory(Long id) {
        OrderHistory history = orderHistoryRepository.findById(id)
                .orElseThrow(() -> new OrderHistoryNotFoundException("Order history not found with ID: " + id));
        orderHistoryRepository.delete(history);
    }

    // Helper method
    private OrderHistoryResponseDto mapToDto(OrderHistory history) {
        OrderHistoryResponseDto dto = new OrderHistoryResponseDto();
        dto.setOrderHistoryId(history.getOrderHistoryId());
        dto.setOrderId(history.getOrder().getOrderId());

        if (history.getOldStatus() != null) {
            dto.setOldStatusId(history.getOldStatus().getStatusId());
            dto.setOldStatusName(history.getOldStatus().getStatusName().name());
        }

        dto.setNewStatusId(history.getNewStatus().getStatusId());
        dto.setNewStatusName(history.getNewStatus().getStatusName().name());
        dto.setCreatedBy(history.getCreatedBy());
        dto.setUpdatedBy(history.getUpdatedBy());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setUpdatedAt(history.getUpdatedAt());

        return dto;
    }
}