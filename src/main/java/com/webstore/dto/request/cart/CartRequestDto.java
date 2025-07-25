package com.webstore.dto.request.cart;

import com.webstore.entity.product.Catalogue;
import com.webstore.validation.cart.CartValidation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartRequestDto {

    @NotNull(groups = CartValidation.class, message = "Phone number is required")
    @Min(value = 1000000000L, groups = CartValidation.class, message = "Phone number must be at least 10 digits")
    @Max(value = 9999999999L, groups = CartValidation.class, message = "Phone number must not exceed 10 digits")
    private Long phoneNumber;

    // CHANGED: catalogueCategoryId -> catalogueId
    @NotNull(groups = CartValidation.class, message = "Catalogue ID is required")
    @Positive(groups = CartValidation.class, message = "Catalogue ID must be positive")
    private Integer catalogueId;

    @NotNull(groups = CartValidation.class, message = "Cart status ID is required")
    @Positive(groups = CartValidation.class, message = "Status ID must be positive")
    private Integer statusId;
}