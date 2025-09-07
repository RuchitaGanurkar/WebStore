package com.webstore.dto.request.order;

import com.webstore.validation.order.OrderValidation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequestDto {

    @NotNull(groups = OrderValidation.class, message = "Cart ID is required")
    @Positive(groups = OrderValidation.class, message = "Cart ID must be positive")
    private Long cartId;

    @NotNull(groups = OrderValidation.class, message = "Status ID is required")
    @Positive(groups = OrderValidation.class, message = "Status ID must be positive")
    private Integer statusId;

    @NotNull(groups = OrderValidation.class, message = "Total amount is required")
    @DecimalMin(value = "0.0", groups = OrderValidation.class, message = "Total amount must be positive")
    private BigDecimal totalAmount;
}