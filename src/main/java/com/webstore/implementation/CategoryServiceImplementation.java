package com.webstore.implementation;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.entity.Category;
import com.webstore.repository.CategoryRepository;
import com.webstore.service.CategoryService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CategoryService interface.
 *
 * Uses setter injection for dependencies following best practices.
 * Exception handling is standardized to use specific exception types
 * that will be caught by the GlobalExceptionHandler.
 */

@Setter
@Service
public class CategoryServiceImplementation implements CategoryService {

    private CategoryRepository categoryRepository;


    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new EntityExistsException("Category name already exists: " + dto.getCategoryName());
        }

        Category category = new Category();
        category.setCategoryName(dto.getCategoryName());
        category.setCategoryDescription(dto.getCategoryDescription());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponseDto> getAllCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        return categoryPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
        return mapToResponse(category);
    }

    @Override
    public CategoryResponseDto updateCategory(Integer id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));

        if (!category.getCategoryName().equals(dto.getCategoryName()) &&
                categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new EntityExistsException("Category with name " + dto.getCategoryName() + " already exists");
        }

        category.setCategoryName(dto.getCategoryName());
        category.setCategoryDescription(dto.getCategoryDescription());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDto mapToResponse(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCategoryDescription(category.getCategoryDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setCreatedBy(category.getCreatedBy());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setUpdatedBy(category.getUpdatedBy());

        return dto;
    }
}