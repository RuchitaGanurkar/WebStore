package com.webstore.implementation;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.repository.CatalogueRepository;
import com.webstore.util.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogueServiceImplementationTest {

    @InjectMocks
    private CatalogueServiceImplementation catalogueService;

    @Mock
    private CatalogueRepository catalogueRepository;

    private CatalogueRequestDto requestDto;
    private Catalogue catalogue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    void testCreateCatalogue() {
        when(catalogueRepository.save(any(Catalogue.class))).thenReturn(catalogue);

        CatalogueResponseDto result = catalogueService.createCatalogue(requestDto);

        assertNotNull(result);
        assertEquals("Books", result.getCatalogueName());
    }

    @Test
    void testGetAllCatalogues() {
        when(catalogueRepository.findAll()).thenReturn(List.of(catalogue));
        List<CatalogueResponseDto> result = catalogueService.getAllCatalogues();

        assertEquals(1, result.size());
        assertEquals("Books", result.get(0).getCatalogueName());
    }

    @Test
    void testGetCatalogueById() {
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        CatalogueResponseDto result = catalogueService.getCatalogueById(1);

        assertNotNull(result);
        assertEquals("Books", result.getCatalogueName());
    }

    @Test
    void testUpdateCatalogue() {
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        when(catalogueRepository.save(any(Catalogue.class))).thenReturn(catalogue);

        CatalogueResponseDto result = catalogueService.updateCatalogue(1, requestDto);

        assertNotNull(result);
        assertEquals("Books", result.getCatalogueName());
    }

    @Test
    void testDeleteCatalogue() {
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        doNothing().when(catalogueRepository).delete(catalogue);

        assertDoesNotThrow(() -> catalogueService.deleteCatalogue(1));
        verify(catalogueRepository, times(1)).delete(catalogue);
    }

    @Test
    void testSearchByName() {
        when(catalogueRepository.findByCatalogueNameContainingIgnoreCase("Books"))
                .thenReturn(List.of(catalogue));

        List<CatalogueResponseDto> result = catalogueService.searchByName("Books");

        assertEquals(1, result.size());
    }

    @Test
    void testSearchByDescription() {
        when(catalogueRepository.findByCatalogueDescriptionContainingIgnoreCase("books"))
                .thenReturn(List.of(catalogue));

        List<CatalogueResponseDto> result = catalogueService.searchByDescription("books");

        assertEquals(1, result.size());
    }
}
