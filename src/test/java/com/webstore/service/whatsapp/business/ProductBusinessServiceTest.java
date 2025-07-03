package com.webstore.service.whatsapp.business;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.Currency;
import com.webstore.entity.ProductPrice;
import com.webstore.repository.ProductPriceRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductBusinessServiceTest {

    private ProductRepository productRepository;
    private ProductPriceRepository productPriceRepository;
    private ProductService productService;
    private CategoryBusinessService categoryBusinessService;

    private ProductBusinessService productBusinessService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productPriceRepository = mock(ProductPriceRepository.class);
        productService = mock(ProductService.class);
        categoryBusinessService = mock(CategoryBusinessService.class);

        productBusinessService = new ProductBusinessService(
                productRepository, productPriceRepository, productService, categoryBusinessService);
    }

    @Test
    void testGetProductNamesByCategory_WithValidId() {
        when(productRepository.findProductNamesByCategoryId(101)).thenReturn(List.of("Apple", "Banana"));

        List<String> result = productBusinessService.getProductNamesByCategory(101);

        assertEquals(2, result.size());
    }

    @Test
    void testGetProductNamesByCategory_WithNullId_ShouldReturnEmptyList() {
        assertTrue(productBusinessService.getProductNamesByCategory(null).isEmpty());
    }

    @Test
    void testGetProductNamesByCategoryName() {
        when(categoryBusinessService.getCategoryIdByName("Fruits")).thenReturn(101);
        when(productRepository.findProductNamesByCategoryId(101)).thenReturn(List.of("Apple"));

        List<String> result = productBusinessService.getProductNamesByCategoryName("Fruits");

        assertEquals(1, result.size());
    }

    @Test
    void testGetProductIdByName() {
        when(productRepository.findProductIdByProductName("Apple")).thenReturn(201);

        assertEquals(201, productBusinessService.getProductIdByName("Apple"));
    }

    @Test
    void testGetProductById() {
        ProductResponseDto dto = ProductResponseDto.builder().productId(101).productName("Apple").build();
        when(productService.getProductById(101)).thenReturn(dto);

        assertEquals("Apple", productBusinessService.getProductById(101).getProductName());
    }

    @Test
    void testGetProductPriceDisplay_WhenInrExists() {
        ProductPrice inrPrice = new ProductPrice();
        inrPrice.setPriceAmount(BigInteger.valueOf(12345L));
        Currency currency = new Currency();
        currency.setCurrencyCode("INR");
        currency.setCurrencySymbol("₹");
        inrPrice.setCurrency(currency);

        when(productPriceRepository.findByProductProductId(101)).thenReturn(List.of(inrPrice));

        String priceDisplay = productBusinessService.getProductPriceDisplay(101);
        assertEquals("₹ 123.45", priceDisplay);
    }

    @Test
    void testGetProductPriceDisplay_WhenNoPrices() {
        when(productPriceRepository.findByProductProductId(101)).thenReturn(Collections.emptyList());

        assertEquals("Price not available", productBusinessService.getProductPriceDisplay(101));
    }

    @Test
    void testGetProductPrices() {
        List<ProductPrice> mockPrices = List.of(new ProductPrice());
        when(productPriceRepository.findByProductProductId(101)).thenReturn(mockPrices);

        assertEquals(1, productBusinessService.getProductPrices(101).size());
    }

    @Test
    void testShouldUseButtonsForProducts() {
        assertTrue(productBusinessService.shouldUseButtonsForProducts(List.of("A", "B", "C")));
        assertFalse(productBusinessService.shouldUseButtonsForProducts(List.of("A", "B", "C", "D")));
    }
}
