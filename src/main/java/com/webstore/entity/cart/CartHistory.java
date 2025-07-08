package com.webstore.entity.cart;

import com.webstore.entity.product.BasicEntities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "cart_history", schema = SCHEMA_NAME,
        indexes = {
                @Index(name = "idx_cart_history_cart", columnList = "cart_id")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartHistory extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_history_seq")
    @SequenceGenerator(name = "cart_history_seq", sequenceName = "web_store.seq_cart_history_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "cart_history_id")
    private Long cartHistoryId;

    @NotNull(message = "Cart cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_history_cart"))
    private Cart cart;
}