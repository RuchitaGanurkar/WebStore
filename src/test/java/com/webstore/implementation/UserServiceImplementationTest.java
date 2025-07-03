package com.webstore.implementation;

import com.webstore.dto.request.product.UserRequestDto;
import com.webstore.dto.response.product.UserResponseDto;
import com.webstore.entity.product.User;
import com.webstore.implementation.product.UserServiceImplementation;
import com.webstore.repository.product.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImplementation userService;

    private User user;
    private UserRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // Set up test user
        user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Set up request DTO
        requestDto = new UserRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setEmail("test@example.com");
        requestDto.setFullName("Test User");
        requestDto.setRole("USER");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getUserId(), result.get(0).getUserId());
        assertEquals(user.getUsername(), result.get(0).getUsername());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        assertEquals(user.getFullName(), result.get(0).getFullName());
        assertEquals(user.getRole(), result.get(0).getRole());

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        UserResponseDto result = userService.getUserById(1);

        // Assert
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFullName(), result.getFullName());
        assertEquals(user.getRole(), result.getRole());

        verify(userRepository).findById(1);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getUserById(99));
        assertEquals("404 NOT_FOUND \"User not found with ID: 99\"", exception.getMessage());
        verify(userRepository).findById(99);
    }

    @Test
    void createUser_WhenUsernameAndEmailDoNotExist_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDto result = userService.createUser(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFullName(), result.getFullName());
        assertEquals(user.getRole(), result.getRole());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(requestDto));
        assertEquals("400 BAD_REQUEST \"Username already exists: testuser\"", exception.getMessage());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(requestDto));
        assertEquals("400 BAD_REQUEST \"Email already exists: test@example.com\"", exception.getMessage());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserExistsAndNoConflicts_ShouldUpdateUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Update with new data
        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setUsername("testuser"); // Same username
        updateDto.setEmail("test@example.com"); // Same email
        updateDto.setFullName("Updated User");
        updateDto.setRole("USER");

        // Act
        UserResponseDto result = userService.updateUser(1, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());

        verify(userRepository).findById(1);
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserExistsWithNewUsername_ShouldCheckUsernameUniqueness() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Update with new username
        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setUsername("newusername");
        updateDto.setEmail("test@example.com");
        updateDto.setFullName("Test User");
        updateDto.setRole("USER");

        // Act
        userService.updateUser(1, updateDto);

        // Assert
        verify(userRepository).findById(1);
        verify(userRepository).existsByUsername("newusername");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(99, requestDto));
        assertEquals("404 NOT_FOUND \"User not found with ID: 99\"", exception.getMessage());

        verify(userRepository).findById(99);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenNewUsernameExists_ShouldThrowException() {
        // Arrange
        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setUsername("oldusername");
        existingUser.setEmail("test@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(true);

        // Update with conflicting username
        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setUsername("newusername");
        updateDto.setEmail("test@example.com");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1, updateDto));
        assertEquals("400 BAD_REQUEST \"Username already exists: newusername\"", exception.getMessage());

        verify(userRepository).findById(1);
        verify(userRepository).existsByUsername("newusername");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenNewEmailExists_ShouldThrowException() {
        // Arrange
        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setUsername("testuser");
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Update with conflicting email
        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setUsername("testuser");
        updateDto.setEmail("new@example.com");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1, updateDto));
        assertEquals("400 BAD_REQUEST \"Email already exists: new@example.com\"", exception.getMessage());

        verify(userRepository).findById(1);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        // Act
        userService.deleteUser(1);

        // Assert
        verify(userRepository).existsById(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(99));
        assertEquals("404 NOT_FOUND \"User not found with ID: 99\"", exception.getMessage());

        verify(userRepository).existsById(99);
        verify(userRepository, never()).deleteById(99);
    }
}