package com.webstore.dto.response.cart;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartProductHistoryResponseDto {
    private Long cartProductHistoryId;
    private Long cartProductId;
    private Integer productId;
    private Integer oldQuantity;
    private Integer newQuantity;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
