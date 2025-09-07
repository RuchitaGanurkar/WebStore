package com.webstore.dto.request.order;

import com.webstore.validation.order.OrderHistoryValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderHistoryRequestDto {

    @NotNull(groups = OrderHistoryValidation.class, message = "Order ID is required")
    @Positive(groups = OrderHistoryValidation.class, message = "Order ID must be positive")
    private Long orderId;

    @Positive(groups = OrderHistoryValidation.class, message = "Old status ID must be positive")
    private Integer oldStatusId; // Can be null for initial status

    @NotNull(groups = OrderHistoryValidation.class, message = "New status ID is required")
    @Positive(groups = OrderHistoryValidation.class, message = "New status ID must be positive")
    private Integer newStatusId;
}