package com.webstore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductPriceRequestDto {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Currency ID is required")
    private Integer currencyId;

    @NotNull(message = "Price amount is required")
    private Long priceAmount;

    private String createdBy;
    private String updatedBy;
}
