package com.webstore.entity.order;

import com.webstore.entity.cart.Cart;
import com.webstore.entity.product.BasicEntities;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "orders", schema = SCHEMA_NAME,
        indexes = {
                @Index(name = "idx_orders_cart", columnList = "cart_id"),
                @Index(name = "idx_orders_status", columnList = "status_id"),
                @Index(name = "idx_orders_created_at", columnList = "created_at")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "web_store.seq_order_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "order_id")
    private Long orderId;

    @NotNull(message = "Cart cannot be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_cart"))
    private Cart cart;

    @NotNull(message = "Order status cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_status"))
    private OrderStatus status;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderHistory> orderHistories;
}