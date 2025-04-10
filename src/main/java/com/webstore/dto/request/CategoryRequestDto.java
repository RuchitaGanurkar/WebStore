package com.webstore.dto.request;

import lombok.Data;

@Data
public class CategoryRequestDto {
    private String categoryName;
    private String categoryDescription;
    private String createdBy;
    private String updatedBy;
}
