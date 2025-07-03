package com.webstore.service.whatsapp.list.impl;

import com.webstore.service.whatsapp.flow.NavigationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ListPageNavigationStrategyTest {

    private NavigationService navigationService;
    private ListPageNavigationStrategy strategy;

    @BeforeEach
    void setUp() {
        navigationService = mock(NavigationService.class);
        strategy = new ListPageNavigationStrategy(navigationService);
    }

    @Test
    void testSupports_returnsTrueForNextOrPrev() {
        assertTrue(strategy.supports("next_cat_page_1"));
        assertTrue(strategy.supports("prev_prod_p_2_cFJydWl0cw=="));
    }

    @Test
    void testSupports_returnsFalseForOthers() {
        assertFalse(strategy.supports("cat_page_1"));
        assertFalse(strategy.supports("invalid_id"));
    }

    @Test
    void testHandle_callsCategoryNavigation() {
        strategy.handle("123", "9876543210", "next_cat_page_2");
        verify(navigationService).handleCategoryPageNavigation("123", "9876543210", "next_cat_page_2");
    }

    @Test
    void testHandle_callsProductNavigation() {
        strategy.handle("123", "9876543210", "prev_prod_p_1_cFZlZ2V0YWJsZXM=");
        verify(navigationService).handleProductPageNavigation("123", "9876543210", "prev_prod_p_1_cFZlZ2V0YWJsZXM=");
    }
}
