package com.webstore.entity.cart;


import com.webstore.entity.product.BasicEntities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "cart", schema = SCHEMA_NAME,
        indexes = {
                @Index(name = "idx_cart_phone_number", columnList = "phone_number"),
                @Index(name = "idx_cart_catalogue_category", columnList = "catalogue_category_id"),
                @Index(name = "idx_cart_status", columnList = "status_id")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_seq")
    @SequenceGenerator(name = "cart_seq", sequenceName = "web_store.seq_cart_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "cart_id")
    private Long cartId;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Phone number contains invalid characters")
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @NotNull(message = "Catalogue category ID cannot be null")
    @Column(name = "catalogue_category_id", nullable = false)
    private Integer catalogueCategoryId;

    @NotNull(message = "Cart status cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_status"))
    private CartStatus status;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartHistory> cartHistories;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartProduct> cartProducts;
}