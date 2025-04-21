package com.webstore.implementation;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.entity.Category;
import com.webstore.entity.CatalogueCategory;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.CatalogueRepository;
import com.webstore.repository.CategoryRepository;
import com.webstore.util.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogueCategoryServiceImplementationTest {

    @Mock
    private CatalogueCategoryRepository catalogueCategoryRepository;

    @Mock
    private CatalogueRepository catalogueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogueCategoryServiceImplementation service;

    private CatalogueCategoryRequestDto requestDto;
    private Catalogue catalogue;
    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = new CatalogueCategoryRequestDto();
        requestDto.setCatalogueId(1);
        requestDto.setCategoryId(2);

        catalogue = new Catalogue();
        catalogue.setCatalogueId(1);
        catalogue.setCatalogueName("Electronics");

        category = new Category();
        category.setCategoryId(2);
        category.setCategoryName("Mobile Phones");
    }

    @Test
    void testCreateCatalogueCategory_Success() {
        when(catalogueCategoryRepository.existsByCatalogueCatalogueIdAndCategoryCategoryId(1, 2)).thenReturn(false);
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(category));
        when(catalogueCategoryRepository.save(any(CatalogueCategory.class))).thenReturn(new CatalogueCategory());

        ResponseEntity<String> response = service.createCatalogueCategory(requestDto);

        assertEquals("Catalogue-Category mapping created successfully.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testCreateCatalogueCategory_AlreadyExists() {
        when(catalogueCategoryRepository.existsByCatalogueCatalogueIdAndCategoryCategoryId(1, 2)).thenReturn(true);

        ResponseEntity<String> response = service.createCatalogueCategory(requestDto);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Catalogue-Category mapping already exists.", response.getBody());
    }

    @Test
    void testGetAllCatalogueCategories() {
        CatalogueCategory catalogueCategory = new CatalogueCategory();
        catalogueCategory.setCatalogue(catalogue);
        catalogueCategory.setCategory(category);
        catalogueCategory.setCreatedBy("admin");
        catalogueCategory.setUpdatedBy("admin");

        when(catalogueCategoryRepository.findAll()).thenReturn(Collections.singletonList(catalogueCategory));

        List<CatalogueCategoryResponseDto> result = service.getAllCatalogueCategories();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCatalogueName());
        assertEquals("Mobile Phones", result.get(0).getCategoryName());
    }
}
