package com.webstore.dto.request;

import com.webstore.validation.CurrencyValidation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRequestDto {

    @NotNull(groups = CurrencyValidation.class, message = "Currency code is required")
    @NotBlank(groups = CurrencyValidation.class, message = "Currency code should not be blank")
    @Size(min = 3, max = 3, groups = CurrencyValidation.class, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @NotNull(groups = CurrencyValidation.class, message = "Currency name is required")
    @NotBlank(groups = CurrencyValidation.class, message = "Currency name should not be blank")
    @Size(min = 1, max = 50, groups = CurrencyValidation.class, message = "Currency name must be between 1 and 50 characters")
    private String currencyName;

    @NotNull(groups = CurrencyValidation.class, message = "Currency symbol is required")
    @NotBlank(groups = CurrencyValidation.class, message = "Currency symbol should not be blank")
    @Size(min = 1, max = 5, groups = CurrencyValidation.class, message = "Currency symbol must be between 1 and 5 characters")
    private String currencySymbol;
}
