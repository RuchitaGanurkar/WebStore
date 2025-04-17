package com.webstore.implementation;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.repository.CatalogueRepository;
import com.webstore.service.CatalogueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CatalogueService interface.
 *
 * Uses setter injection for dependencies following best practices.
 * Setter injection provides better testability and flexibility.
 *
 * Exception handling is standardized to use specific exception types
 * that will be caught by the GlobalExceptionHandler.
 */
@Setter
@Service
public class CatalogueServiceImplementation implements CatalogueService {

    private CatalogueRepository catalogueRepository;

    @Override
    public CatalogueResponseDto createCatalogue(CatalogueRequestDto dto) {
        Catalogue catalogue = new Catalogue();
        catalogue.setCatalogueName(dto.getCatalogueName());
        catalogue.setCatalogueDescription(dto.getCatalogueDescription());

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
                .orElseThrow(() -> new EntityNotFoundException("Catalogue not found with ID: " + id));
        return convertToDto(catalogue);
    }

    @Override
    public CatalogueResponseDto updateCatalogue(Integer id, CatalogueRequestDto dto) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catalogue not found with ID: " + id));

        catalogue.setCatalogueName(dto.getCatalogueName());
        catalogue.setCatalogueDescription(dto.getCatalogueDescription());

        return convertToDto(catalogueRepository.save(catalogue));
    }

    @Override
    public void deleteCatalogue(Integer id) {
        if (!catalogueRepository.existsById(id)) {
            throw new EntityNotFoundException("Catalogue not found with ID: " + id);
        }
        catalogueRepository.deleteById(id);
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