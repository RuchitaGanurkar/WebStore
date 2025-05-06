package com.webstore.dto.request;

import com.webstore.validation.CatalogueValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatalogueRequestDto {

    // Adding Validation Annotation To Requested Data

    @NotNull(groups = CatalogueValidation.class, message = "Catalogue name is required")
    @NotBlank(groups = CatalogueValidation.class, message = "Catalogue name should not be blank")
    @Size(min = 2, max = 100, groups = CatalogueValidation.class, message = "Catalogue name must be between 2 and 100 characters")
    private String catalogueName;

    @NotNull(groups = CatalogueValidation.class, message = "Catalogue description is required")
    @NotBlank(groups = CatalogueValidation.class, message = "Catalogue description should not be blank")
    @Size(max = 500, groups = CatalogueValidation.class, message = "Catalogue description must not exceed 500 characters")
    private String catalogueDescription;

}