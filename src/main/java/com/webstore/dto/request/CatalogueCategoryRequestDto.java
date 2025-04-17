package com.webstore.dto.request;


import com.webstore.validation.CatalogueCategoryValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatalogueCategoryRequestDto {

    // Adding Validation Annotation To Requested Data

    @NotNull(groups = CatalogueCategoryValidation.class, message = "Catalogue ID is required")
    @Min(value = 1, groups = CatalogueCategoryValidation.class, message = "Catalogue ID must be positive")
    private Integer catalogueId;

    @NotNull(groups = CatalogueCategoryValidation.class, message = "Category ID is required")
    @Min(value = 1, groups = CatalogueCategoryValidation.class, message = "Category ID must be positive")
    private Integer categoryId;

}