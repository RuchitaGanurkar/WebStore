package com.webstore.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.service.cart.CartService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponseDto cartResponseDto;
    private CartRequestDto cartRequestDto;

    @BeforeEach
    void setUp() {
        cartResponseDto = new CartResponseDto();
        cartResponseDto.setCartId(1L);
        cartResponseDto.setPhoneNumber(9876543210L);
        cartResponseDto.setCatalogueId(1);
        cartResponseDto.setStatusId(1);
        cartResponseDto.setCreatedAt(LocalDateTime.now());
        cartResponseDto.setUpdatedAt(LocalDateTime.now());

        cartRequestDto = new CartRequestDto();
        cartRequestDto.setPhoneNumber(9876543210L);
        cartRequestDto.setCatalogueId(1);
        cartRequestDto.setStatusId(1);
    }

    @Test
    void testCreateCart() throws Exception {
        Mockito.when(cartService.createCart(any(CartRequestDto.class))).thenReturn(cartResponseDto);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.catalogueId").value(1));
    }

    @Test
    void testGetActiveCartByPhoneNumber() throws Exception {
        Mockito.when(cartService.getActiveCartByPhoneNumber(9876543210L)).thenReturn(cartResponseDto);

        mockMvc.perform(get("/api/carts/user/9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(9876543210L))
                .andExpect(jsonPath("$.catalogueId").value(1));
    }

    @Test
    void testGetCartById() throws Exception {
        Mockito.when(cartService.getCartById(1L)).thenReturn(cartResponseDto);

        mockMvc.perform(get("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.catalogueId").value(1));
    }

    @Test
    void testGetCartsByStatus() throws Exception {
        Mockito.when(cartService.getCartsByStatus("ACTIVE")).thenReturn(List.of(cartResponseDto));

        mockMvc.perform(get("/api/carts")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].catalogueId").value(1));
    }

    @Test
    void testUpdateCartStatus() throws Exception {
        Mockito.when(cartService.updateCartStatus(Mockito.eq(1L), Mockito.eq(1))).thenReturn(cartResponseDto);

        mockMvc.perform(put("/api/carts/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusId").value(1))
                .andExpect(jsonPath("$.catalogueId").value(1));
    }

    @Test
    void testArchiveCart() throws Exception {
        Mockito.when(cartService.archiveCart(1L)).thenReturn("Cart archived successfully, cart_id: 1");

        mockMvc.perform(delete("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart archived successfully, cart_id: 1"));
    }

    @Test
    void testCreateCartValidation() throws Exception {
        // Test with invalid phone number
        CartRequestDto invalidRequest = new CartRequestDto();
        invalidRequest.setPhoneNumber(null);
        invalidRequest.setCatalogueId(1);
        invalidRequest.setStatusId(1);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCartWithInvalidCatalogue() throws Exception {
        // Test with null catalogue
        CartRequestDto invalidRequest = new CartRequestDto();
        invalidRequest.setPhoneNumber(9876543210L);
        invalidRequest.setCatalogueId(null);
        invalidRequest.setStatusId(1);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCartWithInvalidPhoneNumber() throws Exception {
        // Test with invalid phone number (less than 10 digits)
        CartRequestDto invalidRequest = new CartRequestDto();
        invalidRequest.setPhoneNumber(123456789L); // Only 9 digits
        invalidRequest.setCatalogueId(1);
        invalidRequest.setStatusId(1);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCartNotFound() throws Exception {
        Mockito.when(cartService.getCartById(999L))
                .thenThrow(new RuntimeException("Cart not found with ID: 999"));

        mockMvc.perform(get("/api/carts/999"))
                .andExpect(status().isInternalServerError()); // Adjust based on your error handling
    }

//    @Test
//    void testGetActiveCartByInvalidPhoneNumber() throws Exception {
//        // Test with invalid phone number in path variable
//        mockMvc.perform(get("/api/carts/user/123456789")) // Less than 10 digits
//                .andExpect(status().isBadRequest())
//                .andExpect(result -> assertTrue(
//                        result.getResolvedException() instanceof ConstraintViolationException ||
//                                result.getResolvedException().getMessage().contains("Phone number must be at least 10 digits")
//                ));
//    }

    @Test
    void testUpdateCartStatusWithInvalidId() throws Exception {
        Mockito.when(cartService.updateCartStatus(Mockito.eq(999L), Mockito.eq(1)))
                .thenThrow(new RuntimeException("Cart not found with ID: 999"));

        mockMvc.perform(put("/api/carts/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isInternalServerError()); // Adjust based on your error handling
    }

    @Test
    void testArchiveNonExistentCart() throws Exception {
        Mockito.when(cartService.archiveCart(999L))
                .thenThrow(new RuntimeException("Cart not found with ID: 999"));

        mockMvc.perform(delete("/api/carts/999"))
                .andExpect(status().isInternalServerError()); // Adjust based on your error handling
    }
}