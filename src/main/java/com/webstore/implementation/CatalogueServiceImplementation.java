package com.webstore.implementation;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.CatalogueRepository;
import com.webstore.service.CatalogueService;
import com.webstore.service.CategoryService;
import com.webstore.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogueServiceImplementation implements CatalogueService {

    @Autowired
    private final CatalogueRepository catalogueRepository;
    @Autowired
    private final CatalogueCategoryRepository catalogueCategoryRepository;
    @Autowired
    private final CategoryService categoryService;


    public CatalogueServiceImplementation(CatalogueRepository catalogueRepository,
                                          CatalogueCategoryRepository catalogueCategoryRepository,
                                          CategoryService categoryService) {
        this.catalogueRepository = catalogueRepository;
        this.catalogueCategoryRepository = catalogueCategoryRepository;
        this.categoryService = categoryService;
    }

    @Override
    public CatalogueResponseDto createCatalogue(CatalogueRequestDto dto) {
        Catalogue catalogue = new Catalogue();
        catalogue.setCatalogueName(dto.getCatalogueName());
        catalogue.setCatalogueDescription(dto.getCatalogueDescription());

        String currentUser = AuthUtils.getCurrentUsername(); // 🔑 Fetch user
        catalogue.setCreatedBy(currentUser);
        catalogue.setUpdatedBy(currentUser);

        return convertToDto(catalogueRepository.save(catalogue));
    }

    @Override
    public List<CatalogueResponseDto> getAllCatalogues() {
        return catalogueRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CatalogueResponseDto getCatalogueById(Integer id) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalogue not found"));
        return convertToDto(catalogue);
    }

    @Override
    public CatalogueResponseDto updateCatalogue(Integer id, CatalogueRequestDto dto) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalogue not found"));

        catalogue.setCatalogueName(dto.getCatalogueName());
        catalogue.setCatalogueDescription(dto.getCatalogueDescription());
        catalogue.setUpdatedBy(AuthUtils.getCurrentUsername());

        return convertToDto(catalogueRepository.save(catalogue));
    }

    @Override
    public void deleteCatalogue(Integer id) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalogue not found"));
        catalogueRepository.delete(catalogue);
    }

    @Override
    public List<CatalogueResponseDto> searchByName(String name) {
        return catalogueRepository.findByCatalogueNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CatalogueResponseDto> searchByDescription(String description) {
        return catalogueRepository.findByCatalogueDescriptionContainingIgnoreCase(description)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getCategoriesByCatalogueId(Integer catalogueId) {
        // Find the catalogue
        Catalogue catalogue = catalogueRepository.findById(catalogueId)
                .orElseThrow(() -> new RuntimeException("Catalogue not found with ID: " + catalogueId));

        // Get category IDs from catalogue_category mappings
        List<Integer> categoryIds = catalogue.getCatalogueCategories().stream()
                .map(cc -> cc.getCategory().getCategoryId())
                .collect(Collectors.toList());

        // Get detailed category info for each ID
        List<CategoryResponseDto> categories = new ArrayList<>();
        for (Integer categoryId : categoryIds) {
            try {
                CategoryResponseDto category = categoryService.getCategoryById(categoryId);
                categories.add(category);
            } catch (Exception e) {
                // Skip categories that can't be found
            }
        }

        return categories;
    }

    private CatalogueResponseDto convertToDto(Catalogue catalogue) {
        CatalogueResponseDto dto = new CatalogueResponseDto();
        dto.setCatalogueId(catalogue.getCatalogueId());
        dto.setCatalogueName(catalogue.getCatalogueName());
        dto.setCatalogueDescription(catalogue.getCatalogueDescription());
        dto.setCreatedAt(catalogue.getCreatedAt());
        dto.setUpdatedAt(catalogue.getUpdatedAt());
        dto.setCreatedBy(catalogue.getCreatedBy());
        dto.setUpdatedBy(catalogue.getUpdatedBy());
        return dto;
    }
}
