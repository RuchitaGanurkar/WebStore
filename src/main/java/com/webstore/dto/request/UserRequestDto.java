package com.webstore.dto.request;

import lombok.Data;

@Data
public class UserRequestDto {
    private String username;
    private String email;
    private String fullName;
    private String role;
}
