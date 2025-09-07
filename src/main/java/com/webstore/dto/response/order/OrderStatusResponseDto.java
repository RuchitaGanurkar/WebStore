package com.webstore.dto.response.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderStatusResponseDto {
    private Integer statusId;
    private String statusName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}