package com.webstore.implementation.product;

import com.webstore.dto.request.product.UserRequestDto;
import com.webstore.dto.response.product.UserResponseDto;
import com.webstore.entity.product.User;
import com.webstore.repository.product.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplementationTest {

    @InjectMocks
    private UserServiceImplementation userService;

    @Mock
    private UserRepository userRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    private User mockUser(Integer id) {
        User user = new User();
        user.setUserId(id);
        user.setUsername("john.doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setPhoneNumber("9876543210");
        user.setRole("CUSTOMER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private UserRequestDto mockRequestDto() {
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("john.doe");
        dto.setEmail("john@example.com");
        dto.setFullName("John Doe");
        dto.setPhoneNumber(9876543210L);
        dto.setRole("CUSTOMER");
        return dto;
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser(1), mockUser(2)));

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById_Success() {
        User user = mockUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        verify(userRepository).findById(1);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.getUserById(1));
        assertEquals("404 NOT_FOUND \"User not found with ID: 1\"", ex.getMessage());
    }

    @Test
    void testCreateUser_Success() {
        UserRequestDto requestDto = mockRequestDto();

        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(false);
        when(userRepository.existsByUsername("john.doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = mockUser(1);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto result = userService.createUser(requestDto);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        verify(userRepository).save(userCaptor.capture());
    }

    @Test
    void testCreateUser_UsernameExists() {
        UserRequestDto requestDto = mockRequestDto();

        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(false);
        when(userRepository.existsByUsername("john.doe")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(requestDto));
        assertEquals("400 BAD_REQUEST \"Username already exists: john.doe\"", ex.getMessage());
    }

    @Test
    void testCreateUser_EmailExists() {
        UserRequestDto requestDto = mockRequestDto();

        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(false);
        when(userRepository.existsByUsername("john.doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(requestDto));
        assertEquals("400 BAD_REQUEST \"Email already exists: john@example.com\"", ex.getMessage());
    }

    @Test
    void testCreateUser_PhoneNumberExists() {
        UserRequestDto requestDto = mockRequestDto();

        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(requestDto));
        assertEquals("400 BAD_REQUEST \"User phone number already exist9876543210\"", ex.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        User user = mockUser(1);
        UserRequestDto requestDto = mockRequestDto();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("john.doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.updateUser(1, requestDto);

        assertEquals("john.doe", result.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.updateUser(1, mockRequestDto()));
        assertEquals("404 NOT_FOUND \"User not found with ID: 1\"", ex.getMessage());
    }

    @Test
    void testUpdateUser_UsernameAlreadyExists() {
        User user = mockUser(1);
        UserRequestDto requestDto = mockRequestDto();
        requestDto.setUsername("new.username");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new.username")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.updateUser(1, requestDto));
        assertEquals("400 BAD_REQUEST \"Username already exists: new.username\"", ex.getMessage());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteUser(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1));
        assertEquals("404 NOT_FOUND \"User not found with ID: 1\"", ex.getMessage());
    }
}
