package com.webstore.service;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CatalogueCategoryService {
    ResponseEntity<String> createCatalogueCategory(CatalogueCategoryRequestDto dto);

    List<CatalogueCategoryResponseDto> getAllCatalogueCategories();
}
