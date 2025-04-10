package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CurrencyResponseDto {
    private Integer currencyId;
    private String currencyCode;
    private String currencyName;
    private String currencySymbol;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
