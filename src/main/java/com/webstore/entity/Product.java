package com.webstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product", schema = "web_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "web_store.seq_product_id", allocationSize = 1)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", nullable = false, unique = true, length = 50)
    private String productName;

    @Column(name = "product_description", length = 100)
    private String productDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
