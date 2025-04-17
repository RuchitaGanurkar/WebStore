package com.webstore.dto.request;

import com.webstore.validation.ProductPriceValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductPriceRequestDto {


    //    Adding Validation Annotation To Requested Data

    @NotNull(groups = ProductPriceValidation.class, message = "Product ID is required")
    @Min(value = 1, groups = ProductPriceValidation.class, message = "Product ID must be a positive number")
    private Integer productId;

    @NotNull(groups = ProductPriceValidation.class, message = "Currency ID is required")
    @Min(value = 1, groups = ProductPriceValidation.class, message = "Currency ID must be a positive number")
    private Integer currencyId;

    @NotNull(groups = ProductPriceValidation.class, message = "Price amount is required")
    @Min(value = 0, groups = ProductPriceValidation.class, message = "Price amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, groups = ProductPriceValidation.class, message = "Price amount must have at most 10 digits and 2 decimal places")
    private Long priceAmount;

}