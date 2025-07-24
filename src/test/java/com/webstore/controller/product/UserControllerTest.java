package com.webstore.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.product.UserRequestDto;
import com.webstore.dto.response.product.UserResponseDto;
import com.webstore.service.product.UserService;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        requestDto = new UserRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setEmail("test@example.com");
        requestDto.setFullName("Test User");
        requestDto.setRole("USER");
        requestDto.setPhoneNumber(1234567890L); // Added required phone number

        responseDto = new UserResponseDto();
        responseDto.setUserId(1);
        responseDto.setUsername("testuser");
        responseDto.setEmail("test@example.com");
        responseDto.setFullName("Test User");
        responseDto.setRole("USER");
        responseDto.setPhoneNumber(1234567890L); // Added phone number
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")))
                .andExpect(jsonPath("$[0].phoneNumber", is(1234567890)));
    }

    @Test
    void testGetAllUsers_WhenEmpty() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetUserById() throws Exception {
        when(userService.getUserById(1)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is(1234567890)));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(99))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found with ID: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99);
    }

    @Test
    void testCreateUser_Success() throws Exception {
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is(1234567890)));

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void testCreateUser_UsernameExists() throws Exception {
        when(userService.createUser(any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Username already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void testCreateUser_EmailExists() throws Exception {
        when(userService.createUser(any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Email already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void testCreateUser_PhoneNumberExists() throws Exception {
        when(userService.createUser(any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Phone number already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        when(userService.updateUser(eq(1), any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).updateUser(eq(1), any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        when(userService.updateUser(eq(99), any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found with ID: 99"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(99), any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser_EmailConflict() throws Exception {
        when(userService.updateUser(eq(1), any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Email already exists"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(1), any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser_UsernameConflict() throws Exception {
        when(userService.updateUser(eq(1), any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Username already exists"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(1), any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser_PhoneNumberConflict() throws Exception {
        when(userService.updateUser(eq(1), any(UserRequestDto.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Phone number already exists"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(1), any(UserRequestDto.class));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found with ID: 99"))
                .when(userService).deleteUser(99);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(99);
    }

    @Test
    void testCreateUser_ValidationError() throws Exception {
        UserRequestDto invalidRequest = new UserRequestDto();
        // Missing required fields will trigger validation errors

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserRequestDto.class));
    }

    @Test
    void testUpdateUser_ValidationError() throws Exception {
        UserRequestDto invalidRequest = new UserRequestDto();
        // Missing required fields will trigger validation errors

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(), any(UserRequestDto.class));
    }

    @Test
    void testGetUserById_WithInvalidId() throws Exception {
        mockMvc.perform(get("/api/users/invalid"))
                .andExpect(status().isBadRequest()); // Spring will return 400 for invalid path variable type

        verify(userService, never()).getUserById(any());
    }

    @Test
    void testCreateUser_WithNullBody() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Missing request body

        verify(userService, never()).createUser(any());
    }
}