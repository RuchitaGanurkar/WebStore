package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductPriceResponseDto {
    private Integer productPriceId;
    private Integer productId;
    private Integer currencyId;
    private Long priceAmount;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
