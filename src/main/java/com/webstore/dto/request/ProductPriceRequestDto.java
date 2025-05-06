package com.webstore.dto.request;

import com.webstore.validation.ProductPriceValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigInteger;

@Data
public class ProductPriceRequestDto {

    @NotNull(groups = ProductPriceValidation.class, message = "Product ID is required")
    @Min(value = 1, groups = ProductPriceValidation.class, message = "Product ID must be a positive number")
    private Integer productId;

    @NotNull(groups = ProductPriceValidation.class, message = "Currency ID is required")
    @Min(value = 1, groups = ProductPriceValidation.class, message = "Currency ID must be a positive number")
    private Integer currencyId;

    @NotNull(groups = ProductPriceValidation.class, message = "Price amount is required")
    @Digits(integer = 12, fraction = 0, groups = ProductPriceValidation.class, message = "Price amount must be in paise as whole number")
    private BigInteger priceAmount;
}