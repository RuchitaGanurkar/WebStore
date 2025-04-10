package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CatalogueResponseDto {
    private Integer catalogueId;
    private String catalogueName;
    private String catalogueDescription;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
