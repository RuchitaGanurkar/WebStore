package com.webstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "product_price",
        schema = "web_store",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_currency",
                columnNames = {"product_id", "currency_id"}
        )
)
public class ProductPrice extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_price_generator")
    @SequenceGenerator(name = "product_price_generator", sequenceName = "web_store.seq_product_price_id", allocationSize = 1)
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