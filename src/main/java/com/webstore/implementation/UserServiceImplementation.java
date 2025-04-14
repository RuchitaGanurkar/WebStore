package com.webstore.implementation;

import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import com.webstore.entity.User;
import com.webstore.repository.UserRepository;
import com.webstore.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        return mapToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {

        // Check if username already exists
        if (userRepository.existsByUsername((userRequestDto.getUsername()))) {
            throw new IllegalArgumentException("Username already exists: " + userRequestDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userRequestDto.getEmail());
        }

        User user = new User();
        mapToUser(userRequestDto, user);
        User savedUser = userRepository.save(user);

        System.out.println("Created user with ID: " + savedUser.getUserId());
        return mapToUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Integer userId, UserRequestDto userRequestDto) {
        System.out.println("Updating user with ID: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Check if trying to change username to one that already exists
        if (!user.getUsername().equals(userRequestDto.getUsername()) &&
                userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userRequestDto.getUsername());
        }

        // Check if trying to change email to one that already exists
        if (!user.getEmail().equals(userRequestDto.getEmail()) &&
                userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userRequestDto.getEmail());
        }

        mapToUser(userRequestDto, user);
        User updatedUser = userRepository.save(user);

        System.out.println("Updated user with ID: " + updatedUser.getUserId());
        return mapToUserResponseDto(updatedUser);
    }


    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        System.out.println("Deleted user with ID: " + userId);
    }

    // Helper method to map User entity to UserResponseDto
    private UserResponseDto mapToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    // Helper method to map UserRequestDto to User entity
    private void mapToUser(UserRequestDto userRequestDto, User user) {
        user.setUsername(userRequestDto.getUsername());
        user.setEmail(userRequestDto.getEmail());
        user.setFullName(userRequestDto.getFullName());
        user.setRole(userRequestDto.getRole());
    }



}

