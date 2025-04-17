package com.webstore.controller;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size)
    {
        log.info("Retrieving categories - page: {}, size: {}", page, size);

        List<CategoryResponseDto> categories = categoryService.getAllCategories(page, size);
        log.info("Retrieved {} categories", categories.size());

        if (log.isDebugEnabled() && !categories.isEmpty()) {
            log.debug("Categories retrieved: {}",
                    categories.stream()
                            .map(CategoryResponseDto::getCategoryName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Integer id) {
        log.info("Retrieving category with ID: {}", id);

        CategoryResponseDto category = categoryService.getCategoryById(id);
        log.info("Retrieved category: {}", category.getCategoryName());

        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Valid CategoryRequestDto dto) {
        log.info("Creating new category: {}", dto.getCategoryName());

        CategoryResponseDto createdCategory = categoryService.createCategory(dto);
        log.info("Category created successfully with ID: {}", createdCategory.getCategoryId());

        if (log.isDebugEnabled()) {
            log.debug("Created category details: name={}, description={}",
                    createdCategory.getCategoryName(),
                    createdCategory.getCategoryDescription());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Integer id,
                                                              @RequestBody @Valid CategoryRequestDto dto) {
        log.info("Updating category with ID: {}", id);
        log.debug("Update details: name={}", dto.getCategoryName());

        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, dto);
        log.info("Category with ID: {} updated successfully", id);

        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        log.info("Deleting category with ID: {}", id);

        categoryService.deleteCategory(id);
        log.info("Category with ID: {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}