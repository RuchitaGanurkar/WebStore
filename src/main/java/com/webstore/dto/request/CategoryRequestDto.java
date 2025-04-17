package com.webstore.dto.request;


import com.webstore.validation.CategoryValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequestDto {

    //  Adding Validation Annotation To Requested Data

    @NotNull(groups = CategoryValidation.class, message = "Category name is required")
    @NotBlank(groups = CategoryValidation.class, message = "Category name should not be blank")
    @Size(min = 2, max = 100, groups = CategoryValidation.class, message = "Category name must be between 2 and 100 characters")
    private String categoryName;

    @NotNull(groups = CategoryValidation.class, message = "Category description is required")
    @NotBlank(groups = CategoryValidation.class, message = "Category description should not be blank")
    @Size(max = 500, groups = CategoryValidation.class, message = "Category description must not exceed 500 characters")
    private String categoryDescription;

    public void setCategoryId(Integer categoryId) {
    }
}