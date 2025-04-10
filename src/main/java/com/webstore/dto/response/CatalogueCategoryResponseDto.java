package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CatalogueCategoryResponseDto {
    private Integer catalogueCategoryId;
    private Integer catalogueId;
    private Integer categoryId;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
