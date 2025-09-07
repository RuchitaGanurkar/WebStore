package com.webstore.dto.request.order;

import com.webstore.validation.order.OrderStatusValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderStatusRequestDto {

    @NotBlank(groups = OrderStatusValidation.class, message = "Status name is required")
    @Size(max = 20, groups = OrderStatusValidation.class, message = "Status name must not exceed 20 characters")
    private String statusName;

    @Size(max = 255, groups = OrderStatusValidation.class, message = "Description must not exceed 255 characters")
    private String description;
}