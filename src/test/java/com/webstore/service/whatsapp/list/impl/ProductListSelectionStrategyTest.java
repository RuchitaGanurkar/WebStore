package com.webstore.service.whatsapp.list.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductListSelectionStrategyTest {

    @Mock
    private ProductBusinessService productService;

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private PaginationUtil paginationUtil;

    @Mock
    private MessageFormatter formatter;

    @InjectMocks
    private ProductListSelectionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void supports_shouldReturnTrue_whenListIdStartsWithProdP() {
        assertTrue(strategy.supports("prod_p_i3_cXYZ"));
    }

    @Test
    void supports_shouldReturnFalse_whenListIdDoesNotStartWithProdP() {
        assertFalse(strategy.supports("xyz_i3_cXYZ"));
    }

    @Test
    void handle_shouldSendAddToCartMessage_whenProductIsFound() {
        // Given
        String listId = "prod_p_i3_cQ2F0ZWdvcnk"; // category = "Category"
        String from = "919898989898";
        String phoneNumberId = "123456789";

        ProductResponseDto product = new ProductResponseDto();
        product.setProductName("Cool Shirt");

        String decodedCategory = "Category";
        String expectedPrice = "₹499.00";
        String expectedFormattedMessage = String.format(
                "🛒 *Added to Cart!*\n\n" +
                        "📦 Product: %s\n" +
                        "💰 Price: %s\n" +
                        "🏷️ Category: %s\n\n" +
                        "✅ Item added successfully!\n\n" +
                        "Type 'categories' to continue shopping or 'cart' to view your cart.",
                product.getProductName(), expectedPrice, decodedCategory
        );

        when(paginationUtil.decodeFromBase64("Q2F0ZWdvcnk")).thenReturn(decodedCategory);
        when(productService.getProductById(3)).thenReturn(product);
        when(productService.getProductPriceDisplay(3)).thenReturn(expectedPrice);
        when(formatter.formatAddToCartMessage("Cool Shirt", expectedPrice, decodedCategory))
                .thenReturn(expectedFormattedMessage);

        // When
        strategy.handle(phoneNumberId, from, listId);

        // Then
        verify(messageSender).sendTextMessage(phoneNumberId, from, expectedFormattedMessage);
    }

    @Test
    void handle_shouldSendProductNotFoundMessage_whenProductIsNull() {
        // Given
        String listId = "prod_p_i3_cQ2F0ZWdvcnk"; // category = "Category"
        String from = "919898989898";
        String phoneNumberId = "123456789";

        when(paginationUtil.decodeFromBase64("Q2F0ZWdvcnk")).thenReturn("Category");
        when(productService.getProductById(3)).thenReturn(null);

        // When
        strategy.handle(phoneNumberId, from, listId);

        // Then
        verify(messageSender).sendTextMessage(phoneNumberId, from, "Product not found.");
    }

    @Test
    void handle_shouldSendErrorMessage_whenExceptionOccurs() {
        // Given
        String listId = "prod_p_iX_invalidbase64"; // malformed id
        String from = "919898989898";
        String phoneNumberId = "123456789";

        when(paginationUtil.decodeFromBase64("invalidbase64"))
                .thenThrow(new IllegalArgumentException("Invalid Base64"));

        // When
        strategy.handle(phoneNumberId, from, listId);

        // Then
        verify(messageSender).sendTextMessage(phoneNumberId, from, "Unable to process selection.");
    }

    @Test
    void handle_shouldDoNothing_whenListIdHasInsufficientParts() {
        // Given
        String listId = "prod_p_i3"; // Missing category part
        String from = "919898989898";
        String phoneNumberId = "123456789";

        // When
        strategy.handle(phoneNumberId, from, listId);

        // Then
        verifyNoInteractions(productService, paginationUtil, formatter);
        verify(messageSender, never()).sendTextMessage(any(), any(), any());
    }
}