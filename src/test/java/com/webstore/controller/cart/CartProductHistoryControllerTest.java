package com.webstore.controller.cart;

import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import com.webstore.service.cart.CartProductHistoryService;
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

@DisplayName("Cart Product History Controller Tests")
class CartProductHistoryControllerTest {

    @Mock
    private CartProductHistoryService cartProductHistoryService;

    @InjectMocks
    private CartProductHistoryController cartProductHistoryController;

    private CartProductHistoryResponseDto mockResponse;
    private CartProductHistoryRequestDto mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockResponse = new CartProductHistoryResponseDto();
        mockResponse.setCartProductHistoryId(1L);
        mockResponse.setCartProductId(1L);
        mockResponse.setProductId(100);
        mockResponse.setOldQuantity(5);
        mockResponse.setNewQuantity(10);
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());

        mockRequest = new CartProductHistoryRequestDto();
        mockRequest.setCartProductId(1L);
        mockRequest.setProductId(100);
        mockRequest.setOldQuantity(5);
        mockRequest.setNewQuantity(10);
    }

    // ========== CREATE CART PRODUCT HISTORY TESTS ==========

    @Test
    @DisplayName("Should create cart product history successfully")
    void testCreateCartProductHistory() {
        // Given
        when(cartProductHistoryService.createCartProductHistory(any(CartProductHistoryRequestDto.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.createCartProductHistory(mockRequest);

        // Then
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getCartProductHistoryId());
        assertEquals(1L, response.getBody().getCartProductId());
        assertEquals(100, response.getBody().getProductId());
        assertEquals(5, response.getBody().getOldQuantity());
        assertEquals(10, response.getBody().getNewQuantity());
        verify(cartProductHistoryService, times(1)).createCartProductHistory(any(CartProductHistoryRequestDto.class));
    }

    @Test
    @DisplayName("Should handle null request in create")
    void testCreateCartProductHistory_NullRequest() {
        // Given
        when(cartProductHistoryService.createCartProductHistory(null)).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.createCartProductHistory(null);

        // Then
        assertEquals(201, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).createCartProductHistory(null);
    }

    @Test
    @DisplayName("Should handle empty request in create")
    void testCreateCartProductHistory_EmptyRequest() {
        // Given
        CartProductHistoryRequestDto emptyRequest = new CartProductHistoryRequestDto();
        when(cartProductHistoryService.createCartProductHistory(any(CartProductHistoryRequestDto.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.createCartProductHistory(emptyRequest);

        // Then
        assertEquals(201, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).createCartProductHistory(any(CartProductHistoryRequestDto.class));
    }

    @Test
    @DisplayName("Should handle service returning null in create")
    void testCreateCartProductHistory_ServiceReturnsNull() {
        // Given
        when(cartProductHistoryService.createCartProductHistory(any(CartProductHistoryRequestDto.class)))
                .thenReturn(null);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.createCartProductHistory(mockRequest);

        // Then
        assertEquals(201, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductHistoryService, times(1)).createCartProductHistory(any(CartProductHistoryRequestDto.class));
    }

    // ========== GET ALL CART PRODUCT HISTORY TESTS ==========

    @Test
    @DisplayName("Should get all cart product history successfully")
    void testGetAllCartProductHistory() {
        // Given
        CartProductHistoryResponseDto history1 = createCartProductHistoryResponse(1L, 1L, 100, 5, 10);
        CartProductHistoryResponseDto history2 = createCartProductHistoryResponse(2L, 2L, 200, 3, 7);

        List<CartProductHistoryResponseDto> historyList = Arrays.asList(history1, history2);
        when(cartProductHistoryService.getAllCartProductHistory()).thenReturn(historyList);

        // When
        ResponseEntity<List<CartProductHistoryResponseDto>> response =
                cartProductHistoryController.getAllCartProductHistory();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getCartProductHistoryId());
        assertEquals(2L, response.getBody().get(1).getCartProductHistoryId());
        assertEquals(100, response.getBody().get(0).getProductId());
        assertEquals(200, response.getBody().get(1).getProductId());
        verify(cartProductHistoryService, times(1)).getAllCartProductHistory();
    }

    @Test
    @DisplayName("Should return empty list when no cart product history exists")
    void testGetAllCartProductHistory_EmptyList() {
        // Given
        when(cartProductHistoryService.getAllCartProductHistory()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<CartProductHistoryResponseDto>> response =
                cartProductHistoryController.getAllCartProductHistory();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(cartProductHistoryService, times(1)).getAllCartProductHistory();
    }

    @Test
    @DisplayName("Should handle service returning null list")
    void testGetAllCartProductHistory_ServiceReturnsNull() {
        // Given
        when(cartProductHistoryService.getAllCartProductHistory()).thenReturn(null);

        // When
        ResponseEntity<List<CartProductHistoryResponseDto>> response =
                cartProductHistoryController.getAllCartProductHistory();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductHistoryService, times(1)).getAllCartProductHistory();
    }

    // ========== GET CART PRODUCT HISTORY BY ID TESTS ==========

    @Test
    @DisplayName("Should get cart product history by ID successfully")
    void testGetCartProductHistoryById() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.getCartProductHistoryById(historyId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getCartProductHistoryId());
        assertEquals(1L, response.getBody().getCartProductId());
        assertEquals(100, response.getBody().getProductId());
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
    }

    @Test
    @DisplayName("Should handle null history ID")
    void testGetCartProductHistoryById_NullId() {
        // Given
        Long historyId = null;
        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.getCartProductHistoryById(historyId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
    }

    @Test
    @DisplayName("Should handle zero history ID")
    void testGetCartProductHistoryById_ZeroId() {
        // Given
        Long historyId = 0L;
        CartProductHistoryResponseDto zeroResponse = createCartProductHistoryResponse(0L, 1L, 100, 5, 10);
        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(zeroResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.getCartProductHistoryById(historyId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0L, response.getBody().getCartProductHistoryId());
        assertEquals(100, response.getBody().getProductId());
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
    }

    @Test
    @DisplayName("Should handle negative history ID")
    void testGetCartProductHistoryById_NegativeId() {
        // Given
        Long historyId = -1L;
        CartProductHistoryResponseDto negativeResponse = createCartProductHistoryResponse(-1L, 1L, 100, 5, 10);
        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(negativeResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.getCartProductHistoryById(historyId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(-1L, response.getBody().getCartProductHistoryId());
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
    }

    @Test
    @DisplayName("Should handle service returning null")
    void testGetCartProductHistoryById_ServiceReturnsNull() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(null);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.getCartProductHistoryById(historyId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
    }

    // ========== UPDATE CART PRODUCT HISTORY TESTS ==========

    @Test
    @DisplayName("Should update cart product history successfully")
    void testUpdateCartProductHistory() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto updateRequest = new CartProductHistoryRequestDto();
        updateRequest.setCartProductId(1L);
        updateRequest.setProductId(100);
        updateRequest.setOldQuantity(10);
        updateRequest.setNewQuantity(15);

        CartProductHistoryResponseDto updatedResponse = createCartProductHistoryResponse(historyId, 1L, 100, 10, 15);
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.updateCartProductHistory(historyId, updateRequest);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(historyId, response.getBody().getCartProductHistoryId());
        assertEquals(10, response.getBody().getOldQuantity());
        assertEquals(15, response.getBody().getNewQuantity());
        verify(cartProductHistoryService, times(1)).updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class));
    }

    @Test
    @DisplayName("Should update cart product history with null request")
    void testUpdateCartProductHistory_NullRequest() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto request = null;
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), eq(request))).thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.updateCartProductHistory(historyId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).updateCartProductHistory(eq(historyId), eq(request));
    }

    @Test
    @DisplayName("Should update cart product history with empty request")
    void testUpdateCartProductHistory_EmptyRequest() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto request = new CartProductHistoryRequestDto(); // Empty request
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.updateCartProductHistory(historyId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class));
    }

    @Test
    @DisplayName("Should handle null history ID in update")
    void testUpdateCartProductHistory_NullHistoryId() {
        // Given
        Long historyId = null;
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.updateCartProductHistory(historyId, mockRequest);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class));
    }

    @Test
    @DisplayName("Should handle different quantity values in update")
    void testUpdateCartProductHistory_DifferentQuantities() {
        // Test with zero quantities
        Long historyId = 1L;
        CartProductHistoryRequestDto zeroRequest = new CartProductHistoryRequestDto();
        zeroRequest.setCartProductId(1L);
        zeroRequest.setProductId(100);
        zeroRequest.setOldQuantity(0);
        zeroRequest.setNewQuantity(0);

        CartProductHistoryResponseDto zeroResponse = createCartProductHistoryResponse(historyId, 1L, 100, 0, 0);
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(zeroResponse);

        ResponseEntity<CartProductHistoryResponseDto> response1 =
                cartProductHistoryController.updateCartProductHistory(historyId, zeroRequest);
        assertEquals(0, response1.getBody().getOldQuantity());
        assertEquals(0, response1.getBody().getNewQuantity());

        // Test with large quantities
        CartProductHistoryRequestDto largeRequest = new CartProductHistoryRequestDto();
        largeRequest.setCartProductId(1L);
        largeRequest.setProductId(100);
        largeRequest.setOldQuantity(999);
        largeRequest.setNewQuantity(1000);

        CartProductHistoryResponseDto largeResponse = createCartProductHistoryResponse(historyId, 1L, 100, 999, 1000);
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(largeResponse);

        ResponseEntity<CartProductHistoryResponseDto> response2 =
                cartProductHistoryController.updateCartProductHistory(historyId, largeRequest);
        assertEquals(999, response2.getBody().getOldQuantity());
        assertEquals(1000, response2.getBody().getNewQuantity());

        verify(cartProductHistoryService, times(2)).updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class));
    }

    // ========== DELETE CART PRODUCT HISTORY TESTS ==========

    @Test
    @DisplayName("Should delete cart product history successfully")
    void testDeleteCartProductHistory() {
        // Given
        Long historyId = 1L;
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When
        ResponseEntity<Void> response = cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    @Test
    @DisplayName("Should delete non-existent cart product history")
    void testDeleteCartProductHistory_NonExistent() {
        // Given
        Long historyId = 999L;
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When
        ResponseEntity<Void> response = cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    @Test
    @DisplayName("Should handle null history ID in delete")
    void testDeleteCartProductHistory_NullId() {
        // Given
        Long historyId = null;
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When
        ResponseEntity<Void> response = cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    @Test
    @DisplayName("Should handle zero history ID in delete")
    void testDeleteCartProductHistory_ZeroId() {
        // Given
        Long historyId = 0L;
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When
        ResponseEntity<Void> response = cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    @Test
    @DisplayName("Should handle negative history ID in delete")
    void testDeleteCartProductHistory_NegativeId() {
        // Given
        Long historyId = -1L;
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When
        ResponseEntity<Void> response = cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then
        assertEquals(204, response.getStatusCodeValue());
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    // ========== INTEGRATION AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle multiple consecutive calls")
    void testMultipleConsecutiveCalls() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(mockResponse);

        // When - Multiple calls
        ResponseEntity<CartProductHistoryResponseDto> response1 =
                cartProductHistoryController.getCartProductHistoryById(historyId);
        ResponseEntity<CartProductHistoryResponseDto> response2 =
                cartProductHistoryController.getCartProductHistoryById(historyId);

        // Then
        assertEquals(200, response1.getStatusCodeValue());
        assertEquals(200, response2.getStatusCodeValue());
        assertEquals(1L, response1.getBody().getCartProductHistoryId());
        assertEquals(1L, response2.getBody().getCartProductHistoryId());
        verify(cartProductHistoryService, times(2)).getCartProductHistoryById(historyId);
    }

    @Test
    @DisplayName("Should handle different operations on same history ID")
    void testDifferentOperationsOnSameHistoryId() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto updateRequest = new CartProductHistoryRequestDto();
        updateRequest.setCartProductId(1L);
        updateRequest.setProductId(100);
        updateRequest.setOldQuantity(10);
        updateRequest.setNewQuantity(15);

        when(cartProductHistoryService.getCartProductHistoryById(historyId)).thenReturn(mockResponse);
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(createCartProductHistoryResponse(historyId, 1L, 100, 10, 15));
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When - Different operations
        ResponseEntity<CartProductHistoryResponseDto> getResponse =
                cartProductHistoryController.getCartProductHistoryById(historyId);
        ResponseEntity<CartProductHistoryResponseDto> updateResponse =
                cartProductHistoryController.updateCartProductHistory(historyId, updateRequest);
        ResponseEntity<Void> deleteResponse = cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals(200, updateResponse.getStatusCodeValue());
        assertEquals(204, deleteResponse.getStatusCodeValue());
        assertEquals(5, getResponse.getBody().getOldQuantity());
        assertEquals(10, updateResponse.getBody().getOldQuantity());

        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
        verify(cartProductHistoryService, times(1)).updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class));
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    @Test
    @DisplayName("Should handle large history ID values")
    void testLargeHistoryIdValues() {
        // Given
        Long largeHistoryId = Long.MAX_VALUE;
        CartProductHistoryResponseDto largeIdResponse = createCartProductHistoryResponse(largeHistoryId, 1L, 100, 5, 10);
        when(cartProductHistoryService.getCartProductHistoryById(largeHistoryId)).thenReturn(largeIdResponse);

        // When
        ResponseEntity<CartProductHistoryResponseDto> response =
                cartProductHistoryController.getCartProductHistoryById(largeHistoryId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(Long.MAX_VALUE, response.getBody().getCartProductHistoryId());
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(largeHistoryId);
    }

    @Test
    @DisplayName("Should handle full CRUD workflow")
    void testFullCRUDWorkflow() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto createRequest = mockRequest;
        CartProductHistoryRequestDto updateRequest = new CartProductHistoryRequestDto();
        updateRequest.setCartProductId(1L);
        updateRequest.setProductId(100);
        updateRequest.setOldQuantity(10);
        updateRequest.setNewQuantity(15);

        // Mock all operations
        when(cartProductHistoryService.createCartProductHistory(any(CartProductHistoryRequestDto.class)))
                .thenReturn(mockResponse);
        when(cartProductHistoryService.getAllCartProductHistory())
                .thenReturn(Arrays.asList(mockResponse));
        when(cartProductHistoryService.getCartProductHistoryById(historyId))
                .thenReturn(mockResponse);
        when(cartProductHistoryService.updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class)))
                .thenReturn(createCartProductHistoryResponse(historyId, 1L, 100, 10, 15));
        doNothing().when(cartProductHistoryService).deleteCartProductHistory(historyId);

        // When - Execute full CRUD workflow
        ResponseEntity<CartProductHistoryResponseDto> createResponse =
                cartProductHistoryController.createCartProductHistory(createRequest);
        ResponseEntity<List<CartProductHistoryResponseDto>> getAllResponse =
                cartProductHistoryController.getAllCartProductHistory();
        ResponseEntity<CartProductHistoryResponseDto> getResponse =
                cartProductHistoryController.getCartProductHistoryById(historyId);
        ResponseEntity<CartProductHistoryResponseDto> updateResponse =
                cartProductHistoryController.updateCartProductHistory(historyId, updateRequest);
        ResponseEntity<Void> deleteResponse =
                cartProductHistoryController.deleteCartProductHistory(historyId);

        // Then - Verify all operations
        assertEquals(201, createResponse.getStatusCodeValue());
        assertEquals(200, getAllResponse.getStatusCodeValue());
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals(200, updateResponse.getStatusCodeValue());
        assertEquals(204, deleteResponse.getStatusCodeValue());

        assertNotNull(createResponse.getBody());
        assertNotNull(getAllResponse.getBody());
        assertNotNull(getResponse.getBody());
        assertNotNull(updateResponse.getBody());
        assertNull(deleteResponse.getBody());

        // Verify service calls
        verify(cartProductHistoryService, times(1)).createCartProductHistory(any(CartProductHistoryRequestDto.class));
        verify(cartProductHistoryService, times(1)).getAllCartProductHistory();
        verify(cartProductHistoryService, times(1)).getCartProductHistoryById(historyId);
        verify(cartProductHistoryService, times(1)).updateCartProductHistory(eq(historyId), any(CartProductHistoryRequestDto.class));
        verify(cartProductHistoryService, times(1)).deleteCartProductHistory(historyId);
    }

    // ========== HELPER METHODS ==========

    private CartProductHistoryResponseDto createCartProductHistoryResponse(Long historyId, Long cartProductId,
                                                                           Integer productId, Integer oldQuantity, Integer newQuantity) {
        CartProductHistoryResponseDto response = new CartProductHistoryResponseDto();
        response.setCartProductHistoryId(historyId);
        response.setCartProductId(cartProductId);
        response.setProductId(productId);
        response.setOldQuantity(oldQuantity);
        response.setNewQuantity(newQuantity);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}