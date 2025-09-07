package com.webstore.implementation.order;

import com.webstore.dto.request.order.OrderRequestDto;
import com.webstore.dto.response.order.OrderResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.order.Order;
import com.webstore.entity.order.OrderStatus;
import com.webstore.enums.order.OrderStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.order.OrderNotFoundException;
import com.webstore.exception.order.OrderStatusNotFoundException;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.order.OrderRepository;
import com.webstore.repository.order.OrderStatusRepository;
import com.webstore.service.order.OrderService;
import com.webstore.service.order.OrderHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryService orderHistoryService;

    public OrderServiceImplementation(OrderRepository orderRepository,
                                      CartRepository cartRepository,
                                      OrderStatusRepository orderStatusRepository,
                                      OrderHistoryService orderHistoryService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.orderHistoryService = orderHistoryService;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        Cart cart = cartRepository.findById(requestDto.getCartId())
                .orElseThrow(() -> new CartNotFoundException(requestDto.getCartId()));

        OrderStatus status = orderStatusRepository.findById(requestDto.getStatusId())
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + requestDto.getStatusId()));

        Order order = new Order();
        order.setCart(cart);
        order.setStatus(status);
        order.setTotalAmount(requestDto.getTotalAmount());

        Order saved = orderRepository.save(order);

        // Create initial order history entry
        orderHistoryService.createOrderHistory(saved.getOrderId(), null, status.getStatusId());

        return mapToDto(saved);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
        return mapToDto(order);
    }

    @Override
    public List<OrderResponseDto> getOrdersByCartId(Long cartId) {
        return orderRepository.findByCartId(cartId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getOrdersByPhoneNumber(String phoneNumber) {
        return orderRepository.findByPhoneNumber(phoneNumber)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getOrdersByStatus(OrderStatusType status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusType newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus status = orderStatusRepository.findByStatusName(newStatus)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found: " + newStatus));

        order.setStatus(status);
        Order updated = orderRepository.save(order);

        // Create order history entry for status change
        orderHistoryService.createOrderHistory(orderId, oldStatus.getStatusId(), status.getStatusId());

        return mapToDto(updated);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long orderId, OrderRequestDto requestDto) {
        Order existing = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (requestDto.getTotalAmount() != null) {
            existing.setTotalAmount(requestDto.getTotalAmount());
        }

        if (requestDto.getStatusId() != null) {
            OrderStatus status = orderStatusRepository.findById(requestDto.getStatusId())
                    .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + requestDto.getStatusId()));
            existing.setStatus(status);
        }

        Order updated = orderRepository.save(existing);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
        orderRepository.delete(order);
    }

    // Helper method
    private OrderResponseDto mapToDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setCartId(order.getCart().getCartId());
        dto.setStatusId(order.getStatus().getStatusId());
        dto.setStatusName(String.valueOf(order.getStatus().getStatusName()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setUpdatedBy(order.getUpdatedBy());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}