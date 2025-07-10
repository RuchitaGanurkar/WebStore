package com.webstore.dto.request.cart;

import com.webstore.validation.cart.CartStatusValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartStatusRequestDto {

    @NotBlank(groups = CartStatusValidation.class, message = "Status name is required")
    @Size(min = 2, max = 20, groups = CartStatusValidation.class, message = "Status name must be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_-]+$", groups = CartStatusValidation.class, message = "Status name can only contain letters, numbers, spaces, underscores and hyphens")
    private String statusName;
}
