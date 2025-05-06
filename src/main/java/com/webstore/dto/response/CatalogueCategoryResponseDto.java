package com.webstore.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CatalogueCategoryResponseDto {
    private Integer catalogueCategoryId;
    private Integer catalogueId;
    private String catalogueName;
    private Integer categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
