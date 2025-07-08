package com.webstore.dto.response.cart;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartProductResponseDto {
    private Long cartProductId;
    private Long cartId;
    private Integer statusId;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
