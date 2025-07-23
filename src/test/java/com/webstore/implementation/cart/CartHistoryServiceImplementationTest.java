package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartHistoryRequestDto;
import com.webstore.dto.response.cart.CartHistoryResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartHistory;
import com.webstore.exception.cart.CartHistoryNotFoundException;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.repository.cart.CartHistoryRepository;
import com.webstore.repository.cart.CartRepository;
//import com.webstore.implementation.cart.CartHistoryServiceImplementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartHistoryServiceImplementationTest {

    @InjectMocks
    private CartHistoryServiceImplementation service;

    @Mock
    private CartHistoryRepository cartHistoryRepository;

    @Mock
    private CartRepository cartRepository;

    private Cart testCart;
    private CartHistory testCartHistory;

    @BeforeEach
    void setup() {
        testCart = new Cart();
        testCart.setCartId(1L);

        testCartHistory = new CartHistory();
        testCartHistory.setCartHistoryId(10L);
        testCartHistory.setCart(testCart);
        testCartHistory.setCreatedAt(LocalDateTime.now());
        testCartHistory.setCreatedBy("admin");
        testCartHistory.setUpdatedAt(LocalDateTime.now());
        testCartHistory.setUpdatedBy("admin");
    }

    @Test
    void createCartHistory_success() {
        CartHistoryRequestDto dto = new CartHistoryRequestDto();
        dto.setCartId(1L);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(testCartHistory);

        CartHistoryResponseDto response = service.createCartHistory(dto);

        assertNotNull(response);
        assertEquals(10L, response.getCartHistoryId());
        verify(cartRepository).findById(1L);
        verify(cartHistoryRepository).save(any(CartHistory.class));
    }

    @Test
    void createCartHistory_cartNotFound() {
        CartHistoryRequestDto dto = new CartHistoryRequestDto();
        dto.setCartId(999L);

        when(cartRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> service.createCartHistory(dto));
    }

    @Test
    void getCartHistoryById_success() {
        when(cartHistoryRepository.findById(10L)).thenReturn(Optional.of(testCartHistory));

        CartHistoryResponseDto response = service.getCartHistoryById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getCartHistoryId());
    }

    @Test
    void getCartHistoryById_notFound() {
        when(cartHistoryRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(CartHistoryNotFoundException.class, () -> service.getCartHistoryById(100L));
    }

    @Test
    void getCartHistoriesByCartId_success() {
        when(cartHistoryRepository.findByCartIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testCartHistory));

        List<CartHistoryResponseDto> result = service.getCartHistoriesByCartId(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getCartHistoryId());
    }

    @Test
    void getCartHistoriesByCreatedBy_success() {
        when(cartHistoryRepository.findByCreatedBy("admin")).thenReturn(List.of(testCartHistory));

        List<CartHistoryResponseDto> result = service.getCartHistoriesByCreatedBy("admin");

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getCreatedBy());
    }

    @Test
    void getCartHistoriesByUpdatedBy_success() {
        when(cartHistoryRepository.findByUpdatedBy("admin")).thenReturn(List.of(testCartHistory));

        List<CartHistoryResponseDto> result = service.getCartHistoriesByUpdatedBy("admin");

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUpdatedBy());
    }

    @Test
    void getCartHistoriesByDateRange_success() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(cartHistoryRepository.findByDateRange(start, end)).thenReturn(List.of(testCartHistory));

        List<CartHistoryResponseDto> result = service.getCartHistoriesByDateRange(start, end);

        assertEquals(1, result.size());
    }
}
