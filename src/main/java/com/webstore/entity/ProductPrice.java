package com.webstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_price",
        schema = "web_store",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "currency_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_price_seq")
    @SequenceGenerator(name = "product_price_seq", sequenceName = "web_store.seq_product_price_id", allocationSize = 1)
    @Column(name = "product_price_id")
    private Integer productPriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "price_amount", nullable = false)
    private Long priceAmount;
}
