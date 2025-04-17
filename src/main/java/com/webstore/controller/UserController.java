package com.webstore.controller;

import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import com.webstore.service.UserService;
import com.webstore.validation.UserValidation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("GET ALL USERS REQUEST - Endpoint: GET /api/users");

        List<UserResponseDto> users = userService.getAllUsers();

        log.info("Response: Found {} users", users.size());
        if (log.isDebugEnabled()) {
            logUserList(users);
        }

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer userId) {
        log.info("GET USER BY ID REQUEST - Endpoint: GET /api/users/{}", userId);
        log.debug("Request Parameter: userId = {}", userId);

        try {
            UserResponseDto user = userService.getUserById(userId);

            log.info("Response: User found with ID {}", userId);
            if (log.isDebugEnabled()) {
                logUser(user);
            }

            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            log.error("Error retrieving user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("CREATE USER REQUEST - Endpoint: POST /api/users");
        if (log.isDebugEnabled()) {
            log.debug("Request Body: {}", userRequestDto);
        }

        try {
            UserResponseDto createdUser = userService.createUser(userRequestDto);

            log.info("User created successfully with ID: {}", createdUser.getUserId());
            if (log.isDebugEnabled()) {
                logUser(createdUser);
            }

            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer userId,
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("UPDATE USER REQUEST - Endpoint: PUT /api/users/{}", userId);
        log.debug("Request Parameter: userId = {}", userId);
        if (log.isDebugEnabled()) {
            log.debug("Request Body: {}", userRequestDto);
        }

        try {
            UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto);

            log.info("User with ID {} updated successfully", userId);
            if (log.isDebugEnabled()) {
                logUser(updatedUser);
            }

            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            log.error("Error updating user with ID {}: User not found", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        log.info("DELETE USER REQUEST - Endpoint: DELETE /api/users/{}", userId);
        log.debug("Request Parameter: userId = {}", userId);

        try {
            userService.deleteUser(userId);

            log.info("User with ID {} successfully deleted", userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Error deleting user with ID {}: User not found", userId);
            return ResponseEntity.notFound().build();
        }
    }

    // Helper methods for detailed logging
    private void logUser(UserResponseDto user) {
        log.debug("User details: id={}, username={}, email={}, fullName={}, role={}, createdAt={}, updatedAt={}",
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private void logUserList(List<UserResponseDto> users) {
        log.debug("User list details:");
        for (int i = 0; i < users.size(); i++) {
            UserResponseDto user = users.get(i);
            log.debug("  User {}: id={}, username={}, email={}, fullName={}, role={}, createdAt={}, updatedAt={}",
                    i + 1,
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    user.getCreatedAt(),
                    user.getUpdatedAt());
        }
    }
}