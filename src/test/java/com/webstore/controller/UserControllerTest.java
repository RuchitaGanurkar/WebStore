package com.webstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import com.webstore.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserRequestDto requestDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        requestDto = new UserRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setEmail("test@example.com");
        requestDto.setFullName("Test User");
        requestDto.setRole("USER");

        responseDto = new UserResponseDto();
        responseDto.setUserId(1);
        responseDto.setUsername("testuser");
        responseDto.setEmail("test@example.com");
        responseDto.setFullName("Test User");
        responseDto.setRole("USER");
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllUsers() throws Exception {
        List<UserResponseDto> users = Arrays.asList(responseDto);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")))
                .andExpect(jsonPath("$[0].fullName", is("Test User")))
                .andExpect(jsonPath("$[0].role", is("USER")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserById() throws Exception {
        when(userService.getUserById(1)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(99)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99);
    }

    @Test
    void testCreateUser() throws Exception {
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void testCreateUserWithValidationFailure() throws Exception {
        when(userService.createUser(any(UserRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid user data"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", is("Invalid user data")));

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.updateUser(eq(1), any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userService, times(1)).updateUser(eq(1), any(UserRequestDto.class));
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        when(userService.updateUser(eq(99), any(UserRequestDto.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(99), any(UserRequestDto.class));
    }

    @Test
    void testUpdateUserValidationFailure() throws Exception {
        when(userService.updateUser(eq(1), any(UserRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid user data"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", is("Invalid user data")));

        verify(userService, times(1)).updateUser(eq(1), any(UserRequestDto.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(userService).deleteUser(99);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(99);
    }
}