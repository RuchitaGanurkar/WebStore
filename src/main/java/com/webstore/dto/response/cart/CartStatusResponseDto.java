package com.webstore.dto.response.cart;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartStatusResponseDto {
    private Integer statusId;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
