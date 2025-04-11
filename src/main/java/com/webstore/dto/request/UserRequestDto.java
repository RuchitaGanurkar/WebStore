package com.webstore.dto.request;


import com.webstore.validation.UserValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequestDto {


    //  Adding Validation Annotation To Requested Data

    @NotNull(groups = UserValidation.class, message = "Username is required")
    @NotBlank(groups = UserValidation.class, message = "Username should not be blank")
    @Size(min = 3, max = 50, groups = UserValidation.class, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", groups = UserValidation.class, message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @NotNull(groups = UserValidation.class, message = "Email is required")
    @NotBlank(groups = UserValidation.class, message = "Email should not be blank")
    @Email(groups = UserValidation.class, message = "Email should be valid")
    @Size(max = 100, groups = UserValidation.class, message = "Email must not exceed 100 characters")
    private String email;

    @NotNull(groups = UserValidation.class, message = "Full name is required")
    @NotBlank(groups = UserValidation.class, message = "Full name should not be blank")
    @Size(min = 2, max = 100, groups = UserValidation.class, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotNull(groups = UserValidation.class, message = "Role is required")
    @NotBlank(groups = UserValidation.class, message = "Role should not be blank")
    private String role;
}