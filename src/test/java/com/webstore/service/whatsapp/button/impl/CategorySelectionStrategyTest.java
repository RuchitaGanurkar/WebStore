package com.webstore.service.whatsapp.button.impl;

import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CategorySelectionStrategyTest {

    private CategoryFlowService categoryFlowService;
    private CategoryBusinessService categoryService;
    private ProductFlowService productFlowService;
    private WhatsAppMessageSender messageSender;
    private CategorySelectionStrategy strategy;

    @BeforeEach
    void setUp() {
        categoryFlowService = mock(CategoryFlowService.class);
        categoryService = mock(CategoryBusinessService.class);
        productFlowService = mock(ProductFlowService.class);
        messageSender = mock(WhatsAppMessageSender.class);

        strategy = new CategorySelectionStrategy(categoryFlowService, categoryService, productFlowService, messageSender);
    }

    @Test
    void testSupports() {
        assertTrue(strategy.supports("cat_1"));
        assertFalse(strategy.supports("view_product_2"));
    }

    @Test
    void testHandle_SeeAll() {
        strategy.handle("123", "user", "cat_see_all");

        verify(categoryFlowService).sendCategoryList(any(), eq("123"), eq("user"), eq(1));
    }

    @Test
    void testHandle_ValidCategorySelection() {
        when(categoryService.getTop3CategoryNames()).thenReturn(List.of("Fruits", "Vegetables", "Grains"));

        strategy.handle("123", "user", "cat_2");

        verify(productFlowService).sendProductSelection(any(), eq("123"), eq("user"), eq("Vegetables"));
    }

    @Test
    void testHandle_InvalidFormat() {
        strategy.handle("123", "user", "cat_invalid");

        verify(messageSender).sendTextMessage(eq("123"), eq("user"), contains("problem"));
    }
}

