package com.webstore.dto.request;

import com.webstore.dto.validation.CreateGroup;
import com.webstore.dto.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequestDto {

    @NotBlank(message = "Product name is required", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 2, max = 50, message = "Product name must be between 2 and 50 characters", groups = {CreateGroup.class, UpdateGroup.class})
    private String productName;

    @Size(max = 100, message = "Product description can't be longer than 100 characters", groups = {CreateGroup.class, UpdateGroup.class})
    private String productDescription;

    @NotNull(message = "Category ID is required", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer categoryId;

    private String createdBy;  // filled automatically or manually
    private String updatedBy;
}
