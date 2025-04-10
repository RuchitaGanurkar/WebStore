package com.webstore.dto.request;

import lombok.Data;

@Data
public class CatalogueCategoryRequestDto {
    private Integer catalogueId;
    private Integer categoryId;
    private String createdBy;
    private String updatedBy;
}
