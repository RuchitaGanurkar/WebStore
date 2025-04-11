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

    @NotNull(groups = CatalogueCategoryValidation.class, message = "Created by is required")
    @NotBlank(groups = CatalogueCategoryValidation.class, message = "Created by should not be blank")
    @Size(min = 3, max = 50, groups = CatalogueCategoryValidation.class, message = "Created by must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", groups = CatalogueCategoryValidation.class, message = "Created by can only contain letters, numbers, dots, underscores and hyphens")
    private String createdBy;
}