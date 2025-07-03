package com.webstore.service.whatsapp.button.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ProductViewStrategyTest {

    private ProductBusinessService productService;
    private ProductFlowService productFlowService;
    private WhatsAppMessageSender messageSender;
    private ProductViewStrategy strategy;

    @BeforeEach
    void setUp() {
        productService = mock(ProductBusinessService.class);
        productFlowService = mock(ProductFlowService.class);
        messageSender = mock(WhatsAppMessageSender.class);

        strategy = new ProductViewStrategy(productService, productFlowService, messageSender);
    }

    @Test
    void testSupports() {
        assertTrue(strategy.supports("view_product_123"));
        assertFalse(strategy.supports("cat_1"));
    }

    @Test
    void testHandle_Success() {
        ProductResponseDto product = ProductResponseDto.builder().productId(1).productName("Milk").build();
        when(productService.getProductById(1)).thenReturn(product);

        strategy.handle("12345", "user", "view_product_1");

        verify(productFlowService).sendProductDetails(any(), eq("12345"), eq("user"), eq("Milk"));
    }

    @Test
    void testHandle_ProductNotFound() {
        when(productService.getProductById(anyInt())).thenReturn(null);

        strategy.handle("12345", "user", "view_product_99");

        verify(messageSender).sendTextMessage(eq("12345"), eq("user"), contains("not found"));
    }

    @Test
    void testHandle_Exception() {
        when(productService.getProductById(anyInt())).thenThrow(new RuntimeException());

        strategy.handle("12345", "user", "view_product_99");

        verify(messageSender).sendTextMessage(eq("12345"), eq("user"), contains("Something went wrong"));
    }
}

