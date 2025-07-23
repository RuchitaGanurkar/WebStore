package com.webstore.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.service.cart.CartService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        cartResponseDto.setCatalogueCategoryId(10);
        cartResponseDto.setStatusId(1);
        cartResponseDto.setCreatedAt(LocalDateTime.now());
        cartResponseDto.setUpdatedAt(LocalDateTime.now());

        cartRequestDto = new CartRequestDto();
        cartRequestDto.setPhoneNumber(9876543210L);
        cartRequestDto.setCatalogueCategoryId(10);
        cartRequestDto.setStatusId(1);
    }

    @Test
    void testCreateCart() throws Exception {
        Mockito.when(cartService.createCart(any(CartRequestDto.class))).thenReturn(cartResponseDto);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));
    }

    @Test
    void testGetActiveCartByPhoneNumber() throws Exception {
        Mockito.when(cartService.getActiveCartByPhoneNumber(9876543210L)).thenReturn(cartResponseDto);

        mockMvc.perform(get("/api/carts/user/9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(9876543210L));
    }

    @Test
    void testGetCartById() throws Exception {
        Mockito.when(cartService.getCartById(1L)).thenReturn(cartResponseDto);

        mockMvc.perform(get("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));
    }

    @Test
    void testGetCartsByStatus() throws Exception {
        Mockito.when(cartService.getCartsByStatus("ACTIVE")).thenReturn(List.of(cartResponseDto));

        mockMvc.perform(get("/api/carts")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testUpdateCartStatus() throws Exception {
        Mockito.when(cartService.updateCartStatus(Mockito.eq(1L), Mockito.eq(1))).thenReturn(cartResponseDto);

        mockMvc.perform(put("/api/carts/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusId").value(1));
    }

    @Test
    void testArchiveCart() throws Exception {
        Mockito.when(cartService.archiveCart(1L)).thenReturn("Cart archived successfully");

        mockMvc.perform(delete("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart archived successfully"));
    }
}
