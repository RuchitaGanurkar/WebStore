package com.webstore.entity.order;

import com.webstore.entity.product.BasicEntities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "order_history", schema = SCHEMA_NAME,
        indexes = {
                @Index(name = "idx_order_history_order", columnList = "order_id"),
                @Index(name = "idx_order_history_old_status", columnList = "old_status_id"),
                @Index(name = "idx_order_history_new_status", columnList = "new_status_id"),
                @Index(name = "idx_order_history_created_at", columnList = "created_at")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_history_seq")
    @SequenceGenerator(name = "order_history_seq", sequenceName = "web_store.seq_order_history_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "order_history_id")
    private Long orderHistoryId;

    @NotNull(message = "Order cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_history_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_status_id",
            foreignKey = @ForeignKey(name = "fk_order_history_old_status"))
    private OrderStatus oldStatus;

    @NotNull(message = "New status cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_status_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_history_new_status"))
    private OrderStatus newStatus;
}