package com.webstore.entity.product;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "product_price",
        schema = SCHEMA_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_currency",
                columnNames = {"product_id", "currency_id"}
        )
)
public class ProductPrice extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_price_generator")
    @SequenceGenerator(
            name = "product_price_generator",
            sequenceName = SCHEMA_NAME + ".seq_product_price_id",
            allocationSize = 1
    )
    @Column(name = "product_price_id")
    private Integer productPriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "price_amount", nullable = false)
    private BigInteger priceAmount;
}
