package com.webstore.dto.request;

import lombok.Data;

@Data
public class ProductPriceRequestDto {
    private Integer productId;
    private Integer currencyId;
    private Long priceAmount;
    private String createdBy;
    private String updatedBy;
}
