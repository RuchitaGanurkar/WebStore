package com.webstore.service.whatsapp.flow;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.exception.ProductNotFoundException;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import com.webstore.service.whatsapp.strategy.ProductDisplayStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductFlowServiceTest {

    @Mock
    private CategoryBusinessService categoryService;

    @Mock
    private ProductBusinessService productService;

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private MessageBuilderService messageBuilder;

    @Mock
    private MessageFormatter formatter;

    @Mock
    private PaginationUtil paginationUtil;

    @InjectMocks
    private ProductFlowService productFlowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendProductDetails_validProduct_sendsMessage() {
        // Arrange
        String version = "v1";
        String phoneNumberId = "123";
        String recipientPhoneNumber = "9876543210";
        String productName = "Sample Product";
        int productId = 1;

        // ✅ Updated CategoryRequestDto with required fields only
        CategoryRequestDto categoryDto = new CategoryRequestDto();
        categoryDto.setCategoryName("Electronics");
        categoryDto.setCategoryDescription("All electronic gadgets");

        ProductResponseDto productDto = ProductResponseDto.builder()
                .productId(productId)
                .productName(productName)
                .productDescription("A great gadget")
                .category(categoryDto) // ✅ Using updated CategoryRequestDto
                .prices(Collections.emptyList())
                .build();

        WhatsAppRequestDto.Button button = new WhatsAppRequestDto.Button();
        WhatsAppRequestDto builtRequest = new WhatsAppRequestDto();

        when(productService.getProductIdByName(productName)).thenReturn(productId);
        when(productService.getProductById(productId)).thenReturn(productDto);
        when(productService.getProductPriceDisplay(productId)).thenReturn("₹99.00");
        when(formatter.formatProductDetails(any(), any(), any(), any()))
                .thenReturn("Formatted Product Details");
        when(messageBuilder.createButton(anyString(), anyString())).thenReturn(button);
        when(messageBuilder.buildButtonMessage(anyString(), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(builtRequest);

        // Act
        productFlowService.sendProductDetails(version, phoneNumberId, recipientPhoneNumber, productName);

        // Assert
        verify(messageSender).sendMessage(eq(phoneNumberId), eq(builtRequest), eq("Product details message"));
    }


    @Test
    void testSendProductDetails_invalidProduct_throwsException() {
        // Arrange
        String productName = "Invalid Product";
        when(productService.getProductIdByName(productName)).thenReturn(null);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                productFlowService.sendProductDetails("v1", "123", "9876543210", productName));
    }
}
