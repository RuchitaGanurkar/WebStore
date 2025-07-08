package com.webstore.dto.request.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartHistoryRequestDto {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @Size(max = 50, message = "Created by must not exceed 50 characters")
    private String createdBy;

    @Size(max = 50, message = "Updated by must not exceed 50 characters")
    private String updatedBy;
}
