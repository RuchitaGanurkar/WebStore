package com.webstore.dto.response.cart;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartResponseDto {
    private Long cartId;
    private Long phoneNumber;
    private Integer catalogueId;
    private Integer statusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
