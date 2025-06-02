package com.webstore.controller;

import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import com.webstore.exception.UserNotFoundException;
import com.webstore.exception.DuplicateUserException;
import com.webstore.service.UserService;
import com.webstore.validation.UserValidation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("userServiceImplementation")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("Fetching all users");
        List<UserResponseDto> users = userService.getAllUsers();

        log.info("Found {} users", users.size());
        return ResponseEntity.ok(users); // Always 200, even if list is empty
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer userId) {
        log.info("Fetching user with ID: {}", userId);
        try {
            UserResponseDto user = userService.getUserById(userId);
            return ResponseEntity.ok(user); // HTTP 200
        } catch (UserNotFoundException ex) {
            log.error("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // HTTP 404
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("Creating new user");
        try {
            UserResponseDto createdUser = userService.createUser(userRequestDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // HTTP 201
        } catch (DuplicateUserException ex) {
            log.error("Duplicate user detected: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()); // HTTP 400
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer userId,
            @Validated(UserValidation.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("Updating user with ID: {}", userId);
        try {
            UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto);
            return ResponseEntity.ok(updatedUser); // HTTP 200
        } catch (UserNotFoundException ex) {
            log.error("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // HTTP 404
        } catch (DuplicateUserException ex) {
            log.error("Duplicate user during update: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()); // HTTP 400
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        log.info("Deleting user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build(); // HTTP 204
        } catch (UserNotFoundException ex) {
            log.error("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // HTTP 404
        }
    }
}
