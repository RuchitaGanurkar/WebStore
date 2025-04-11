package com.webstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CatalogueRequestDto {

    @NotBlank(message = "Catalogue name must not be blank")
    @Size(min = 1, max = 30, message = "Catalogue name must be between 1 and 30 characters")
    private String catalogueName;

    private String createdBy;
    private String updatedBy;
}
