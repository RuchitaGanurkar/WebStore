package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductStatusRequestDto;
import com.webstore.dto.response.cart.CartProductStatusResponseDto;
import com.webstore.service.cart.CartProductStatusService;
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

@DisplayName("Cart Product Status Controller Tests")
class CartProductStatusControllerTest {

    @Mock
    private CartProductStatusService cartProductStatusService;

    @InjectMocks
    private CartProductStatusController cartProductStatusController;

    private CartProductStatusResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockResponse = new CartProductStatusResponseDto();
        mockResponse.setStatusId(1);
        mockResponse.setStatusName("ADDED");
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());
    }

    // ========== GET CART PRODUCT STATUS BY ID TESTS ==========

    @Test
    @DisplayName("Should get cart product status by ID successfully")
    void testGetCartProductStatusById() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.getCartProductStatusById(statusId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("ADDED", response.getBody().getStatusName());
        assertEquals(1, response.getBody().getStatusId());
        verify(cartProductStatusService, times(1)).getCartProductStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle null status ID")
    void testGetCartProductStatusById_NullId() {
        // Given
        Integer statusId = null;
        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.getCartProductStatusById(statusId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).getCartProductStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle zero status ID")
    void testGetCartProductStatusById_ZeroId() {
        // Given
        Integer statusId = 0;
        CartProductStatusResponseDto zeroResponse = createCartProductStatusResponse(0, "ADDED");
        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(zeroResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.getCartProductStatusById(statusId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getStatusId());
        assertEquals("ADDED", response.getBody().getStatusName());
        verify(cartProductStatusService, times(1)).getCartProductStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle negative status ID")
    void testGetCartProductStatusById_NegativeId() {
        // Given
        Integer statusId = -1;
        CartProductStatusResponseDto negativeResponse = createCartProductStatusResponse(-1, "REMOVED");
        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(negativeResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.getCartProductStatusById(statusId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(-1, response.getBody().getStatusId());
        assertEquals("REMOVED", response.getBody().getStatusName());
        verify(cartProductStatusService, times(1)).getCartProductStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle service returning null")
    void testGetCartProductStatusById_ServiceReturnsNull() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(null);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.getCartProductStatusById(statusId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductStatusService, times(1)).getCartProductStatusById(statusId);
    }

    // ========== GET ALL CART PRODUCT STATUSES TESTS ==========

    @Test
    @DisplayName("Should get all cart product statuses successfully")
    void testGetAllCartProductStatuses() {
        // Given
        CartProductStatusResponseDto status1 = createCartProductStatusResponse(1, "ADDED");
        CartProductStatusResponseDto status2 = createCartProductStatusResponse(2, "REMOVED");

        List<CartProductStatusResponseDto> statusList = Arrays.asList(status1, status2);
        when(cartProductStatusService.getAllCartProductStatuses()).thenReturn(statusList);

        // When
        ResponseEntity<List<CartProductStatusResponseDto>> response = cartProductStatusController.getAllCartProductStatuses();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("ADDED", response.getBody().get(0).getStatusName());
        assertEquals("REMOVED", response.getBody().get(1).getStatusName());
        assertEquals(1, response.getBody().get(0).getStatusId());
        assertEquals(2, response.getBody().get(1).getStatusId());
        verify(cartProductStatusService, times(1)).getAllCartProductStatuses();
    }

    @Test
    @DisplayName("Should return empty list when no cart product statuses exist")
    void testGetAllCartProductStatuses_EmptyList() {
        // Given
        when(cartProductStatusService.getAllCartProductStatuses()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<CartProductStatusResponseDto>> response = cartProductStatusController.getAllCartProductStatuses();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(cartProductStatusService, times(1)).getAllCartProductStatuses();
    }

    @Test
    @DisplayName("Should handle service returning null list")
    void testGetAllCartProductStatuses_ServiceReturnsNull() {
        // Given
        when(cartProductStatusService.getAllCartProductStatuses()).thenReturn(null);

        // When
        ResponseEntity<List<CartProductStatusResponseDto>> response = cartProductStatusController.getAllCartProductStatuses();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductStatusService, times(1)).getAllCartProductStatuses();
    }

    // ========== UPDATE CART PRODUCT STATUS TESTS ==========

    @Test
    @DisplayName("Should update cart product status successfully")
    void testUpdateCartProductStatus() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto request = new CartProductStatusRequestDto();
        request.setStatusName("REMOVED");

        CartProductStatusResponseDto updatedResponse = createCartProductStatusResponse(statusId, "REMOVED");
        when(cartProductStatusService.updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.updateCartProductStatus(statusId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("REMOVED", response.getBody().getStatusName());
        assertEquals(statusId, response.getBody().getStatusId());
        verify(cartProductStatusService, times(1)).updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class));
    }

    @Test
    @DisplayName("Should update cart product status with null request")
    void testUpdateCartProductStatus_NullRequest() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto request = null;
        when(cartProductStatusService.updateCartProductStatus(eq(statusId), eq(request))).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.updateCartProductStatus(statusId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).updateCartProductStatus(eq(statusId), eq(request));
    }

    @Test
    @DisplayName("Should update cart product status with empty request")
    void testUpdateCartProductStatus_EmptyRequest() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto request = new CartProductStatusRequestDto(); // Empty request
        when(cartProductStatusService.updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.updateCartProductStatus(statusId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class));
    }

    @Test
    @DisplayName("Should handle update with both enum values")
    void testUpdateCartProductStatus_AllEnumValues() {
        // Test ADDED
        Integer statusId = 1;
        CartProductStatusRequestDto addedRequest = new CartProductStatusRequestDto();
        addedRequest.setStatusName("ADDED");

        CartProductStatusResponseDto addedResponse = createCartProductStatusResponse(statusId, "ADDED");
        when(cartProductStatusService.updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class)))
                .thenReturn(addedResponse);

        ResponseEntity<CartProductStatusResponseDto> response1 = cartProductStatusController.updateCartProductStatus(statusId, addedRequest);
        assertEquals("ADDED", response1.getBody().getStatusName());

        // Test REMOVED
        CartProductStatusRequestDto removedRequest = new CartProductStatusRequestDto();
        removedRequest.setStatusName("REMOVED");

        CartProductStatusResponseDto removedResponse = createCartProductStatusResponse(statusId, "REMOVED");
        when(cartProductStatusService.updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class)))
                .thenReturn(removedResponse);

        ResponseEntity<CartProductStatusResponseDto> response2 = cartProductStatusController.updateCartProductStatus(statusId, removedRequest);
        assertEquals("REMOVED", response2.getBody().getStatusName());

        verify(cartProductStatusService, times(2)).updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class));
    }

    // ========== DELETE CART PRODUCT STATUS TESTS ==========

    @Test
    @DisplayName("Should delete cart product status successfully")
    void testDeleteCartProductStatus() {
        // Given
        Integer statusId = 1;
        doNothing().when(cartProductStatusService).deleteCartProductStatus(statusId);

        // When
        ResponseEntity<Void> response = cartProductStatusController.deleteCartProductStatus(statusId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductStatusService, times(1)).deleteCartProductStatus(statusId);
    }

    @Test
    @DisplayName("Should delete non-existent cart product status")
    void testDeleteCartProductStatus_NonExistent() {
        // Given
        Integer statusId = 999;
        doNothing().when(cartProductStatusService).deleteCartProductStatus(statusId);

        // When
        ResponseEntity<Void> response = cartProductStatusController.deleteCartProductStatus(statusId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).deleteCartProductStatus(statusId);
    }

    @Test
    @DisplayName("Should handle null status ID in delete")
    void testDeleteCartProductStatus_NullId() {
        // Given
        Integer statusId = null;
        doNothing().when(cartProductStatusService).deleteCartProductStatus(statusId);

        // When
        ResponseEntity<Void> response = cartProductStatusController.deleteCartProductStatus(statusId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).deleteCartProductStatus(statusId);
    }

    @Test
    @DisplayName("Should handle zero status ID in delete")
    void testDeleteCartProductStatus_ZeroId() {
        // Given
        Integer statusId = 0;
        doNothing().when(cartProductStatusService).deleteCartProductStatus(statusId);

        // When
        ResponseEntity<Void> response = cartProductStatusController.deleteCartProductStatus(statusId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).deleteCartProductStatus(statusId);
    }

    @Test
    @DisplayName("Should handle negative status ID in delete")
    void testDeleteCartProductStatus_NegativeId() {
        // Given
        Integer statusId = -1;
        doNothing().when(cartProductStatusService).deleteCartProductStatus(statusId);

        // When
        ResponseEntity<Void> response = cartProductStatusController.deleteCartProductStatus(statusId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductStatusService, times(1)).deleteCartProductStatus(statusId);
    }

    // ========== INTEGRATION AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle multiple consecutive calls")
    void testMultipleConsecutiveCalls() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(mockResponse);

        // When - Multiple calls
        ResponseEntity<CartProductStatusResponseDto> response1 = cartProductStatusController.getCartProductStatusById(statusId);
        ResponseEntity<CartProductStatusResponseDto> response2 = cartProductStatusController.getCartProductStatusById(statusId);

        // Then
        assertEquals(200, response1.getStatusCodeValue());
        assertEquals(200, response2.getStatusCodeValue());
        assertEquals("ADDED", response1.getBody().getStatusName());
        assertEquals("ADDED", response2.getBody().getStatusName());
        verify(cartProductStatusService, times(2)).getCartProductStatusById(statusId);
    }

    @Test
    @DisplayName("Should handle different operations on same status ID")
    void testDifferentOperationsOnSameStatusId() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto updateRequest = new CartProductStatusRequestDto();
        updateRequest.setStatusName("REMOVED");

        when(cartProductStatusService.getCartProductStatusById(statusId)).thenReturn(mockResponse);
        when(cartProductStatusService.updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class)))
                .thenReturn(createCartProductStatusResponse(statusId, "REMOVED"));
        doNothing().when(cartProductStatusService).deleteCartProductStatus(statusId);

        // When - Different operations
        ResponseEntity<CartProductStatusResponseDto> getResponse = cartProductStatusController.getCartProductStatusById(statusId);
        ResponseEntity<CartProductStatusResponseDto> updateResponse = cartProductStatusController.updateCartProductStatus(statusId, updateRequest);
        ResponseEntity<Void> deleteResponse = cartProductStatusController.deleteCartProductStatus(statusId);

        // Then
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals(200, updateResponse.getStatusCodeValue());
        assertEquals(204, deleteResponse.getStatusCodeValue());
        assertEquals("ADDED", getResponse.getBody().getStatusName());
        assertEquals("REMOVED", updateResponse.getBody().getStatusName());

        verify(cartProductStatusService, times(1)).getCartProductStatusById(statusId);
        verify(cartProductStatusService, times(1)).updateCartProductStatus(eq(statusId), any(CartProductStatusRequestDto.class));
        verify(cartProductStatusService, times(1)).deleteCartProductStatus(statusId);
    }

    @Test
    @DisplayName("Should handle large status ID values")
    void testLargeStatusIdValues() {
        // Given
        Integer largeStatusId = Integer.MAX_VALUE;
        CartProductStatusResponseDto largeIdResponse = createCartProductStatusResponse(largeStatusId, "ADDED");
        when(cartProductStatusService.getCartProductStatusById(largeStatusId)).thenReturn(largeIdResponse);

        // When
        ResponseEntity<CartProductStatusResponseDto> response = cartProductStatusController.getCartProductStatusById(largeStatusId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(Integer.MAX_VALUE, response.getBody().getStatusId());
        verify(cartProductStatusService, times(1)).getCartProductStatusById(largeStatusId);
    }

    // ========== HELPER METHODS ==========

    private CartProductStatusResponseDto createCartProductStatusResponse(Integer statusId, String statusName) {
        CartProductStatusResponseDto response = new CartProductStatusResponseDto();
        response.setStatusId(statusId);
        response.setStatusName(statusName);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}