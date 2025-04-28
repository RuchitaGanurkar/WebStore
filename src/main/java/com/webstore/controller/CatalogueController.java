package com.webstore.controller;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.service.CatalogueService;
import jakarta.validation.Valid;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
@RestController
@RequestMapping("/api/catalogues")
public class CatalogueController {

    private CatalogueService catalogueService;

    @Autowired
    @Qualifier("catalogueServiceImplementation")  // Assuming you have a specific implementation to inject
    public void setCatalogueService(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @PostMapping
    public ResponseEntity<CatalogueResponseDto> createCatalogue(@RequestBody @Valid CatalogueRequestDto dto) {
        return ResponseEntity.ok(catalogueService.createCatalogue(dto));
    }

    @GetMapping
    public ResponseEntity<List<CatalogueResponseDto>> getAllCatalogues() {
        return ResponseEntity.ok(catalogueService.getAllCatalogues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueResponseDto> getCatalogueById(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogueService.getCatalogueById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogueResponseDto> updateCatalogue(
            @PathVariable Integer id,
            @RequestBody @Valid CatalogueRequestDto dto) {
        return ResponseEntity.ok(catalogueService.updateCatalogue(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalogue(@PathVariable Integer id) {
        catalogueService.deleteCatalogue(id);
        return ResponseEntity.noContent().build();
    }
}
