package com.webstore.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartRequestDto {

    @NotNull(message = "Phone number is required")
    @Min(value = 1000000000L, message = "Phone number must be at least 10 digits")
    private Long phoneNumber;

    @NotNull(message = "Catalogue category ID is required")
    private Integer catalogueCategoryId;

    @NotNull(message = "Cart status ID is required")
    private Integer statusId;
}
