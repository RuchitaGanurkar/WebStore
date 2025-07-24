package com.webstore.implementation.product;

import com.webstore.dto.request.product.UserRequestDto;
import com.webstore.dto.response.product.UserResponseDto;
import com.webstore.entity.product.User;
import com.webstore.repository.product.UserRepository;
import com.webstore.service.product.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImplementation implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Integer userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
        return mapToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating user with username: {}", userRequestDto.getUsername());

        // Check if phone number already exists (FIXED)
        if (userRequestDto.getPhoneNumber() != null &&
                userRepository.findByPhoneNumber(String.valueOf(userRequestDto.getPhoneNumber())).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User phone number already exists: " + userRequestDto.getPhoneNumber());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists: " + userRequestDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists: " + userRequestDto.getEmail());
        }

        User user = new User();
        mapToUser(userRequestDto, user);
        User savedUser = userRepository.save(user);

        log.info("Created user with ID: {}", savedUser.getUserId());
        return mapToUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Integer userId, UserRequestDto userRequestDto) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        // Check if phone number is being changed and if new phone number already exists
        if (userRequestDto.getPhoneNumber() != null &&
                !user.getPhoneNumber().equals(String.valueOf(userRequestDto.getPhoneNumber())) &&
                userRepository.findByPhoneNumber(String.valueOf(userRequestDto.getPhoneNumber())).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists: " + userRequestDto.getPhoneNumber());
        }

        // Check username uniqueness only if it's being changed
        if (!user.getUsername().equals(userRequestDto.getUsername()) &&
                userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists: " + userRequestDto.getUsername());
        }

        // Check email uniqueness only if it's being changed
        if (!user.getEmail().equals(userRequestDto.getEmail()) &&
                userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists: " + userRequestDto.getEmail());
        }

        mapToUser(userRequestDto, user);
        User updatedUser = userRepository.save(user);

        log.info("Updated user with ID: {}", updatedUser.getUserId());
        return mapToUserResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        log.info("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("Deleted user with ID: {}", userId);
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        if (user.getPhoneNumber() != null) {
            dto.setPhoneNumber(Long.valueOf(user.getPhoneNumber()));
        }
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private void mapToUser(UserRequestDto dto, User user) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(String.valueOf(dto.getPhoneNumber()));
        }
        user.setRole(dto.getRole());
    }
}