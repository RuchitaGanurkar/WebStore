package com.webstore.dto.response.product;

import java.time.LocalDateTime;
import java.util.List;

import com.webstore.dto.request.product.CategoryRequestDto;
import lombok.Data;

@Data
public class CatalogueResponseDto {
    private Integer catalogueId;
    private String catalogueName;
    private String catalogueDescription;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<CategoryRequestDto> categories;
}