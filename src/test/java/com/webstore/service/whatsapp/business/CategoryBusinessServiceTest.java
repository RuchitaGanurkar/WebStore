package com.webstore.service.whatsapp.business;

import com.webstore.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryBusinessServiceTest {

    private CategoryRepository categoryRepository;
    private CategoryBusinessService categoryBusinessService;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        categoryBusinessService = new CategoryBusinessService(categoryRepository);
    }

    @Test
    void testGetAllCategoryNames_WithSuccess() {
        List<String> mockList = List.of("Fruits", "Vegetables");
        when(categoryRepository.findAllCategoryNames()).thenReturn(mockList);

        List<String> result = categoryBusinessService.getAllCategoryNames();

        assertEquals(2, result.size());
    }

    @Test
    void testGetAllCategoryNames_WithException_ShouldReturnTop3() {
        when(categoryRepository.findAllCategoryNames()).thenThrow(RuntimeException.class);
        when(categoryRepository.findTop3CategoryNames()).thenReturn(List.of("Fruits", "Grains", "Dairy"));

        List<String> result = categoryBusinessService.getAllCategoryNames();

        assertEquals(3, result.size());
    }

    @Test
    void testGetTotalCategoryCount() {
        when(categoryRepository.count()).thenReturn(5L);

        assertEquals(5, categoryBusinessService.getTotalCategoryCount());
    }

    @Test
    void testGetCategoryIdByName() {
        when(categoryRepository.findCategoryIdByCategoryName("Fruits")).thenReturn(101);

        assertEquals(101, categoryBusinessService.getCategoryIdByName("Fruits"));
    }

    @Test
    void testShouldUseButtonsForCategories_True() {
        when(categoryRepository.count()).thenReturn(3L);

        assertTrue(categoryBusinessService.shouldUseButtonsForCategories());
    }

    @Test
    void testShouldUseButtonsForCategories_False() {
        when(categoryRepository.count()).thenReturn(5L);

        assertFalse(categoryBusinessService.shouldUseButtonsForCategories());
    }
}
