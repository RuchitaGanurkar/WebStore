package com.webstore.service.product;

import com.webstore.dto.request.product.CatalogueRequestDto;
import com.webstore.dto.response.product.CatalogueResponseDto;
import com.webstore.dto.response.product.CategoryResponseDto;

import java.util.List;

public interface CatalogueService {
    CatalogueResponseDto createCatalogue(CatalogueRequestDto dto);
    List<CatalogueResponseDto> getAllCatalogues();
    CatalogueResponseDto getCatalogueById(Integer id);
    CatalogueResponseDto updateCatalogue(Integer id, CatalogueRequestDto dto);
    void deleteCatalogue(Integer id);
    List<CatalogueResponseDto> searchByName(String name);

    List<CategoryResponseDto> getCategoriesByCatalogueId(Integer catalogueId);
}
