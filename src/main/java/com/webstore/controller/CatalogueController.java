package com.webstore.controller;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.service.CatalogueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/catalogues")
@RequiredArgsConstructor
public class CatalogueController {

    private final CatalogueService catalogueService;

    @PostMapping
    public ResponseEntity<CatalogueResponseDto> createCatalogue(@RequestBody @Valid CatalogueRequestDto dto) {
        log.info("Creating new catalogue: {}", dto.getCatalogueName());

        CatalogueResponseDto createdCatalogue = catalogueService.createCatalogue(dto);
        log.info("Catalogue created successfully with ID: {}", createdCatalogue.getCatalogueId());

        if (log.isDebugEnabled()) {
            log.debug("Created catalogue details: name={}, description={}",
                    createdCatalogue.getCatalogueName(),
                    createdCatalogue.getCatalogueDescription());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCatalogue);
    }

    @GetMapping
    public ResponseEntity<List<CatalogueResponseDto>> getAllCatalogues() {
        log.info("Retrieving all catalogues");

        List<CatalogueResponseDto> catalogues = catalogueService.getAllCatalogues();
        log.info("Retrieved {} catalogues", catalogues.size());

        if (log.isDebugEnabled() && !catalogues.isEmpty()) {
            log.debug("Catalogues retrieved: {}",
                    catalogues.stream()
                            .map(CatalogueResponseDto::getCatalogueName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }

        return ResponseEntity.ok(catalogues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueResponseDto> getCatalogueById(@PathVariable Integer id) {
        log.info("Retrieving catalogue with ID: {}", id);

        CatalogueResponseDto catalogue = catalogueService.getCatalogueById(id);
        log.info("Retrieved catalogue: {}", catalogue.getCatalogueName());

        return ResponseEntity.ok(catalogue);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogueResponseDto> updateCatalogue(
            @PathVariable Integer id,
            @RequestBody @Valid CatalogueRequestDto dto) {
        log.info("Updating catalogue with ID: {}", id);
        log.debug("Update details: name={}, description={}", dto.getCatalogueName(), dto.getCatalogueDescription());

        CatalogueResponseDto updatedCatalogue = catalogueService.updateCatalogue(id, dto);
        log.info("Catalogue with ID: {} updated successfully", id);

        return ResponseEntity.ok(updatedCatalogue);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalogue(@PathVariable Integer id) {
        log.info("Deleting catalogue with ID: {}", id);

        catalogueService.deleteCatalogue(id);
        log.info("Catalogue with ID: {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<CatalogueResponseDto>> searchByName(@RequestParam String name) {
        log.info("Searching catalogues by name: {}", name);

        List<CatalogueResponseDto> results = catalogueService.searchByName(name);
        log.info("Found {} catalogues matching name search: {}", results.size(), name);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/description")
    public ResponseEntity<List<CatalogueResponseDto>> searchByDescription(@RequestParam String description) {
        log.info("Searching catalogues by description: {}", description);

        List<CatalogueResponseDto> results = catalogueService.searchByDescription(description);
        log.info("Found {} catalogues matching description search: {}", results.size(), description);

        return ResponseEntity.ok(results);
    }
}