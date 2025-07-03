package com.webstore.service.whatsapp.button.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class AddToCartStrategyTest {

    private ProductBusinessService productService;
    private WhatsAppMessageSender messageSender;
    private AddToCartStrategy strategy;

    @BeforeEach
    void setUp() {
        productService = mock(ProductBusinessService.class);
        messageSender = mock(WhatsAppMessageSender.class);
        strategy = new AddToCartStrategy(productService, messageSender);
    }

    @Test
    void testSupports() {
        assertTrue(strategy.supports("add_cart_101"));
        assertFalse(strategy.supports("cat_1"));
    }

    @Test
    void testHandle_Success() {
        ProductResponseDto product = ProductResponseDto.builder().productId(101).productName("Apple").build();
        when(productService.getProductById(101)).thenReturn(product);

        strategy.handle("12345", "user-1", "add_cart_101");

        verify(messageSender).sendTextMessage(eq("12345"), eq("user-1"), contains("Apple"));
    }

    @Test
    void testHandle_Error() {
        when(productService.getProductById(999)).thenThrow(new RuntimeException());

        strategy.handle("12345", "user-1", "add_cart_999");

        verify(messageSender).sendTextMessage(eq("12345"), eq("user-1"), contains("error"));
    }
}

