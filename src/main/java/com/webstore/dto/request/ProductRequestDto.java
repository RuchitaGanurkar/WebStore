package com.webstore.dto.request;

import com.webstore.validation.ProductValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequestDto {


    //    Adding Validation Annotation To Requested Data

    @NotNull(groups = ProductValidation.class, message = "Product name is required")
    @NotBlank(groups = ProductValidation.class, message = "Product name should not be blank")
    @Size(min = 1, max = 100, groups = ProductValidation.class, message = "Product name must be between 1 and 100 characters")
    private String productName;

    @Size(max = 500, groups = ProductValidation.class, message = "Product description must not exceed 500 characters")
    private String productDescription;

    @NotNull(groups = ProductValidation.class, message = "Category ID is required")
    private Integer categoryId;

}