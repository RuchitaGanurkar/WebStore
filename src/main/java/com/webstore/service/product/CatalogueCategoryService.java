package com.webstore.service.product;

import com.webstore.dto.request.product.CatalogueCategoryRequestDto;
import com.webstore.dto.response.product.CatalogueCategoryResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CatalogueCategoryService {
    ResponseEntity<String> createCatalogueCategory(CatalogueCategoryRequestDto dto);

    List<CatalogueCategoryResponseDto> getAllCatalogueCategories();
}
