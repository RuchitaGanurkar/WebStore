package com.webstore.service;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.dto.response.CategoryResponseDto;

import java.util.List;

public interface CatalogueService {
    CatalogueResponseDto createCatalogue(CatalogueRequestDto dto);
    List<CatalogueResponseDto> getAllCatalogues();
    CatalogueResponseDto getCatalogueById(Integer id);
    CatalogueResponseDto updateCatalogue(Integer id, CatalogueRequestDto dto);
    void deleteCatalogue(Integer id);
    List<CatalogueResponseDto> searchByName(String name);
    List<CatalogueResponseDto> searchByDescription(String description);
    List<CategoryResponseDto> getCategoriesByCatalogueId(Integer catalogueId);
}
