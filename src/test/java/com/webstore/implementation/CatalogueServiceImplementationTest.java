package com.webstore.implementation;

import com.webstore.dto.request.product.CatalogueRequestDto;
import com.webstore.dto.response.product.CatalogueResponseDto;
import com.webstore.entity.product.Catalogue;
import com.webstore.implementation.product.CatalogueServiceImplementation;
import com.webstore.repository.product.CatalogueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceImplementationTest {

    @Mock
    private CatalogueRepository catalogueRepository;

    @InjectMocks
    private CatalogueServiceImplementation catalogueService;

    private CatalogueRequestDto requestDto;
    private Catalogue catalogue;

    @BeforeEach
    void setUp() {
        requestDto = new CatalogueRequestDto();
        requestDto.setCatalogueName("Books");
        requestDto.setCatalogueDescription("All kinds of books");

        catalogue = new Catalogue();
        catalogue.setCatalogueId(1);
        catalogue.setCatalogueName("Books");
        catalogue.setCatalogueDescription("All kinds of books");
        catalogue.setCreatedBy("admin");
        catalogue.setUpdatedBy("admin");
    }

    @Test
    void givenValidRequest_whenCreateCatalogue_thenReturnsSavedCatalogue() {
        when(catalogueRepository.save(any(Catalogue.class))).thenReturn(catalogue);

        CatalogueResponseDto result = catalogueService.createCatalogue(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getCatalogueName()).isEqualTo("Books");

        verify(catalogueRepository).save(any(Catalogue.class));
    }

    @Test
    void whenGetAllCatalogues_thenReturnsCatalogueList() {
        when(catalogueRepository.findAll()).thenReturn(List.of(catalogue));

        List<CatalogueResponseDto> result = catalogueService.getAllCatalogues();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCatalogueName()).isEqualTo("Books");

        verify(catalogueRepository).findAll();
    }

    @Test
    void givenValidId_whenGetCatalogueById_thenReturnsCatalogue() {
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));

        CatalogueResponseDto result = catalogueService.getCatalogueById(1);

        assertThat(result).isNotNull();
        assertThat(result.getCatalogueName()).isEqualTo("Books");

        verify(catalogueRepository).findById(1);
    }

    @Test
    void givenValidIdAndRequest_whenUpdateCatalogue_thenReturnsUpdatedCatalogue() {
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        when(catalogueRepository.save(any(Catalogue.class))).thenReturn(catalogue);

        CatalogueResponseDto result = catalogueService.updateCatalogue(1, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getCatalogueName()).isEqualTo("Books");

        verify(catalogueRepository).findById(1);
        verify(catalogueRepository).save(any(Catalogue.class));
    }

    @Test
    void givenValidId_whenDeleteCatalogue_thenDeletesSuccessfully() {
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));

        catalogueService.deleteCatalogue(1);

        verify(catalogueRepository).findById(1);
        verify(catalogueRepository).delete(catalogue);
    }

    @Test
    void givenName_whenSearchByName_thenReturnsMatchingCatalogues() {
        when(catalogueRepository.findByCatalogueNameContainingIgnoreCase("Books"))
                .thenReturn(List.of(catalogue));

        List<CatalogueResponseDto> result = catalogueService.searchByName("Books");

        assertThat(result).hasSize(1);
        verify(catalogueRepository).findByCatalogueNameContainingIgnoreCase("Books");
    }

//    @Test
//    void givenDescription_whenSearchByDescription_thenReturnsMatchingCatalogues() {
//        when(catalogueRepository.findByCatalogueDescriptionContainingIgnoreCase("books"))
//                .thenReturn(List.of(catalogue));
//
//        List<CatalogueResponseDto> result = catalogueService.searchByDescription("books");
//
//        assertThat(result).hasSize(1);
//        verify(catalogueRepository).findByCatalogueDescriptionContainingIgnoreCase("books");
//    }
}
