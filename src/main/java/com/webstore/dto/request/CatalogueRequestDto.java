package com.webstore.dto.request;

import lombok.Data;

@Data
public class CatalogueRequestDto {
    private String catalogueName;
    private String catalogueDescription;
    private String createdBy;
    private String updatedBy;
}
