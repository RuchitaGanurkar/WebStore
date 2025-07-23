package com.webstore.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.cart.CartHistoryRequestDto;
import com.webstore.dto.response.cart.CartHistoryResponseDto;
import com.webstore.service.cart.CartHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartHistoryService cartHistoryService;

    @InjectMocks
    private CartHistoryController cartHistoryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartHistoryController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createCartHistory_success() throws Exception {
        CartHistoryRequestDto requestDto = new CartHistoryRequestDto();
        requestDto.setCartId(1L);

        CartHistoryResponseDto responseDto = new CartHistoryResponseDto();
        responseDto.setCartId(1L);
        responseDto.setCartHistoryId(100L);

        when(cartHistoryService.createCartHistory(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/cart-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.cartHistoryId").value(100L));

        verify(cartHistoryService, times(1)).createCartHistory(any());
    }

    @Test
    void getCartHistoryById_success() throws Exception {
        Long historyId = 100L;
        CartHistoryResponseDto responseDto = new CartHistoryResponseDto();
        responseDto.setCartHistoryId(historyId);
        responseDto.setCartId(1L);

        when(cartHistoryService.getCartHistoryById(historyId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cart-histories/{id}", historyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartHistoryId").value(100L));
    }

    @Test
    void getCartHistoriesByCartId_success() throws Exception {
        Long cartId = 1L;

        CartHistoryResponseDto dto1 = new CartHistoryResponseDto();
        dto1.setCartHistoryId(1L);
        dto1.setCartId(cartId);

        CartHistoryResponseDto dto2 = new CartHistoryResponseDto();
        dto2.setCartHistoryId(2L);
        dto2.setCartId(cartId);

        when(cartHistoryService.getCartHistoriesByCartId(cartId))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/cart-histories/cart/{cartId}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCartHistoriesByCreatedBy_success() throws Exception {
        String username = "test_user";

        CartHistoryResponseDto dto = new CartHistoryResponseDto();
        dto.setCreatedBy(username);

        when(cartHistoryService.getCartHistoriesByCreatedBy(username))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cart-histories/created-by/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].createdBy").value("test_user"));
    }

    @Test
    void getCartHistoriesByUpdatedBy_success() throws Exception {
        String username = "admin";

        CartHistoryResponseDto dto = new CartHistoryResponseDto();
        dto.setUpdatedBy(username);

        when(cartHistoryService.getCartHistoriesByUpdatedBy(username))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cart-histories/updated-by/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].updatedBy").value("admin"));
    }

    @Test
    void getCartHistoriesByDateRange_success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        CartHistoryResponseDto dto = new CartHistoryResponseDto();
        dto.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 30));

        when(cartHistoryService.getCartHistoriesByDateRange(start, end))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cart-histories/date-range")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
