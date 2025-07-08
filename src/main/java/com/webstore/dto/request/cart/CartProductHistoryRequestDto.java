package com.webstore.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartProductHistoryRequestDto {

    @NotNull(message = "Cart product ID is required")
    private Long cartProductId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Old quantity is required")
    @Min(value = 1, message = "Old quantity must be at least 1")
    private Integer oldQuantity;

    @NotNull(message = "New quantity is required")
    @Min(value = 1, message = "New quantity must be at least 1")
    private Integer newQuantity;

    @Size(max = 50, message = "Created by must not exceed 50 characters")
    private String createdBy;

    @Size(max = 50, message = "Updated by must not exceed 50 characters")
    private String updatedBy;
}
