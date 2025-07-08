package com.webstore.dto.request.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartProductStatusRequestDto {

    @NotBlank(message = "Status name is required")
    @Size(max = 20, message = "Status name must not exceed 20 characters")
    private String statusName;
}
