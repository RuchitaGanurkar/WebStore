package com.webstore.controller;

import com.webstore.controller.product.CatalogueController;
import com.webstore.dto.request.product.CatalogueRequestDto;
import com.webstore.dto.response.product.CatalogueResponseDto;
import com.webstore.service.product.CatalogueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogueControllerTest {

    @Mock
    private CatalogueService catalogueService;

    @InjectMocks
    private CatalogueController catalogueController;

    private CatalogueRequestDto requestDto;
    private CatalogueResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = new CatalogueRequestDto();
        requestDto.setCatalogueName("Electronics");
        requestDto.setCatalogueDescription("Gadgets & Devices");

        responseDto = new CatalogueResponseDto();
        responseDto.setCatalogueId(1);
        responseDto.setCatalogueName("Electronics");
        responseDto.setCatalogueDescription("Gadgets & Devices");
    }

    @Test
    void testCreateCatalogue() {
        when(catalogueService.createCatalogue(requestDto)).thenReturn(responseDto);
        ResponseEntity<CatalogueResponseDto> response = catalogueController.createCatalogue(requestDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void testGetAllCatalogues() {
        when(catalogueService.getAllCatalogues()).thenReturn(Arrays.asList(responseDto));
        ResponseEntity<List<CatalogueResponseDto>> response = catalogueController.getAllCatalogues();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetCatalogueById() {
        when(catalogueService.getCatalogueById(1)).thenReturn(responseDto);
        ResponseEntity<CatalogueResponseDto> response = catalogueController.getCatalogueById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void testUpdateCatalogue() {
        when(catalogueService.updateCatalogue(1, requestDto)).thenReturn(responseDto);
        ResponseEntity<CatalogueResponseDto> response = catalogueController.updateCatalogue(1, requestDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void testDeleteCatalogue() {
        doNothing().when(catalogueService).deleteCatalogue(1);
        ResponseEntity<Void> response = catalogueController.deleteCatalogue(1);

        assertEquals(204, response.getStatusCodeValue());
        verify(catalogueService, times(1)).deleteCatalogue(1);
    }

}
