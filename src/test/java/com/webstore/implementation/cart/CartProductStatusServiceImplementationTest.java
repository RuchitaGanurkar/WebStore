package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductStatusRequestDto;
import com.webstore.dto.response.cart.CartProductStatusResponseDto;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.repository.cart.CartProductStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Product Status Service Implementation Tests")
class CartProductStatusServiceImplementationTest {

    @Mock
    private CartProductStatusRepository cartProductStatusRepository;

    @InjectMocks
    private CartProductStatusServiceImplementation cartProductStatusService;

    private CartProductStatus testCartProductStatus;
    private CartProductStatusRequestDto testRequestDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();
        testCartProductStatus = createTestCartProductStatus(1, CartProductStatusType.ADDED);
        testRequestDto = createTestRequestDto("ADDED");
    }

    // ========== GET CART PRODUCT STATUS BY ID TESTS ==========

    @Test
    @DisplayName("Should get cart product status by ID successfully")
    void getCartProductStatusById_Success() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));

        // When
        CartProductStatusResponseDto result = cartProductStatusService.getCartProductStatusById(statusId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusId()).isEqualTo(statusId);
        assertThat(result.getStatusName()).isEqualTo("ADDED");
        assertThat(result.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(result.getUpdatedAt()).isEqualTo(testDateTime);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should throw exception when cart product status not found")
    void getCartProductStatusById_NotFound() {
        // Given
        Integer statusId = 999;
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.getCartProductStatusById(statusId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product status not found with id: " + statusId);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should handle database exception when getting by ID")
    void getCartProductStatusById_DatabaseError() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusRepository.findById(statusId)).thenThrow(new DataAccessException("DB Error") {});

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.getCartProductStatusById(statusId))
                .isInstanceOf(DataAccessException.class);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should handle null status ID")
    void getCartProductStatusById_NullId() {
        // Given
        Integer statusId = null;
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.getCartProductStatusById(statusId))
                .isInstanceOf(RuntimeException.class);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
    }

    // ========== GET ALL CART PRODUCT STATUSES TESTS ==========

    @Test
    @DisplayName("Should get all cart product statuses successfully")
    void getAllCartProductStatuses_Success() {
        // Given
        List<CartProductStatus> statuses = Arrays.asList(
                createTestCartProductStatus(1, CartProductStatusType.ADDED),
                createTestCartProductStatus(2, CartProductStatusType.REMOVED)
        );
        when(cartProductStatusRepository.findAll()).thenReturn(statuses);

        // When
        List<CartProductStatusResponseDto> result = cartProductStatusService.getAllCartProductStatuses();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatusName()).isEqualTo("ADDED");
        assertThat(result.get(1).getStatusName()).isEqualTo("REMOVED");
        assertThat(result.get(0).getStatusId()).isEqualTo(1);
        assertThat(result.get(1).getStatusId()).isEqualTo(2);
        verify(cartProductStatusRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no cart product statuses exist")
    void getAllCartProductStatuses_EmptyList() {
        // Given
        when(cartProductStatusRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CartProductStatusResponseDto> result = cartProductStatusService.getAllCartProductStatuses();

        // Then
        assertThat(result).isEmpty();
        verify(cartProductStatusRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle database exception when getting all")
    void getAllCartProductStatuses_DatabaseError() {
        // Given
        when(cartProductStatusRepository.findAll()).thenThrow(new DataAccessException("DB Error") {});

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.getAllCartProductStatuses())
                .isInstanceOf(DataAccessException.class);
        verify(cartProductStatusRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle null entities in list")
    void getAllCartProductStatuses_WithNullEntities() {
        // Given
        List<CartProductStatus> statuses = Arrays.asList(
                createTestCartProductStatus(1, CartProductStatusType.ADDED),
                null,
                createTestCartProductStatus(2, CartProductStatusType.REMOVED)
        );
        when(cartProductStatusRepository.findAll()).thenReturn(statuses);

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.getAllCartProductStatuses())
                .isInstanceOf(NullPointerException.class);
        verify(cartProductStatusRepository, times(1)).findAll();
    }

    // ========== UPDATE CART PRODUCT STATUS TESTS ==========

    @Test
    @DisplayName("Should update cart product status successfully")
    void updateCartProductStatus_Success() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto updateDto = createTestRequestDto("REMOVED");
        CartProductStatus updatedStatus = createTestCartProductStatus(statusId, CartProductStatusType.REMOVED);

        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));
        when(cartProductStatusRepository.save(any(CartProductStatus.class))).thenReturn(updatedStatus);

        // When
        CartProductStatusResponseDto result = cartProductStatusService.updateCartProductStatus(statusId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusName()).isEqualTo("REMOVED");
        assertThat(result.getStatusId()).isEqualTo(statusId);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, times(1)).save(any(CartProductStatus.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent status")
    void updateCartProductStatus_NotFound() {
        // Given
        Integer statusId = 999;
        CartProductStatusRequestDto updateDto = createTestRequestDto("REMOVED");
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.updateCartProductStatus(statusId, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product status not found with id: " + statusId);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for invalid enum value in update")
    void updateCartProductStatus_InvalidEnumValue() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto updateDto = createTestRequestDto("INVALID_STATUS");
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.updateCartProductStatus(statusId, updateDto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null request DTO in update")
    void updateCartProductStatus_NullRequest() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.updateCartProductStatus(statusId, null))
                .isInstanceOf(NullPointerException.class);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null status name in update")
    void updateCartProductStatus_NullStatusName() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto updateDto = createTestRequestDto(null);
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.updateCartProductStatus(statusId, updateDto))
                .isInstanceOf(NullPointerException.class);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle database exception during update")
    void updateCartProductStatus_DatabaseError() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto updateDto = createTestRequestDto("REMOVED");
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));
        when(cartProductStatusRepository.save(any(CartProductStatus.class)))
                .thenThrow(new DataAccessException("DB Error") {});

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.updateCartProductStatus(statusId, updateDto))
                .isInstanceOf(DataAccessException.class);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, times(1)).save(any(CartProductStatus.class));
    }

    // ========== DELETE CART PRODUCT STATUS TESTS ==========

    @Test
    @DisplayName("Should delete cart product status successfully")
    void deleteCartProductStatus_Success() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusRepository.existsById(statusId)).thenReturn(true);
        doNothing().when(cartProductStatusRepository).deleteById(statusId);

        // When
        cartProductStatusService.deleteCartProductStatus(statusId);

        // Then
        verify(cartProductStatusRepository, times(1)).existsById(statusId);
        verify(cartProductStatusRepository, times(1)).deleteById(statusId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent status")
    void deleteCartProductStatus_NotFound() {
        // Given
        Integer statusId = 999;
        when(cartProductStatusRepository.existsById(statusId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.deleteCartProductStatus(statusId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product status not found with id: " + statusId);
        verify(cartProductStatusRepository, times(1)).existsById(statusId);
        verify(cartProductStatusRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should handle null status ID in delete")
    void deleteCartProductStatus_NullId() {
        // Given
        Integer statusId = null;
        when(cartProductStatusRepository.existsById(statusId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.deleteCartProductStatus(statusId))
                .isInstanceOf(RuntimeException.class);
        verify(cartProductStatusRepository, times(1)).existsById(statusId);
        verify(cartProductStatusRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should handle database exception during delete")
    void deleteCartProductStatus_DatabaseError() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusRepository.existsById(statusId)).thenReturn(true);
        doThrow(new DataAccessException("DB Error") {}).when(cartProductStatusRepository).deleteById(statusId);

        // When & Then
        assertThatThrownBy(() -> cartProductStatusService.deleteCartProductStatus(statusId))
                .isInstanceOf(DataAccessException.class);
        verify(cartProductStatusRepository, times(1)).existsById(statusId);
        verify(cartProductStatusRepository, times(1)).deleteById(statusId);
    }

    // ========== EDGE CASES AND BOUNDARY TESTS ==========

    @Test
    @DisplayName("Should handle zero status ID")
    void handleZeroStatusId() {
        // Given
        Integer statusId = 0;
        CartProductStatus zeroStatus = createTestCartProductStatus(0, CartProductStatusType.ADDED);
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(zeroStatus));

        // When
        CartProductStatusResponseDto result = cartProductStatusService.getCartProductStatusById(statusId);

        // Then
        assertThat(result.getStatusId()).isEqualTo(0);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should handle negative status ID")
    void handleNegativeStatusId() {
        // Given
        Integer statusId = -1;
        CartProductStatus negativeStatus = createTestCartProductStatus(-1, CartProductStatusType.REMOVED);
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(negativeStatus));

        // When
        CartProductStatusResponseDto result = cartProductStatusService.getCartProductStatusById(statusId);

        // Then
        assertThat(result.getStatusId()).isEqualTo(-1);
        verify(cartProductStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should handle case insensitive enum values")
    void updateCartProductStatus_CaseInsensitive() {
        // Given
        Integer statusId = 1;
        CartProductStatusRequestDto updateDto = createTestRequestDto("added"); // lowercase
        CartProductStatus updatedStatus = createTestCartProductStatus(statusId, CartProductStatusType.ADDED);

        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));
        when(cartProductStatusRepository.save(any(CartProductStatus.class))).thenReturn(updatedStatus);

        // When
        CartProductStatusResponseDto result = cartProductStatusService.updateCartProductStatus(statusId, updateDto);

        // Then
        assertThat(result.getStatusName()).isEqualTo("ADDED");
        verify(cartProductStatusRepository, times(1)).findById(statusId);
        verify(cartProductStatusRepository, times(1)).save(any(CartProductStatus.class));
    }

    @Test
    @DisplayName("Should handle both enum values correctly")
    void validateAllEnumValues() {
        // Test all CartProductStatusType enum values
        for (CartProductStatusType statusType : CartProductStatusType.values()) {
            // Given
            Integer statusId = 1;
            CartProductStatusRequestDto dto = createTestRequestDto(statusType.name());
            CartProductStatus updatedStatus = createTestCartProductStatus(statusId, statusType);

            when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));
            when(cartProductStatusRepository.save(any(CartProductStatus.class))).thenReturn(updatedStatus);

            // When
            CartProductStatusResponseDto result = cartProductStatusService.updateCartProductStatus(statusId, dto);

            // Then
            assertThat(result.getStatusName()).isEqualTo(statusType.name());

            // Reset mocks for next iteration
            reset(cartProductStatusRepository);
        }
    }

    @Test
    @DisplayName("Should handle multiple consecutive operations")
    void testMultipleConsecutiveOperations() {
        // Given
        Integer statusId = 1;
        when(cartProductStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartProductStatus));

        // When - Multiple calls
        CartProductStatusResponseDto result1 = cartProductStatusService.getCartProductStatusById(statusId);
        CartProductStatusResponseDto result2 = cartProductStatusService.getCartProductStatusById(statusId);

        // Then
        assertThat(result1.getStatusName()).isEqualTo("ADDED");
        assertThat(result2.getStatusName()).isEqualTo("ADDED");
        verify(cartProductStatusRepository, times(2)).findById(statusId);
    }

    // ========== HELPER METHODS FOR TEST DATA ==========

    private CartProductStatus createTestCartProductStatus(Integer statusId, CartProductStatusType statusType) {
        CartProductStatus cartProductStatus = new CartProductStatus();
        cartProductStatus.setStatusId(statusId);
        cartProductStatus.setStatusName(statusType);
        cartProductStatus.setCreatedAt(testDateTime);
        cartProductStatus.setUpdatedAt(testDateTime);
        return cartProductStatus;
    }

    private CartProductStatusRequestDto createTestRequestDto(String statusName) {
        CartProductStatusRequestDto dto = new CartProductStatusRequestDto();
        dto.setStatusName(statusName);
        return dto;
    }
}