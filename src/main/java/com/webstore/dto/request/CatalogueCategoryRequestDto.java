package com.webstore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CatalogueCategoryRequestDto {

    @NotNull(message = "Catalogue ID is required")
    private Integer catalogueId;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    private String createdBy;
    private String updatedBy;
}
