package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.service.cart.CartProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartProductControllerTest {

    @InjectMocks
    private CartProductController cartProductController;

    @Mock
    private CartProductService cartProductService;

    private CartProductResponseDto responseDto;
    private CartProductRequestDto requestDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        responseDto = new CartProductResponseDto();
        responseDto.setCartProductId(1L);
        responseDto.setCartId(100L);
        responseDto.setStatusId(1);
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());

        requestDto = new CartProductRequestDto();
        requestDto.setCartId(100L);
        requestDto.setStatusId(1);
    }

    @Test
    public void testCreateCartProduct() {
        when(cartProductService.createCartProduct(any())).thenReturn(responseDto);

        ResponseEntity<CartProductResponseDto> response = cartProductController.create(requestDto);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getCartProductId());
    }

    @Test
    public void testGetById() {
        when(cartProductService.getCartProductById(1L)).thenReturn(responseDto);

        ResponseEntity<CartProductResponseDto> response = cartProductController.getById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(100L, response.getBody().getCartId());
    }

    @Test
    public void testGetByCartId() {
        when(cartProductService.getCartProductsByCartId(100L)).thenReturn(List.of(responseDto));

        ResponseEntity<List<CartProductResponseDto>> response = cartProductController.getByCartId(100L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getCartProductId());
    }

    @Test
    public void testCountActive() {
        when(cartProductService.countActiveCartProducts(100L)).thenReturn(5L);

        ResponseEntity<Long> response = cartProductController.countActive(100L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(5L, response.getBody());
    }

    @Test
    public void testGetByPhone() {
        when(cartProductService.getActiveCartProductsByPhoneNumber("9876543210"))
                .thenReturn(List.of(responseDto));

        ResponseEntity<List<CartProductResponseDto>> response = cartProductController.getByPhone("9876543210");

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1L, response.getBody().get(0).getCartProductId());
    }

    @Test
    public void testDelete() {
        doNothing().when(cartProductService).deleteCartProduct(1L);

        ResponseEntity<Void> response = cartProductController.delete(1L);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductService, times(1)).deleteCartProduct(1L);
    }
}
