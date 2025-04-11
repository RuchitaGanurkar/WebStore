package com.webstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
public class CurrencyRequestDto {

    @NotBlank(message = "Currency name must not be blank")
    @Size(max = 30, message = "Currency name can be up to 30 characters")
    private String currencyName;

    @NotBlank(message = "Currency code must not be blank")
    @Size(min = 1, max = 10, message = "Currency code must be between 1 and 10 characters")
    private String currencyCode;

    @Getter
    @NotBlank(message = "Currency symbol must not be blank")
    @Size(max = 5, message = "Currency symbol can be up to 5 characters")
    private String currencySymbol;

    private String createdBy;
    private String updatedBy;


}
