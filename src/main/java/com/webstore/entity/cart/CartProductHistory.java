package com.webstore.entity.cart;


import com.webstore.entity.product.BasicEntities;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "cart_product_history", schema = SCHEMA_NAME,
        indexes = {
                @Index(name = "idx_cart_product_history_cart_product", columnList = "cart_product_id"),
                @Index(name = "idx_cart_product_history_product", columnList = "product_id")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartProductHistory extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_product_history_seq")
    @SequenceGenerator(name = "cart_product_history_seq", sequenceName = "web_store.seq_cart_product_history_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "cart_product_history_id")
    private Long cartProductHistoryId;

    @NotNull(message = "Cart product cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_product_history_cart_product"))
    private CartProduct cartProduct;

    @NotNull(message = "Product ID cannot be null")
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @NotNull(message = "Old quantity cannot be null")
    @Min(value = 0, message = "Old quantity cannot be negative")
    @Column(name = "old_quantity", nullable = false)
    private Integer oldQuantity = 1;

    @NotNull(message = "New quantity cannot be null")
    @Min(value = 0, message = "New quantity cannot be negative")
    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;
}