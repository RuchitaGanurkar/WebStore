package com.webstore.service.whatsapp.button.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CheckoutStrategyTest {

private ProductBusinessService productService;
private WhatsAppMessageSender messageSender;
private CheckoutStrategy strategy;

@BeforeEach
void setUp() {
    productService = mock(ProductBusinessService.class);
    messageSender = mock(WhatsAppMessageSender.class);
    strategy = new CheckoutStrategy(productService, messageSender);
}

@Test
void testSupports() {
    assertTrue(strategy.supports("checkout_123"));
    assertFalse(strategy.supports("add_cart_123"));
}

@Test
void testHandle_Success() {
    ProductResponseDto product = ProductResponseDto.builder().productId(123).productName("Rice").build();
    when(productService.getProductById(123)).thenReturn(product);

    strategy.handle("12345", "user", "checkout_123");

    verify(messageSender).sendTextMessage(eq("12345"), eq("user"), contains("Rice"));
}

@Test
void testHandle_Failure() {
    strategy.handle("12345", "user", "checkout_invalid");

    verify(messageSender).sendTextMessage(eq("12345"), eq("user"), contains("error"));
}
}
