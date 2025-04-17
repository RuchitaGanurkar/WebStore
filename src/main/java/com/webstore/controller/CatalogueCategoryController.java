package com.webstore.controller;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.service.CatalogueCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalogue-categories")
public class CatalogueCategoryController {

    private final CatalogueCategoryService catalogueCategoryService;

    @PostMapping
    public ResponseEntity<String> createMapping(@RequestBody @Valid CatalogueCategoryRequestDto dto) {
        return catalogueCategoryService.createCatalogueCategory(dto);
    }

    @GetMapping
    public List<CatalogueCategoryResponseDto> getAllMappings() {
        return catalogueCategoryService.getAllCatalogueCategories();
    }
}
