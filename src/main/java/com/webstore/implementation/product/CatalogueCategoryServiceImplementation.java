package com.webstore.implementation.product;

import com.webstore.dto.request.product.CatalogueCategoryRequestDto;
import com.webstore.dto.response.product.CatalogueCategoryResponseDto;
import com.webstore.entity.product.Catalogue;
import com.webstore.entity.product.Category;
import com.webstore.entity.product.CatalogueCategory;
import com.webstore.repository.product.CatalogueCategoryRepository;
import com.webstore.repository.product.CatalogueRepository;
import com.webstore.repository.product.CategoryRepository;
import com.webstore.service.product.CatalogueCategoryService;
import com.webstore.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("catalogueCategoryServiceImplementation")
public class CatalogueCategoryServiceImplementation implements CatalogueCategoryService {

    private final CatalogueCategoryRepository catalogueCategoryRepository;
    private final CatalogueRepository catalogueRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public CatalogueCategoryServiceImplementation(CatalogueCategoryRepository catalogueCategoryRepository, CatalogueRepository catalogueRepository, CategoryRepository categoryRepository) {
        this.catalogueCategoryRepository = catalogueCategoryRepository;
        this.catalogueRepository = catalogueRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ResponseEntity<String> createCatalogueCategory(CatalogueCategoryRequestDto dto) {
        if (catalogueCategoryRepository.existsByCatalogueCatalogueIdAndCategoryCategoryId(dto.getCatalogueId(), dto.getCategoryId())) {
            return ResponseEntity.badRequest().body("Catalogue-Category mapping already exists.");
        }

        Catalogue catalogue = catalogueRepository.findById(dto.getCatalogueId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Catalogue ID"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        String currentUsername = AuthUtils.getCurrentUsername();

        CatalogueCategory catalogueCategory = new CatalogueCategory();
        catalogueCategory.setCatalogue(catalogue);
        catalogueCategory.setCategory(category);
        catalogueCategory.setCreatedBy(currentUsername);
        catalogueCategory.setUpdatedBy(currentUsername);

        catalogueCategoryRepository.save(catalogueCategory);

        return ResponseEntity.ok("Catalogue-Category mapping created successfully.");
    }


    @Override
    public List<CatalogueCategoryResponseDto> getAllCatalogueCategories() {
        return catalogueCategoryRepository.findAll().stream().map(entity -> {
            CatalogueCategoryResponseDto dto = new CatalogueCategoryResponseDto();
            dto.setCatalogueCategoryId(entity.getCatalogueCategoryId());
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
