package com.webstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_price", schema = "web_store",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "currency_id"}))
public class ProductPrice {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and Setters
    public Integer getProductPriceId() { return productPriceId; }
    public void setProductPriceId(Integer productPriceId) { this.productPriceId = productPriceId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public Long getPriceAmount() { return priceAmount; }
    public void setPriceAmount(Long priceAmount) { this.priceAmount = priceAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
