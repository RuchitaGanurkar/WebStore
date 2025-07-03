package com.webstore.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.webstore.dto.request.CategoryRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Integer productId;
    private String productName;
    private String productDescription;
    private CategoryRequestDto category;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<ProductPriceResponseDto> prices;
}