package com.webstore.service.whatsapp.flow;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class WelcomeFlowServiceTest {

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private MessageBuilderService messageBuilder;

    @InjectMocks
    private WelcomeFlowService welcomeFlowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendWelcomeMessage_sendsCorrectButtonMessage() {
        // Arrange
        String version = "v1";
        String phoneNumberId = "123";
        String recipientPhoneNumber = "9876543210";

        WhatsAppRequestDto.Button button1 = new WhatsAppRequestDto.Button();
        WhatsAppRequestDto.Button button2 = new WhatsAppRequestDto.Button();
        WhatsAppRequestDto request = new WhatsAppRequestDto();

        when(messageBuilder.createButton("welcome_hi", "Hi 👋")).thenReturn(button1);
        when(messageBuilder.createButton("welcome_info", "Tell me more")).thenReturn(button2);
        when(messageBuilder.buildButtonMessage(
                eq(recipientPhoneNumber),
                eq("👋 Welcome to WebStore"),
                eq("Thanks for joining us! What would you like to do next?"),
                eq("Choose an option below"),
                anyList()
        )).thenReturn(request);

        // Act
        welcomeFlowService.sendWelcomeMessage(version, phoneNumberId, recipientPhoneNumber);

        // Assert
        verify(messageSender).sendMessage(phoneNumberId, request, "Welcome message");
    }
}
