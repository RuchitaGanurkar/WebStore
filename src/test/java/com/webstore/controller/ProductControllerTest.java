package com.webstore.controller;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockResponse = new ProductResponseDto();
        mockResponse.setProductId(1);
        mockResponse.setProductName("Sample Product");
        mockResponse.setProductDescription("Sample Description");
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateProduct() {
        ProductRequestDto request = new ProductRequestDto();
        request.setProductName("Sample Product");
        request.setProductDescription("Sample Description");
        request.setCategoryId(1);

        when(productService.createProduct(any())).thenReturn(mockResponse);

        ResponseEntity<ProductResponseDto> response = productController.createProduct(request);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Sample Product", response.getBody().getProductName());
    }

    @Test
    void testGetAllProducts() {
        when(productService.getAllProducts()).thenReturn(List.of(mockResponse));

        ResponseEntity<List<ProductResponseDto>> response = productController.getAllProducts();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetProductById() {
        when(productService.getProductById(1)).thenReturn(mockResponse);

        ResponseEntity<ProductResponseDto> response = productController.getProductById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Sample Product", response.getBody().getProductName());
    }

    @Test
    void testUpdateProduct() {
        ProductRequestDto request = new ProductRequestDto();
        request.setProductName("Updated Product");
        request.setProductDescription("Updated Description");
        request.setCategoryId(1);

        when(productService.updateProduct(eq(1), any())).thenReturn(mockResponse);

        ResponseEntity<ProductResponseDto> response = productController.updateProduct(1, request);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productService).deleteProduct(1);

        ResponseEntity<Void> response = productController.deleteProduct(1);

        assertEquals(204, response.getStatusCodeValue());
        verify(productService, times(1)).deleteProduct(1);
    }
}
