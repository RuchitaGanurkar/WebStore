package com.webstore.service.whatsapp.flow;

import com.webstore.service.whatsapp.business.CartBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartFlowServiceTest {

    @Mock
    private CartBusinessService cartService;

    @Mock
    private WhatsAppMessageSender messageSender;

    private CartFlowService cartFlowService;

    @BeforeEach
    void setUp() {
        cartFlowService = new CartFlowService(cartService, messageSender);
    }

    @Test
    void testAddToCart() {
        cartFlowService.addToCart("123", "user", 5);

        verify(cartService).addProductToCart("user", 5, 1);
    }

    @Test
    void testViewCart() {
        when(cartService.getCartSummary("user")).thenReturn("Cart: 2 Apples");

        cartFlowService.viewCart("123", "user");

        verify(messageSender).sendTextMessage("123", "user", "Cart: 2 Apples");
    }
}
