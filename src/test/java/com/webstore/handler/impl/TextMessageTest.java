package com.webstore.handler.impl;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CartFlowService;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.handler.impl.TextMessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.webstore.constant.WhatsAppConstants.API_VERSION;
import static org.mockito.Mockito.*;

class TextMessageTest {

    private WhatsAppMessageSender messageSender;
    private CategoryFlowService categoryFlowService;
    private CartFlowService cartFlowService;
    private TextMessageHandler handler;

    @BeforeEach
    void setUp() {
        messageSender = mock(WhatsAppMessageSender.class);
        categoryFlowService = mock(CategoryFlowService.class);
        cartFlowService = mock(CartFlowService.class);

        handler = new TextMessageHandler(messageSender, categoryFlowService, cartFlowService);
    }

    @Test
    void testHandle_WelcomeMessages() {
        handler.handle("12345", "user-1", "Hi");
        handler.handle("12345", "user-1", "hello");

        verify(messageSender, times(2)).sendTextMessage(eq("12345"), eq("user-1"), contains("Welcome to WebStore"));
    }

    @Test
    void testHandle_CategoryCommand() {
        handler.handle("12345", "user-1", "menu");

        verify(categoryFlowService, times(1))
                .sendCategorySelection(eq(API_VERSION), eq("12345"), eq("user-1"));
    }

    @Test
    void testHandle_CartCommand() {
        handler.handle("12345", "user-1", "cart");

        verify(cartFlowService, times(1))
                .viewCart(eq("12345"), eq("user-1"));
    }

    @Test
    void testHandle_UnknownMessage_ShouldEcho() {
        handler.handle("12345", "user-1", "some random text");

        verify(messageSender, times(1))
                .sendTextMessage(eq("12345"), eq("user-1"), contains("Echo: some random text"));
    }
}
