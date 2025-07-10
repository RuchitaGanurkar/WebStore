package com.webstore.dto.request.cart;

import com.webstore.validation.cart.CartProductStatusValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartProductStatusRequestDto {

    @NotBlank(groups = CartProductStatusValidation.class, message = "Status name is required")
    @Size(min = 2, max = 20, groups = CartProductStatusValidation.class, message = "Status name must be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_-]+$", groups = CartProductStatusValidation.class, message = "Status name can only contain letters, numbers, spaces, underscores and hyphens")
    private String statusName;

}
