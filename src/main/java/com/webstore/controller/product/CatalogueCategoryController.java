package com.webstore.controller.product;

import com.webstore.dto.request.product.CatalogueCategoryRequestDto;
import com.webstore.dto.response.product.CatalogueCategoryResponseDto;
import com.webstore.service.product.CatalogueCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogue-categories")
public class CatalogueCategoryController {

    private CatalogueCategoryService catalogueCategoryService;

    @Autowired
    @Qualifier("catalogueCategoryServiceImplementation")
    public void setCatalogueCategoryService(CatalogueCategoryService catalogueCategoryService) {
        this.catalogueCategoryService = catalogueCategoryService;
    }

    @PostMapping
    public ResponseEntity<String> createMapping(@RequestBody @Valid CatalogueCategoryRequestDto dto) {
        return catalogueCategoryService.createCatalogueCategory(dto);
    }

    @GetMapping
    public ResponseEntity<List<CatalogueCategoryResponseDto>> getAllMappings() {
        List<CatalogueCategoryResponseDto> mappings = catalogueCategoryService.getAllCatalogueCategories();
        if (mappings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mappings);
    }
}
