package com.webstore.implementation.order;

import com.webstore.dto.request.order.OrderRequestDto;
import com.webstore.dto.response.order.OrderResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartStatus;
import com.webstore.entity.order.Order;
import com.webstore.entity.order.OrderStatus;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.enums.order.OrderStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.cart.EmptyCartException;
import com.webstore.exception.cart.InvalidCartStatusException;
import com.webstore.exception.order.OrderNotFoundException;
import com.webstore.exception.order.OrderStatusNotFoundException;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.repository.order.OrderRepository;
import com.webstore.repository.order.OrderStatusRepository;
import com.webstore.repository.product.ProductPriceRepository;
import com.webstore.service.order.OrderHistoryService;
import com.webstore.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;
    private final CartProductRepository cartProductRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryService orderHistoryService;
    private final ProductPriceRepository productPriceRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        // 1) Fetch cart (no need to eagerly load cart products for validation)
        Cart cart = cartRepository.findById(requestDto.getCartId())
                .orElseThrow(() -> new CartNotFoundException(requestDto.getCartId()));

        // 2) Validate cart
        if (cart.getStatus() == null || cart.getStatus().getStatusName() != CartStatusType.ACTIVE) {
            throw new InvalidCartStatusException("Only ACTIVE carts can be converted to orders");
        }
        
        // Check if cart has any ACTIVE products using repository count
        Long activeProductCount = cartProductRepository.countByCartIdAndStatusName(
                requestDto.getCartId(), 
                CartProductStatusType.ADDED
        );
        if (activeProductCount == null || activeProductCount == 0) {
            throw new EmptyCartException("Cart is empty, cannot create order");
        }

        // 3) Fetch active cart products and calculate total amount
        List<CartProduct> activeCartProducts = cartProductRepository.findByCartIdAndStatusName(
                requestDto.getCartId(), 
                CartProductStatusType.ADDED
        );
        
        BigDecimal totalAmount = activeCartProducts.stream()
                .map(cp -> {
                    Integer productId = cp.getProduct().getProductId();

                    var price = productPriceRepository.findByProductProductId(productId)
                            .stream()
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException("No price configured for product ID: " + productId))
                            .getPriceAmount(); // BigInteger

                    BigDecimal unitPrice = new BigDecimal(price);
                    return unitPrice.multiply(BigDecimal.valueOf(cp.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4) Resolve status (default to PENDING if not provided)
        OrderStatus status = (requestDto.getStatusId() != null)
                ? orderStatusRepository.findById(requestDto.getStatusId())
                .orElseThrow(() -> new OrderStatusNotFoundException(
                        "Order status not found with ID: " + requestDto.getStatusId()))
                : orderStatusRepository.findByStatusName(OrderStatusType.PENDING)
                .orElseThrow(() -> new OrderStatusNotFoundException("Default status PENDING not found"));

        // 5) Create order
        Order order = new Order();
        order.setCart(cart);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);

        // 6) Initial order history
        orderHistoryService.createOrderHistory(saved.getOrderId(), null, status.getStatusId());

        // 7) Archive the cart
        CartStatus archivedStatus = cartStatusRepository.findByStatusName(CartStatusType.ARCHIVED)
                .orElseThrow(() -> new InvalidCartStatusException("Cart status 'ARCHIVED' not found"));
        cart.setStatus(archivedStatus);
        cartRepository.save(cart);

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
        OrderStatus resolved = orderStatusRepository.findByStatusName(newStatus)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found: " + newStatus));

        order.setStatus(resolved);
        Order updated = orderRepository.save(order);

        // Log history
        orderHistoryService.createOrderHistory(orderId, oldStatus.getStatusId(), resolved.getStatusId());

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
                    .orElseThrow(() -> new OrderStatusNotFoundException(
                            "Order status not found with ID: " + requestDto.getStatusId()));
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

    @Override
    @Transactional
    public OrderResponseDto checkoutCart(Long cartId) {
        OrderRequestDto dto = new OrderRequestDto();
        dto.setCartId(cartId);
        dto.setStatusId(null); // let it default to PENDING
        return createOrder(dto);
    }

    // ===== Helper =====
    private OrderResponseDto mapToDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setCartId(order.getCart().getCartId());
        dto.setStatusId(order.getStatus().getStatusId());
        dto.setStatusName(order.getStatus().getStatusName().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setUpdatedBy(order.getUpdatedBy());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
