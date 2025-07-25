package com.webstore.service.whatsapp.flow;

import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.business.CartBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.MessageFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFlowServiceTest {

    @Mock
    private CartBusinessService cartBusinessService;

    @Mock
    private ProductBusinessService productBusinessService;

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private MessageBuilderService messageBuilderService;

    @Mock
    private MessageFormatter messageFormatter;

    private CartFlowService cartFlowService;

    @BeforeEach
    void setUp() {
        cartFlowService = new CartFlowService(
                cartBusinessService,
                productBusinessService,
                messageSender,
                messageBuilderService,
                messageFormatter
        );
    }

    @Test
    void testAddToCart() {
        // Mock ProductValidationResult
        ProductBusinessService.ProductValidationResult validationResult =
                mock(ProductBusinessService.ProductValidationResult.class);
        when(validationResult.isValid()).thenReturn(true);
        // Remove getMessage() stubbing as it's not used when isValid() returns true

        // Mock product validation
        when(productBusinessService.validateProductForCart(5, 1))
                .thenReturn(validationResult);

        // Mock product details
        ProductResponseDto product = new ProductResponseDto();
        product.setProductName("Test Product");
        when(productBusinessService.getProductById(5)).thenReturn(product);

        // Mock price calculations
        when(productBusinessService.calculateProductTotal(5, 1))
                .thenReturn(BigDecimal.valueOf(100));
        when(productBusinessService.getFormattedProductPrice(5))
                .thenReturn("₹100");
        when(productBusinessService.getProductPrice(5))
                .thenReturn(BigDecimal.valueOf(100));

        // Mock cart response
        CartResponseDto cartResponse = new CartResponseDto();
        when(cartBusinessService.addProductToCart("user", 5, 1))
                .thenReturn(cartResponse);

        // Mock message builder
        when(messageBuilderService.createButton(anyString(), anyString()))
                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.Button.class));
        when(messageBuilderService.buildButtonMessage(anyString(), anyString(), anyString(), any(), anyList()))
                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.class));

        // Execute test
        cartFlowService.addToCart("123", "user", 5);

        // Verify
        verify(cartBusinessService).addProductToCart("user", 5, 1);
        verify(productBusinessService).validateProductForCart(5, 1);
    }

    @Test
    void testAddToCart_ValidationFails() {
        // Mock ProductValidationResult for failure case
        ProductBusinessService.ProductValidationResult validationResult =
                mock(ProductBusinessService.ProductValidationResult.class);
        when(validationResult.isValid()).thenReturn(false);
        when(validationResult.getMessage()).thenReturn("Product not available");

        // Mock product validation
        when(productBusinessService.validateProductForCart(5, 1))
                .thenReturn(validationResult);

        // Execute test
        cartFlowService.addToCart("123", "user", 5);

        // Verify that cart service is NOT called when validation fails
        verify(cartBusinessService, never()).addProductToCart(anyString(), anyInt(), anyInt());
        verify(messageSender).sendTextMessage("123", "user", "❌ Product not available");
    }

    @Test
    void testViewCart() {
        when(cartBusinessService.getCartProducts("user")).thenReturn(new ArrayList<>());

        // Mock message builder for empty cart
        when(messageBuilderService.createButton(anyString(), anyString()))
                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.Button.class));
        when(messageBuilderService.buildButtonMessage(anyString(), anyString(), anyString(), any(), anyList()))
                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.class));

        cartFlowService.viewCart("123", "user");

        verify(cartBusinessService, times(1)).getCartProducts("user");
    }

    @Test
    void testClearCart() {
        when(cartBusinessService.clearCart("user")).thenReturn("Cart cleared successfully");

        // Mock message builder
        when(messageBuilderService.createButton(anyString(), anyString()))
                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.Button.class));
        when(messageBuilderService.buildButtonMessage(anyString(), anyString(), anyString(), any(), anyList()))
                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.class));

        cartFlowService.clearCart("123", "user");

        verify(cartBusinessService).clearCart("user");
    }

    @Test
    void testStartCheckout() {
        when(cartBusinessService.getCartProducts("user")).thenReturn(new ArrayList<>());

        cartFlowService.startCheckout("123", "user");

        // The method calls getCartProducts twice - once in startCheckout and once in viewCart
        verify(cartBusinessService, times(2)).getCartProducts("user");
    }

    @Test
    void testStartCheckout_WithNonEmptyCart() {
        // Mock non-empty cart
        CartProductResponseDto cartProduct = mock(CartProductResponseDto.class);
        when(cartBusinessService.getCartProducts("user"))
                .thenReturn(new ArrayList<>() {{ add(cartProduct); }});

        // Mock validation
        when(cartBusinessService.validateCartItems("user"))
                .thenReturn(new ArrayList<>()); // No validation errors

        cartFlowService.startCheckout("123", "user");

        verify(cartBusinessService, times(1)).getCartProducts("user");
        verify(cartBusinessService).validateCartItems("user");
        verify(messageSender).sendTextMessage(anyString(), eq("user"), contains("Please provide your delivery address"));
    }

    @Test
    void testShowEditCartOptions() {
        when(cartBusinessService.getCartProducts("user")).thenReturn(new ArrayList<>());

        cartFlowService.showEditCartOptions("123", "user");

        // If empty cart, it calls viewCart which calls getCartProducts again
        verify(cartBusinessService, times(2)).getCartProducts("user");
    }

//    @Test
//    void testShowEditCartOptions_WithNonEmptyCart() {
//        // Mock non-empty cart
//        CartProductResponseDto cartProduct = mock(CartProductResponseDto.class);
//        when(cartProduct.getCartProductId()).thenReturn(1L);
//        when(cartBusinessService.getCartProducts("user"))
//                .thenReturn(new ArrayList<>() {{ add(cartProduct); }});
//
//        // Mock message builder
//        when(messageBuilderService.createButton(anyString(), anyString()))
//                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.Button.class));
//        when(messageBuilderService.buildButtonMessage(anyString(), anyString(), anyString(), any(), anyList()))
//                .thenReturn(mock(com.webstore.dto.request.whatsapp.WhatsAppRequestDto.class));
//
//        cartFlowService.showEditCartOptions("123", "user");
//
//        verify(cartBusinessService, times(1)).getCartProducts("user");
//    }
}