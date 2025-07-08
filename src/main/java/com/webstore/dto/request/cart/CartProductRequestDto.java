package com.webstore.dto.request.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartProductRequestDto {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @NotNull(message = "Cart product status ID is required")
    private Integer statusId;
}
