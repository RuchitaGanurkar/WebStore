package com.webstore.controller;

import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import com.webstore.service.UserService;
import com.webstore.validation.UserValidation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
@RestController
@RequestMapping("/api/users")
public class UserController {

    // ✅ Setter Injection
    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        System.out.println("\n==== GET ALL USERS REQUEST ====");
        System.out.println("Endpoint: GET /api/users");

        List<UserResponseDto> users = userService.getAllUsers();

        System.out.println("Response: Found " + users.size() + " users");
        printUserList(users);
        System.out.println("==== END GET ALL USERS ====\n");

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer userId) {
        System.out.println("\n==== GET USER BY ID REQUEST ====");
        System.out.println("Endpoint: GET /api/users/" + userId);
        System.out.println("Request Parameter: userId = " + userId);

        try {
            UserResponseDto user = userService.getUserById(userId);

            System.out.println("Response: User found");
            printUser(user);
            System.out.println("==== END GET USER BY ID ====\n");

            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Response: 404 Not Found");
            System.out.println("==== END GET USER BY ID ====\n");

            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        System.out.println("\n==== CREATE USER REQUEST ====");
        System.out.println("Endpoint: POST /api/users");
        System.out.println("Request Body:");
        printUserRequest(userRequestDto);

        try {
            UserResponseDto createdUser = userService.createUser(userRequestDto);

            System.out.println("Response: 201 Created");
            printUser(createdUser);
            System.out.println("==== END CREATE USER ====\n");

            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Response: 400 Bad Request");
            System.out.println("==== END CREATE USER ====\n");

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer userId,
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        System.out.println("\n==== UPDATE USER REQUEST ====");
        System.out.println("Endpoint: PUT /api/users/" + userId);
        System.out.println("Request Parameter: userId = " + userId);
        System.out.println("Request Body:");
        printUserRequest(userRequestDto);

        try {
            UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto);

            System.out.println("Response: 200 OK");
            printUser(updatedUser);
            System.out.println("==== END UPDATE USER ====\n");

            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Response: 404 Not Found");
            System.out.println("==== END UPDATE USER ====\n");

            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Response: 400 Bad Request");
            System.out.println("==== END UPDATE USER ====\n");

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        System.out.println("\n==== DELETE USER REQUEST ====");
        System.out.println("Endpoint: DELETE /api/users/" + userId);
        System.out.println("Request Parameter: userId = " + userId);

        try {
            userService.deleteUser(userId);

            System.out.println("Response: 204 No Content");
            System.out.println("User with ID " + userId + " successfully deleted");
            System.out.println("==== END DELETE USER ====\n");

            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Response: 404 Not Found");
            System.out.println("==== END DELETE USER ====\n");

            return ResponseEntity.notFound().build();
        }
    }

    // Helper methods for printing
    private void printUserRequest(UserRequestDto userRequestDto) {
        System.out.println("  {");
        System.out.println("    \"username\": \"" + userRequestDto.getUsername() + "\",");
        System.out.println("    \"email\": \"" + userRequestDto.getEmail() + "\",");
        System.out.println("    \"fullName\": \"" + userRequestDto.getFullName() + "\",");
        System.out.println("    \"role\": \"" + userRequestDto.getRole() + "\"");
        System.out.println("  }");
    }

    private void printUser(UserResponseDto user) {
        System.out.println("  {");
        System.out.println("    \"userId\": " + user.getUserId() + ",");
        System.out.println("    \"username\": \"" + user.getUsername() + "\",");
        System.out.println("    \"email\": \"" + user.getEmail() + "\",");
        System.out.println("    \"fullName\": \"" + user.getFullName() + "\",");
        System.out.println("    \"role\": \"" + user.getRole() + "\",");
        System.out.println("    \"createdAt\": \"" + user.getCreatedAt() + "\",");
        System.out.println("    \"updatedAt\": \"" + user.getUpdatedAt() + "\"");
        System.out.println("  }");
    }

    private void printUserList(List<UserResponseDto> users) {
        System.out.println("  [");
        for (int i = 0; i < users.size(); i++) {
            UserResponseDto user = users.get(i);
            System.out.println("    {");
            System.out.println("      \"userId\": " + user.getUserId() + ",");
            System.out.println("      \"username\": \"" + user.getUsername() + "\",");
            System.out.println("      \"email\": \"" + user.getEmail() + "\",");
            System.out.println("      \"fullName\": \"" + user.getFullName() + "\",");
            System.out.println("      \"role\": \"" + user.getRole() + "\",");
            System.out.println("      \"createdAt\": \"" + user.getCreatedAt() + "\",");
            System.out.println("      \"updatedAt\": \"" + user.getUpdatedAt() + "\"");
            System.out.print("    }");
            if (i < users.size() - 1) {
                System.out.println(",");
            } else {
                System.out.println();
            }
        }
        System.out.println("  ]");
    }
}
