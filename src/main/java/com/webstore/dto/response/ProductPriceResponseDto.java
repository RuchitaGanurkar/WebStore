package com.webstore.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductPriceResponseDto {
    private Integer productPriceId;
    private Integer productId;
    private String productName;
    private Integer currencyId;
    private String currencyCode;
    private String currencySymbol;
    private Long priceAmount;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}