package com.webstore.entity.cart;

import com.webstore.entity.product.BasicEntities;
import com.webstore.enums.cart.CartProductStatusType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "cart_product_status", schema = SCHEMA_NAME,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_product_status_name", columnNames = "status_name")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartProductStatus extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_product_status_seq")
    @SequenceGenerator(name = "cart_product_status_seq", sequenceName = "web_store.seq_cart_product_status_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "status_id")
    private Integer statusId;

    @NotNull(message = "Status name cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status_name", nullable = false, length = 20)
    private CartProductStatusType statusName;
}
