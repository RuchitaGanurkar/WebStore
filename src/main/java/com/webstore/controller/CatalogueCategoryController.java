package com.webstore.controller;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.service.CatalogueCategoryService;
import com.webstore.service.CatalogueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
@RestController
@RequestMapping("/api/catalogue-categories")
public class CatalogueCategoryController {

    private CatalogueCategoryService catalogueCategoryService;
    @Autowired
    public void setCatalogueCategoryService(CatalogueCategoryService catalogueCategoryService) {
        this.catalogueCategoryService = catalogueCategoryService;
    }


    @PostMapping
    public ResponseEntity<String> createMapping(@RequestBody @Valid CatalogueCategoryRequestDto dto) {
        return catalogueCategoryService.createCatalogueCategory(dto);
    }

    @GetMapping
    public List<CatalogueCategoryResponseDto> getAllMappings() {
        return catalogueCategoryService.getAllCatalogueCategories();
    }
}
