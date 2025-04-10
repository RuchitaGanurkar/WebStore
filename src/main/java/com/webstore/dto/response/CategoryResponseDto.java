package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryResponseDto {
    private Integer categoryId;
    private String categoryName;
    private String categoryDescription;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
