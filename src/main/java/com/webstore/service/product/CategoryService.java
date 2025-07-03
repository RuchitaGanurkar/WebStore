package com.webstore.service.product;

import com.webstore.dto.request.product.CategoryRequestDto;
import com.webstore.dto.response.product.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    CategoryResponseDto createCategory(CategoryRequestDto dto);
    List<CategoryResponseDto> getAllCategories(int page, int size);
    CategoryResponseDto getCategoryById(Integer id);

    CategoryResponseDto updateCategory(Integer id, CategoryRequestDto dto);

    void deleteCategory(Integer id);
}
