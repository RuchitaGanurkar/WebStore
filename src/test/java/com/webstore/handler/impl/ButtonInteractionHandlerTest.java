package com.webstore.handler.impl;

import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.handler.impl.ButtonInteractionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.*;

class ButtonInteractionHandlerTest {

    private WhatsAppMessageSender messageSender;
    private ButtonActionStrategy supportedStrategy;
    private ButtonActionStrategy unsupportedStrategy;
    private ButtonInteractionHandler handler;

    @BeforeEach
    void setUp() {
        messageSender = mock(WhatsAppMessageSender.class);
        supportedStrategy = mock(ButtonActionStrategy.class);
        unsupportedStrategy = mock(ButtonActionStrategy.class);

        when(supportedStrategy.supports("btn-123")).thenReturn(true);
        when(unsupportedStrategy.supports(anyString())).thenReturn(false);

        handler = new ButtonInteractionHandler(List.of(unsupportedStrategy, supportedStrategy), messageSender);
    }

    @Test
    void testHandle_WithSupportedButtonId_ShouldDelegateToStrategy() {
        handler.handle("12345", "user-1", "btn-123");

        verify(supportedStrategy, times(1)).handle("12345", "user-1", "btn-123");
        verify(messageSender, never()).sendTextMessage(anyString(), anyString(), anyString());
    }

    @Test
    void testHandle_WithUnsupportedButtonId_ShouldSendFallbackMessage() {
        handler.handle("12345", "user-1", "unknown-btn");

        verify(supportedStrategy, never()).handle(any(), any(), any());
        verify(messageSender, times(1))
                .sendTextMessage(eq("12345"), eq("user-1"), contains("I didn't understand"));
    }
}
