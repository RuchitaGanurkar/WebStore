package com.webstore.service.serviceImplementation;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.entity.Category;
import com.webstore.entity.CatalogueCategory;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.CatalogueRepository;
import com.webstore.repository.CategoryRepository;
import com.webstore.service.CatalogueCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogueCategoryServiceImplementation implements CatalogueCategoryService {

    private final CatalogueCategoryRepository catalogueCategoryRepository;
    private final CatalogueRepository catalogueRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ResponseEntity<String> createCatalogueCategory(CatalogueCategoryRequestDto dto) {

        if (catalogueCategoryRepository.existsByCatalogueCatalogueIdAndCategoryCategoryId(dto.getCatalogueId(), dto.getCategoryId())) {
            return ResponseEntity.badRequest().body("Catalogue-Category mapping already exists.");
        }

        Catalogue catalogue = catalogueRepository.findById(dto.getCatalogueId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Catalogue ID"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        CatalogueCategory catalogueCategory = new CatalogueCategory();
        catalogueCategory.setCatalogue(catalogue);
        catalogueCategory.setCategory(category);
        catalogueCategory.setCreatedBy(dto.getCreatedBy());
        catalogueCategory.setUpdatedBy(dto.getCreatedBy());

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
