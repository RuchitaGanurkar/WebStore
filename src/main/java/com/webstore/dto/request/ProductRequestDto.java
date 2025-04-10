package com.webstore.dto.request;

import lombok.Data;

@Data
public class ProductRequestDto {
    private String productName;
    private String productDescription;
    private Integer categoryId;
    private String createdBy;
}