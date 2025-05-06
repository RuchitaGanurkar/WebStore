package com.webstore.controller;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.service.CatalogueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueControllerTest {

    @Mock
    private CatalogueService catalogueService;

    @InjectMocks
    private CatalogueController catalogueController;

    private CatalogueRequestDto requestDto;
    private CatalogueResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new CatalogueRequestDto();
        requestDto.setCatalogueName("Electronics");
        requestDto.setCatalogueDescription("Gadgets & Devices");

        responseDto = new CatalogueResponseDto();
        responseDto.setCatalogueId(1);
        responseDto.setCatalogueName("Electronics");
        responseDto.setCatalogueDescription("Gadgets & Devices");
    }

    @Test
    void givenValidRequest_whenCreateCatalogue_thenReturnsCreatedCatalogue() {
        when(catalogueService.createCatalogue(requestDto)).thenReturn(responseDto);

        ResponseEntity<CatalogueResponseDto> response = catalogueController.createCatalogue(requestDto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseDto);

        verify(catalogueService).createCatalogue(requestDto);
    }

    @Test
    void whenGetAllCatalogues_thenReturnsCatalogueList() {
        when(catalogueService.getAllCatalogues()).thenReturn(List.of(responseDto));

        ResponseEntity<List<CatalogueResponseDto>> response = catalogueController.getAllCatalogues();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);

        verify(catalogueService).getAllCatalogues();
    }

    @Test
    void givenValidId_whenGetCatalogueById_thenReturnsCatalogue() {
        when(catalogueService.getCatalogueById(1)).thenReturn(responseDto);

        ResponseEntity<CatalogueResponseDto> response = catalogueController.getCatalogueById(1);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseDto);

        verify(catalogueService).getCatalogueById(1);
    }

    @Test
    void givenValidIdAndRequest_whenUpdateCatalogue_thenReturnsUpdatedCatalogue() {
        when(catalogueService.updateCatalogue(1, requestDto)).thenReturn(responseDto);

        ResponseEntity<CatalogueResponseDto> response = catalogueController.updateCatalogue(1, requestDto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseDto);

        verify(catalogueService).updateCatalogue(1, requestDto);
    }

    @Test
    void givenValidId_whenDeleteCatalogue_thenReturnsNoContent() {
        ResponseEntity<Void> response = catalogueController.deleteCatalogue(1);

        assertThat(response.getStatusCodeValue()).isEqualTo(204);

        verify(catalogueService).deleteCatalogue(1);
    }
}
