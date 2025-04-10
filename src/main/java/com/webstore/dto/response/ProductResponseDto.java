package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductResponseDto {
    private Integer productId;
    private String productName;
    private String productDescription;
    private Integer categoryId;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
