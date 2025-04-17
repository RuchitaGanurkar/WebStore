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

//    @NotNull(groups = CategoryValidation.class, message = "Created by is required")
//    @NotBlank(groups = CategoryValidation.class, message = "Created by should not be blank")
//    @Size(min = 3, max = 50, groups = CategoryValidation.class, message = "Created by must be between 3 and 50 characters")
//    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", groups = CategoryValidation.class, message = "Created by can only contain letters, numbers, dots, underscores and hyphens")
//    private String createdBy;


    public void setCategoryId(Integer categoryId) {
    }
}