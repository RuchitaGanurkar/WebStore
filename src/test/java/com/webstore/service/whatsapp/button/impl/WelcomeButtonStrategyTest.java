package com.webstore.service.whatsapp.button.impl;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)

public class WelcomeButtonStrategyTest {

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private CategoryFlowService categoryFlowService;

    @InjectMocks
    private WelcomeButtonStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new WelcomeButtonStrategy(messageSender, categoryFlowService);
    }

    @Test
    void testSupports() {
        assertTrue(strategy.supports("welcome_hi"));
        assertTrue(strategy.supports("welcome_info"));
        assertFalse(strategy.supports("cat_1"));
    }

    @Test
    void testHandleWelcomeHi() {
        strategy.handle("123", "user", "welcome_hi");

        verify(messageSender).sendTextMessage(eq("123"), eq("user"), contains("Welcome"));
        verify(categoryFlowService).sendCategorySelection(any(String.class), eq("123"), eq("user"));
    }

    @Test
    void testHandleWelcomeInfo() {
        strategy.handle("123", "user", "welcome_info");

        verify(messageSender).sendTextMessage(eq("123"), eq("user"), contains("WebStore"));
        verify(categoryFlowService).sendCategorySelection(any(), eq("123"), eq("user"));
    }
}
