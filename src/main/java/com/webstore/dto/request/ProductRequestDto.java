package com.webstore.dto.request;

import com.webstore.validation.ProductValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequestDto {

    @NotBlank(groups = ProductValidation.class, message = "Product name is required and must not be blank")
    @Size(min = 1, max = 100, groups = ProductValidation.class, message = "Product name must be between 1 and 100 characters")
    private String productName;

    @Size(max = 500, groups = ProductValidation.class, message = "Product description must not exceed 500 characters")
    private String productDescription;

    @NotNull(groups = ProductValidation.class, message = "Category ID is required")
    private Integer categoryId;
}
