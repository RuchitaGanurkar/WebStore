package com.webstore.dto.response.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderHistoryResponseDto {
    private Long orderHistoryId;
    private Long orderId;
    private Integer oldStatusId;
    private String oldStatusName; // For easier display
    private Integer newStatusId;
    private String newStatusName; // For easier display
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}