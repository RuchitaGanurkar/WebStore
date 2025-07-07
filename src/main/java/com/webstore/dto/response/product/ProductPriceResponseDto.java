package com.webstore.dto.response.product;

import java.math.BigInteger;
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
    private BigInteger priceAmount;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public void setFormattedPrice(String formattedPrice) {
    }
}
