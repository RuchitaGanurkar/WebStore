package com.webstore.service.whatsapp.list.impl;

import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoryListSelectionStrategyTest {

    @Mock
    private CategoryBusinessService categoryService;
    @Mock
    private ProductFlowService productFlowService;
    @Mock
    private WhatsAppMessageSender messageSender;
    @InjectMocks
    private CategoryListSelectionStrategy strategy;

    @BeforeEach
    void setUp() {
        categoryService = mock(CategoryBusinessService.class);
        productFlowService = mock(ProductFlowService.class);
        messageSender = mock(WhatsAppMessageSender.class);
        strategy = new CategoryListSelectionStrategy(categoryService, productFlowService, messageSender);
    }

    @Test
    void testSupports_returnsTrueForValidListId() {
        assertTrue(strategy.supports("cat_page_1_item_3"));
    }

    @Test
    void testSupports_returnsFalseForInvalidListId() {
        assertFalse(strategy.supports("invalid_id"));
    }

    @Test
    void testHandle_validSelection_invokesProductFlowService() {
        String phoneNumberId = "123";
        String from = "9876543210";
        String listId = "cat_page_1_item_2";

        List<String> categories = List.of("Fruits", "Vegetables", "Beverages");
        when(categoryService.getAllCategoryNames()).thenReturn(categories);

        strategy.handle(phoneNumberId, from, listId);

        verify(messageSender).sendTextMessage(eq(phoneNumberId), eq(from), contains("Vegetables"));
        verify(productFlowService).sendProductSelection(eq(API_VERSION), eq(phoneNumberId), eq(from), eq("Vegetables"));
    }

    @Test
    void testHandle_invalidSelection_sendsInvalidMessage() {
        String phoneNumberId = "123";
        String from = "9876543210";
        String listId = "cat_page_1_item_10";

        when(categoryService.getAllCategoryNames()).thenReturn(List.of("Fruits", "Vegetables"));

        strategy.handle(phoneNumberId, from, listId);

        verify(messageSender).sendTextMessage(phoneNumberId, from, "Invalid selection.");
    }

    @Test
    void testHandle_malformedListId_sendsErrorMessage() {
        String phoneNumberId = "123";
        String from = "9876543210";
        String malformedListId = "bad_format";  // or "cat_page_abc"

        strategy.handle(phoneNumberId, from, malformedListId);

        verify(messageSender).sendTextMessage(eq(phoneNumberId), eq(from), contains("Something went wrong"));
    }
}
