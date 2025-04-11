package com.webstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDto {

    @NotBlank(message = "Category name must not be blank")
    @Size(min = 1, max = 30, message = "Category name must be between 1 and 30 characters")
    private String categoryName;

    private String createdBy;
    private String updatedBy;
}
