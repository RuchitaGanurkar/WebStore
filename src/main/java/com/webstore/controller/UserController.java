package com.webstore.controller;

import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import com.webstore.service.UserService;
import com.webstore.validation.UserValidation;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Setter
@RestController
@RequestMapping("/api/users")
public class UserController {


    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("Processing request to get all users");
        List<UserResponseDto> users = userService.getAllUsers();
        log.info("Retrieved {} users successfully", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer userId) {
        log.info("Processing request to get user with id: {}", userId);
        try {
            UserResponseDto user = userService.getUserById(userId);
            log.info("User with id: {} retrieved successfully", userId);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            log.error("User with id: {} not found", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("Processing request to create a new user");
        try {
            UserResponseDto createdUser = userService.createUser(userRequestDto);
            log.info("User created successfully with id: {}", createdUser.getUserId());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer userId,
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("Processing request to update user with id: {}", userId);
        try {
            UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto);
            log.info("User with id: {} updated successfully", userId);
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            log.error("User with id: {} not found for update", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        log.info("Processing request to delete user with id: {}", userId);
        try {
            userService.deleteUser(userId);
            log.info("User with id: {} deleted successfully", userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("User with id: {} not found for deletion", userId);
            return ResponseEntity.notFound().build();
        }
    }
}