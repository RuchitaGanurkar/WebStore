package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartStatusRequestDto;
import com.webstore.dto.response.cart.CartStatusResponseDto;
import com.webstore.service.cart.CartStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Cart Status Controller Tests")
class CartStatusControllerTest {

    @Mock
    private CartStatusService cartStatusService;

    @InjectMocks
    private CartStatusController cartStatusController;

    private CartStatusResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockResponse = new CartStatusResponseDto();
        mockResponse.setStatusId(1);
        mockResponse.setStatusName("ACTIVE");
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get cart status by ID successfully")
    void testGetCartStatusById() {

        Integer statusId = 1;
        when(cartStatusService.getCartStatusById(statusId)).thenReturn(mockResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.getCartStatusById(statusId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("ACTIVE", response.getBody().getStatusName());
        assertEquals(1, response.getBody().getStatusId());
        verify(cartStatusService, times(1)).getCartStatusById(statusId);
    }

    @Test
    @DisplayName("Should get all cart statuses successfully")
    void testGetAllCartStatuses() {

        CartStatusResponseDto status1 = createCartStatusResponse(1, "ACTIVE");
        CartStatusResponseDto status2 = createCartStatusResponse(2, "INACTIVE");
        CartStatusResponseDto status3 = createCartStatusResponse(3, "PENDING");

        List<CartStatusResponseDto> statusList = Arrays.asList(status1, status2, status3);
        when(cartStatusService.getAllCartStatuses()).thenReturn(statusList);

        ResponseEntity<List<CartStatusResponseDto>> response = cartStatusController.getAllCartStatuses();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3, response.getBody().size());
        assertEquals("ACTIVE", response.getBody().get(0).getStatusName());
        assertEquals("INACTIVE", response.getBody().get(1).getStatusName());
        assertEquals("PENDING", response.getBody().get(2).getStatusName());
        verify(cartStatusService, times(1)).getAllCartStatuses();
    }

    @Test
    @DisplayName("Should return empty list when no cart statuses exist")
    void testGetAllCartStatuses_EmptyList() {
        when(cartStatusService.getAllCartStatuses()).thenReturn(Collections.emptyList());

        ResponseEntity<List<CartStatusResponseDto>> response = cartStatusController.getAllCartStatuses();

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(cartStatusService, times(1)).getAllCartStatuses();
    }

    @Test
    @DisplayName("Should update cart status successfully")
    void testUpdateCartStatus() {

        Integer statusId = 1;
        CartStatusRequestDto request = new CartStatusRequestDto();
        request.setStatusName("UPDATED");

        CartStatusResponseDto updatedResponse = createCartStatusResponse(statusId, "UPDATED");
        when(cartStatusService.updateCartStatus(eq(statusId), any(CartStatusRequestDto.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.updateCartStatus(statusId, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("UPDATED", response.getBody().getStatusName());
        assertEquals(statusId, response.getBody().getStatusId());
        verify(cartStatusService, times(1)).updateCartStatus(eq(statusId), any(CartStatusRequestDto.class));
    }

    @Test
    @DisplayName("Should delete cart status successfully")
    void testDeleteCartStatus() {

        Integer statusId = 1;
        doNothing().when(cartStatusService).deleteCartStatus(statusId);

        ResponseEntity<Void> response = cartStatusController.deleteCartStatus(statusId);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartStatusService, times(1)).deleteCartStatus(statusId);
    }

    @Test
    @DisplayName("Should handle null status ID")
    void testGetCartStatusById_NullId() {

        Integer statusId = null;
        when(cartStatusService.getCartStatusById(statusId)).thenReturn(mockResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.getCartStatusById(statusId);

        assertEquals(200, response.getStatusCodeValue());
        verify(cartStatusService, times(1)).getCartStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle zero status ID")
    void testGetCartStatusById_ZeroId() {

        Integer statusId = 0;
        CartStatusResponseDto zeroResponse = createCartStatusResponse(0, "ZERO");
        when(cartStatusService.getCartStatusById(statusId)).thenReturn(zeroResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.getCartStatusById(statusId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getStatusId());
        assertEquals("ZERO", response.getBody().getStatusName());
        verify(cartStatusService, times(1)).getCartStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle negative status ID")
    void testGetCartStatusById_NegativeId() {

        Integer statusId = -1;
        CartStatusResponseDto negativeResponse = createCartStatusResponse(-1, "NEGATIVE");
        when(cartStatusService.getCartStatusById(statusId)).thenReturn(negativeResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.getCartStatusById(statusId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(-1, response.getBody().getStatusId());
        assertEquals("NEGATIVE", response.getBody().getStatusName());
        verify(cartStatusService, times(1)).getCartStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle service returning null")
    void testGetCartStatusById_ServiceReturnsNull() {

        Integer statusId = 1;
        when(cartStatusService.getCartStatusById(statusId)).thenReturn(null);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.getCartStatusById(statusId);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartStatusService, times(1)).getCartStatusById(statusId);
    }

    @Test
    @DisplayName("Should update cart status with null request")
    void testUpdateCartStatus_NullRequest() {

        Integer statusId = 1;
        CartStatusRequestDto request = null;
        when(cartStatusService.updateCartStatus(eq(statusId), eq(request))).thenReturn(mockResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.updateCartStatus(statusId, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(cartStatusService, times(1)).updateCartStatus(eq(statusId), eq(request));
    }

    @Test
    @DisplayName("Should update cart status with empty request")
    void testUpdateCartStatus_EmptyRequest() {

        Integer statusId = 1;
        CartStatusRequestDto request = new CartStatusRequestDto(); // Empty request
        when(cartStatusService.updateCartStatus(eq(statusId), any(CartStatusRequestDto.class)))
                .thenReturn(mockResponse);

        ResponseEntity<CartStatusResponseDto> response = cartStatusController.updateCartStatus(statusId, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(cartStatusService, times(1)).updateCartStatus(eq(statusId), any(CartStatusRequestDto.class));
    }

    @Test
    @DisplayName("Should delete non-existent cart status")
    void testDeleteCartStatus_NonExistent() {

        Integer statusId = 999;
        doNothing().when(cartStatusService).deleteCartStatus(statusId);

        ResponseEntity<Void> response = cartStatusController.deleteCartStatus(statusId);

        assertEquals(204, response.getStatusCodeValue());
        verify(cartStatusService, times(1)).deleteCartStatus(statusId);
    }

    @Test
    @DisplayName("Should handle multiple consecutive calls")
    void testMultipleConsecutiveCalls() {

        Integer statusId = 1;
        when(cartStatusService.getCartStatusById(statusId)).thenReturn(mockResponse);

        ResponseEntity<CartStatusResponseDto> response1 = cartStatusController.getCartStatusById(statusId);
        ResponseEntity<CartStatusResponseDto> response2 = cartStatusController.getCartStatusById(statusId);

        assertEquals(200, response1.getStatusCodeValue());
        assertEquals(200, response2.getStatusCodeValue());
        assertEquals("ACTIVE", response1.getBody().getStatusName());
        assertEquals("ACTIVE", response2.getBody().getStatusName());
        verify(cartStatusService, times(2)).getCartStatusById(statusId);
    }

    private CartStatusResponseDto createCartStatusResponse(Integer statusId, String statusName) {
        CartStatusResponseDto response = new CartStatusResponseDto();
        response.setStatusId(statusId);
        response.setStatusName(statusName);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}