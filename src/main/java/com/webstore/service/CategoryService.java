package com.webstore.service;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    CategoryResponseDto createCategory(CategoryRequestDto dto);
    List<CategoryResponseDto> getAllCategories(int page, int size);
    CategoryResponseDto getCategoryById(Integer id);

    CategoryResponseDto updateCategory(Integer id, CategoryRequestDto dto);

    void deleteCategory(Integer id);
}
