package com.webstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsersResponseDto {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
