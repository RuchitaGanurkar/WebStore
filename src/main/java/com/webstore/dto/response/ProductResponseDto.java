package com.webstore.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.webstore.dto.request.Category;
import lombok.Data;

@Data
public class ProductResponseDto {
    private Integer productId;
    private String productName;
    private String productDescription;
    private Category category;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<ProductPrice> prices;
}