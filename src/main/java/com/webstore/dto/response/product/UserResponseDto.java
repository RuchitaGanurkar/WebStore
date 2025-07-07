package com.webstore.dto.response.product;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponseDto {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}