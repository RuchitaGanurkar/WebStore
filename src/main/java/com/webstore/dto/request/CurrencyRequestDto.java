package com.webstore.dto.request;

import lombok.Data;

@Data
public class CurrencyRequestDto {
    private String currencyCode;
    private String currencyName;
    private String currencySymbol;
    private String createdBy;
    private String updatedBy;
}
