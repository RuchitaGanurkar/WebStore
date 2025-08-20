package com.webstore.dto.response.cart;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartProductResponseDto {
    private Long cartProductId;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private Integer statusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
