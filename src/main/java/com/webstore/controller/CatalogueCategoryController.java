package com.webstore.controller;

import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.service.CatalogueCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/catalogue-categories")
@RequiredArgsConstructor
public class CatalogueCategoryController {

    private final CatalogueCategoryService catalogueCategoryService;

    @PostMapping
    public ResponseEntity<String> createMapping(
            @RequestBody @Valid CatalogueCategoryRequestDto dto
    ) {
        log.info("Creating new catalogue-category mapping: catalogueId={}, categoryId={}",
                dto.getCatalogueId(), dto.getCategoryId());

        ResponseEntity<String> response = catalogueCategoryService.createCatalogueCategory(dto);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Catalogue-category mapping created successfully");
            if (log.isDebugEnabled()) {
                log.debug("Response body: {}", response.getBody());
            }
        } else {
            log.warn("Failed to create catalogue-category mapping: status={}", response.getStatusCode());
            if (log.isDebugEnabled() && response.getBody() != null) {
                log.debug("Error details: {}", response.getBody());
            }
        }

        return response;
    }

    @GetMapping
    public List<CatalogueCategoryResponseDto> getAllMappings() {
        log.info("Retrieving all catalogue-category mappings");

        List<CatalogueCategoryResponseDto> mappings = catalogueCategoryService.getAllCatalogueCategories();
        log.info("Retrieved {} catalogue-category mappings", mappings.size());

        if (log.isDebugEnabled() && !mappings.isEmpty()) {
            log.debug("First few mappings: {}",
                    mappings.stream()
                            .limit(5)
                            .map(m -> "catalogueId=" + m.getCatalogueId() + ", categoryId=" + m.getCategoryId())
                            .reduce((a, b) -> a + "; " + b)
                            .orElse(""));

            if (mappings.size() > 5) {
                log.debug("... and {} more mappings", mappings.size() - 5);
            }
        }

        return mappings;
    }
}