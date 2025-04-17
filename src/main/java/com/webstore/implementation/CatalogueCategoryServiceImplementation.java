package com.webstore.implementation;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.entity.Category;
import com.webstore.entity.CatalogueCategory;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.CatalogueRepository;
import com.webstore.repository.CategoryRepository;
import com.webstore.service.CatalogueCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CatalogueCategoryService interface.
 *
 * Uses setter injection for dependencies following best practices.
 * Exception handling is standardized to use specific exception types
 * that will be caught by the GlobalExceptionHandler.
 */
@Setter
@Service
public class CatalogueCategoryServiceImplementation implements CatalogueCategoryService {

    private CatalogueCategoryRepository catalogueCategoryRepository;
    private CatalogueRepository catalogueRepository;
    private CategoryRepository categoryRepository;


    @Override
    public ResponseEntity<String> createCatalogueCategory(CatalogueCategoryRequestDto dto) {
        if (catalogueCategoryRepository.existsByCatalogueCatalogueIdAndCategoryCategoryId(
                dto.getCatalogueId(), dto.getCategoryId())) {
            throw new IllegalArgumentException("Catalogue-Category mapping already exists");
        }

        Catalogue catalogue = catalogueRepository.findById(dto.getCatalogueId())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Catalogue ID: " + dto.getCatalogueId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Category ID: " + dto.getCategoryId()));

        CatalogueCategory catalogueCategory = new CatalogueCategory();
        catalogueCategory.setCatalogue(catalogue);
        catalogueCategory.setCategory(category);

        catalogueCategoryRepository.save(catalogueCategory);

        return ResponseEntity.ok("Catalogue-Category mapping created successfully.");
    }

    @Override
    public List<CatalogueCategoryResponseDto> getAllCatalogueCategories() {
        return catalogueCategoryRepository.findAll().stream().map(entity -> {
            CatalogueCategoryResponseDto dto = new CatalogueCategoryResponseDto();
            dto.setCatalogueId(entity.getCatalogue().getCatalogueId());
            dto.setCatalogueName(entity.getCatalogue().getCatalogueName());
            dto.setCategoryId(entity.getCategory().getCategoryId());
            dto.setCategoryName(entity.getCategory().getCategoryName());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setCreatedBy(entity.getCreatedBy());
            dto.setUpdatedAt(entity.getUpdatedAt());
            dto.setUpdatedBy(entity.getUpdatedBy());
            return dto;
        }).collect(Collectors.toList());
    }
}