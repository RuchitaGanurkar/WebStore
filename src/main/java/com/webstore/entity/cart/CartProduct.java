package com.webstore.entity.cart;

import com.webstore.entity.product.BasicEntities;
import com.webstore.entity.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "cart_product", schema = SCHEMA_NAME,
        indexes = {
                @Index(name = "idx_cart_product_cart", columnList = "cart_id"),
                @Index(name = "idx_cart_product_status", columnList = "status_id")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartProduct extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_product_seq")
    @SequenceGenerator(name = "cart_product_seq", sequenceName = "web_store.seq_cart_product_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "cart_product_id")
    private Long cartProductId;

    @NotNull(message = "Cart cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_product_cart"))
    private Cart cart;

    @NotNull(message = "Cart product status cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_product_status"))
    private CartProductStatus status;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    @OneToMany(mappedBy = "cartProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartProductHistory> cartProductHistories;
}