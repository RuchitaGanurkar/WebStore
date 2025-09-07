package com.webstore.dto.response.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponseDto {
    private Long orderId;
    private Long cartId;
    private Integer statusId;
    private String statusName; // For easier display
    private BigDecimal totalAmount;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}