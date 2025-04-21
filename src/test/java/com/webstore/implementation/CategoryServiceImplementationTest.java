package com.webstore.implementation;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.entity.Category;
import com.webstore.repository.CategoryRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

class CategoryServiceImplementationTest {

    @InjectMocks
    private CategoryServiceImplementation categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private Category category;
    private CategoryRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = new CategoryRequestDto();
        requestDto.setCategoryName("Books");
        requestDto.setCategoryDescription("All types of books");

        category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Books");
        category.setCategoryDescription("All types of books");
    }

    @Test
    void testCreateCategory_Success() {
        when(categoryRepository.existsByCategoryName("Books")).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(category);

        CategoryResponseDto responseDto = categoryService.createCategory(requestDto);

        assertEquals("Books", responseDto.getCategoryName());
    }

    @Test
    void testCreateCategory_AlreadyExists() {
        when(categoryRepository.existsByCategoryName("Books")).thenReturn(true);

        assertThrows(EntityExistsException.class, () -> categoryService.createCategory(requestDto));
    }

    @Test
    void testGetAllCategories() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);

        List<CategoryResponseDto> result = categoryService.getAllCategories(0, 5);

        assertEquals(1, result.size());
        assertEquals("Books", result.get(0).getCategoryName());
    }

    @Test
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        CategoryResponseDto dto = categoryService.getCategoryById(1);

        assertEquals("Books", dto.getCategoryName());
    }

    @Test
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(1));
    }

    @Test
    void testUpdateCategory_Success() {
        Category updatedCategory = new Category();
        updatedCategory.setCategoryId(1);
        updatedCategory.setCategoryName("Books");
        updatedCategory.setCategoryDescription("Updated");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryName("Books")).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(updatedCategory);

        CategoryResponseDto updated = categoryService.updateCategory(1, requestDto);

        assertEquals("Books", updated.getCategoryName());
        assertEquals("Updated", updated.getCategoryDescription());
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.existsById(1)).thenReturn(true);

        categoryService.deleteCategory(1);

        verify(categoryRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> categoryService.deleteCategory(1));
    }
}
