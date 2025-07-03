package com.webstore.service.whatsapp.list.impl;

import com.webstore.service.whatsapp.flow.CategoryFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

class BackToCategoriesStrategyTest {

    private CategoryFlowService categoryFlowService;
    private BackToCategoriesStrategy strategy;

    @BeforeEach
    void setUp() {
        categoryFlowService = mock(CategoryFlowService.class);
        strategy = new BackToCategoriesStrategy(categoryFlowService);
    }

    @Test
    void testSupports_returnsTrueForMatchingListId() {
        assertTrue(strategy.supports("back_to_categories"));
    }

    @Test
    void testSupports_returnsFalseForNonMatchingListId() {
        assertFalse(strategy.supports("something_else"));
    }


    @Test
    void testHandle_invokesCategoryFlowService() {
        String phoneNumberId = "123";
        String from = "9876543210";

        strategy.handle(phoneNumberId, from, "back_to_categories");

        verify(categoryFlowService).sendCategorySelection(API_VERSION, phoneNumberId, from);
    }

}