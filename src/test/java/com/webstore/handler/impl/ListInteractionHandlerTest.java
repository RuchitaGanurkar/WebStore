package com.webstore.handler.impl;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.handler.impl.ListInteractionHandler;
import com.webstore.service.whatsapp.list.ListActionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListInteractionHandlerTest {

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private ListActionStrategy strategy1;

    @Mock
    private ListActionStrategy strategy2;

    private ListInteractionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListInteractionHandler(List.of(strategy1, strategy2), messageSender);
    }

    @Test
    void testHandle_WithSupportedListId_ShouldDelegateToStrategy() {
        // Arrange
        when(strategy1.supports("list-123")).thenReturn(false);
        when(strategy2.supports("list-123")).thenReturn(true);

        // Act
        handler.handle("123", "user", "list-123");

        // Assert
        verify(strategy2).handle("123", "user", "list-123");
        verify(messageSender, never()).sendTextMessage(any(), any(), any());
    }

    @Test
    void testHandle_WithUnsupportedListId_ShouldSendFallbackMessage() {
        // Arrange
        when(strategy1.supports("unknown-list")).thenReturn(false);
        when(strategy2.supports("unknown-list")).thenReturn(false);

        // Act
        handler.handle("123", "user", "unknown-list");

        // Assert
        verify(messageSender).sendTextMessage(eq("123"), eq("user"),
                contains("I didn't understand"));
        verify(strategy1, never()).handle(any(), any(), any());
        verify(strategy2, never()).handle(any(), any(), any());
    }
}
