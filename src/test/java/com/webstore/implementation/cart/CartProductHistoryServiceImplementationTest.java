package com.webstore.implementation.cart;
import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductHistory;
import com.webstore.repository.cart.CartProductHistoryRepository;
import com.webstore.repository.cart.CartProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Product History Service Implementation Tests")
class CartProductHistoryServiceImplementationTest {
    @Mock
    private CartProductHistoryRepository cartProductHistoryRepository;
    @Mock
    private CartProductRepository cartProductRepository;
    @InjectMocks
    private CartProductHistoryServiceImplementation cartProductHistoryService;
    private CartProductHistory testCartProductHistory;
    private CartProductHistoryRequestDto testRequestDto;
    private CartProduct testCartProduct;
    private Cart testCart;
    private LocalDateTime testDateTime;
    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();
        testCart = createTestCart();
        testCartProduct = createTestCartProduct();
        testCartProductHistory = createTestCartProductHistory(1L);
        testRequestDto = createTestRequestDto();
    }
    // ========== CREATE CART PRODUCT HISTORY TESTS ==========
    @Test
    @DisplayName("Should create cart product history successfully")
    void createCartProductHistory_Success() {
        // Given
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class))).thenReturn(testCartProductHistory);
        // When
        CartProductHistoryResponseDto result = cartProductHistoryService.createCartProductHistory(testRequestDto);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCartProductHistoryId()).isEqualTo(1L);
        assertThat(result.getCartProductId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(100);
        assertThat(result.getOldQuantity()).isEqualTo(5);
        assertThat(result.getNewQuantity()).isEqualTo(10);
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    @Test
    @DisplayName("Should throw exception when cart product not found")
    void createCartProductHistory_CartProductNotFound() {
        // Given
        when(cartProductRepository.findById(1L)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product not found with ID: 1");
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when request is null")
    void createCartProductHistory_NullRequest() {
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CartProductHistoryRequestDto cannot be null");
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when cart product ID is null")
    void createCartProductHistory_NullCartProductId() {
        // Given
        testRequestDto.setCartProductId(null);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cart product ID cannot be null");
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when product ID is null")
    void createCartProductHistory_NullProductId() {
        // Given
        testRequestDto.setProductId(null);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null");
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when old quantity is null")
    void createCartProductHistory_NullOldQuantity() {
        // Given
        testRequestDto.setOldQuantity(null);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Old quantity cannot be null");
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when new quantity is null")
    void createCartProductHistory_NullNewQuantity() {
        // Given
        testRequestDto.setNewQuantity(null);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New quantity cannot be null");
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when quantities are negative")
    void createCartProductHistory_NegativeQuantities() {
        // Given
        testRequestDto.setOldQuantity(-1);
        testRequestDto.setNewQuantity(-5);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantities cannot be negative");
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should handle database exception during create")
    void createCartProductHistory_DatabaseError() {
        // Given
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class)))
                .thenThrow(new DataAccessException("DB Error") {});
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.createCartProductHistory(testRequestDto))
                .isInstanceOf(DataAccessException.class);
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    // ========== GET ALL CART PRODUCT HISTORY TESTS ==========
    @Test
    @DisplayName("Should get all cart product history successfully")
    void getAllCartProductHistory_Success() {
        // Given
        List<CartProductHistory> histories = Arrays.asList(
                createTestCartProductHistory(1L),
                createTestCartProductHistory(2L)
        );
        when(cartProductHistoryRepository.findAll()).thenReturn(histories);
        // When
        List<CartProductHistoryResponseDto> result = cartProductHistoryService.getAllCartProductHistory();
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCartProductHistoryId()).isEqualTo(1L);
        assertThat(result.get(1).getCartProductHistoryId()).isEqualTo(2L);
        assertThat(result.get(0).getProductId()).isEqualTo(100);
        assertThat(result.get(1).getProductId()).isEqualTo(100);
        verify(cartProductHistoryRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Should return empty list when no cart product history exists")
    void getAllCartProductHistory_EmptyList() {
        // Given
        when(cartProductHistoryRepository.findAll()).thenReturn(Collections.emptyList());
        // When
        List<CartProductHistoryResponseDto> result = cartProductHistoryService.getAllCartProductHistory();
        // Then
        assertThat(result).isEmpty();
        verify(cartProductHistoryRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Should handle database exception when getting all")
    void getAllCartProductHistory_DatabaseError() {
        // Given
        when(cartProductHistoryRepository.findAll()).thenThrow(new DataAccessException("DB Error") {});
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.getAllCartProductHistory())
                .isInstanceOf(DataAccessException.class);
        verify(cartProductHistoryRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Should handle null entities in list")
    void getAllCartProductHistory_WithNullEntities() {
        // Given
        List<CartProductHistory> histories = Arrays.asList(
                createTestCartProductHistory(1L),
                null,
                createTestCartProductHistory(2L)
        );
        when(cartProductHistoryRepository.findAll()).thenReturn(histories);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.getAllCartProductHistory())
                .isInstanceOf(NullPointerException.class);
        verify(cartProductHistoryRepository, times(1)).findAll();
    }
    // ========== GET CART PRODUCT HISTORY BY ID TESTS ==========
    @Test
    @DisplayName("Should get cart product history by ID successfully")
    void getCartProductHistoryById_Success() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        // When
        CartProductHistoryResponseDto result = cartProductHistoryService.getCartProductHistoryById(historyId);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCartProductHistoryId()).isEqualTo(historyId);
        assertThat(result.getCartProductId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(100);
        assertThat(result.getOldQuantity()).isEqualTo(5);
        assertThat(result.getNewQuantity()).isEqualTo(10);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
    }
    @Test
    @DisplayName("Should throw exception when cart product history not found")
    void getCartProductHistoryById_NotFound() {
        // Given
        Long historyId = 999L;
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.getCartProductHistoryById(historyId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product history not found with ID: " + historyId);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
    }
    @Test
    @DisplayName("Should handle null history ID")
    void getCartProductHistoryById_NullId() {
        // Given
        Long historyId = null;
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.getCartProductHistoryById(historyId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("History ID cannot be null");
        verify(cartProductHistoryRepository, never()).findById(any());
    }
    @Test
    @DisplayName("Should handle database exception when getting by ID")
    void getCartProductHistoryById_DatabaseError() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryRepository.findById(historyId)).thenThrow(new DataAccessException("DB Error") {});
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.getCartProductHistoryById(historyId))
                .isInstanceOf(DataAccessException.class);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
    }
    // ========== UPDATE CART PRODUCT HISTORY TESTS ==========
    @Test
    @DisplayName("Should update cart product history successfully")
    void updateCartProductHistory_Success() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto updateDto = createTestUpdateRequestDto();
        CartProductHistory updatedHistory = createTestCartProductHistory(historyId);
        updatedHistory.setOldQuantity(10);
        updatedHistory.setNewQuantity(15);
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class))).thenReturn(updatedHistory);
        // When
        CartProductHistoryResponseDto result = cartProductHistoryService.updateCartProductHistory(historyId, updateDto);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCartProductHistoryId()).isEqualTo(historyId);
        assertThat(result.getOldQuantity()).isEqualTo(10);
        assertThat(result.getNewQuantity()).isEqualTo(15);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    @Test
    @DisplayName("Should throw exception when updating non-existent history")
    void updateCartProductHistory_NotFound() {
        // Given
        Long historyId = 999L;
        CartProductHistoryRequestDto updateDto = createTestUpdateRequestDto();
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.updateCartProductHistory(historyId, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product history not found with ID: " + historyId);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw exception when cart product not found during update")
    void updateCartProductHistory_CartProductNotFound() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto updateDto = createTestUpdateRequestDto();
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        when(cartProductRepository.findById(1L)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.updateCartProductHistory(historyId, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product not found with ID: 1");
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should handle null request DTO in update")
    void updateCartProductHistory_NullRequest() {
        // Given
        Long historyId = 1L;
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.updateCartProductHistory(historyId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CartProductHistoryRequestDto cannot be null");
        verify(cartProductHistoryRepository, never()).findById(any());
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should handle null history ID in update")
    void updateCartProductHistory_NullHistoryId() {
        // Given
        CartProductHistoryRequestDto updateDto = createTestUpdateRequestDto();
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.updateCartProductHistory(null, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("History ID cannot be null");
        verify(cartProductHistoryRepository, never()).findById(any());
        verify(cartProductRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should handle data integrity violation during update")
    void updateCartProductHistory_DataIntegrityViolation() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto updateDto = createTestUpdateRequestDto();
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.updateCartProductHistory(historyId, updateDto))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    @Test
    @DisplayName("Should handle optimistic locking failure during update")
    void updateCartProductHistory_OptimisticLockingFailure() {
        // Given
        Long historyId = 1L;
        CartProductHistoryRequestDto updateDto = createTestUpdateRequestDto();
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class)))
                .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.updateCartProductHistory(historyId, updateDto))
                .isInstanceOf(OptimisticLockingFailureException.class);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    // ========== DELETE CART PRODUCT HISTORY TESTS ==========
    @Test
    @DisplayName("Should delete cart product history successfully")
    void deleteCartProductHistory_Success() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        doNothing().when(cartProductHistoryRepository).delete(testCartProductHistory);
        // When
        cartProductHistoryService.deleteCartProductHistory(historyId);
        // Then
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductHistoryRepository, times(1)).delete(testCartProductHistory);
    }
    @Test
    @DisplayName("Should throw exception when deleting non-existent history")
    void deleteCartProductHistory_NotFound() {
        // Given
        Long historyId = 999L;
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.deleteCartProductHistory(historyId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart product history not found with ID: " + historyId);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductHistoryRepository, never()).delete(any());
    }
    @Test
    @DisplayName("Should handle null history ID in delete")
    void deleteCartProductHistory_NullId() {
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.deleteCartProductHistory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("History ID cannot be null");
        verify(cartProductHistoryRepository, never()).findById(any());
        verify(cartProductHistoryRepository, never()).delete(any());
    }
    @Test
    @DisplayName("Should handle database exception during delete")
    void deleteCartProductHistory_DatabaseError() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        doThrow(new DataAccessException("DB Error") {}).when(cartProductHistoryRepository).delete(testCartProductHistory);
        // When & Then
        assertThatThrownBy(() -> cartProductHistoryService.deleteCartProductHistory(historyId))
                .isInstanceOf(DataAccessException.class);
        verify(cartProductHistoryRepository, times(1)).findById(historyId);
        verify(cartProductHistoryRepository, times(1)).delete(testCartProductHistory);
    }
    // ========== EDGE CASES AND BOUNDARY TESTS ==========
    @Test
    @DisplayName("Should handle zero quantities")
    void createCartProductHistory_ZeroQuantities() {
        // Given
        testRequestDto.setOldQuantity(0);
        testRequestDto.setNewQuantity(0);
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class))).thenReturn(testCartProductHistory);
        // When
        CartProductHistoryResponseDto result = cartProductHistoryService.createCartProductHistory(testRequestDto);
        // Then
        assertThat(result).isNotNull();
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    @Test
    @DisplayName("Should handle large quantity values")
    void createCartProductHistory_LargeQuantities() {
        // Given
        testRequestDto.setOldQuantity(Integer.MAX_VALUE);
        testRequestDto.setNewQuantity(Integer.MAX_VALUE - 1);
        when(cartProductRepository.findById(1L)).thenReturn(Optional.of(testCartProduct));
        when(cartProductHistoryRepository.save(any(CartProductHistory.class))).thenReturn(testCartProductHistory);
        // When
        CartProductHistoryResponseDto result = cartProductHistoryService.createCartProductHistory(testRequestDto);
        // Then
        assertThat(result).isNotNull();
        verify(cartProductRepository, times(1)).findById(1L);
        verify(cartProductHistoryRepository, times(1)).save(any(CartProductHistory.class));
    }
    @Test
    @DisplayName("Should handle multiple consecutive operations")
    void testMultipleConsecutiveOperations() {
        // Given
        Long historyId = 1L;
        when(cartProductHistoryRepository.findById(historyId)).thenReturn(Optional.of(testCartProductHistory));
        // When - Multiple calls
        CartProductHistoryResponseDto result1 = cartProductHistoryService.getCartProductHistoryById(historyId);
        CartProductHistoryResponseDto result2 = cartProductHistoryService.getCartProductHistoryById(historyId);
        // Then
        assertThat(result1.getCartProductHistoryId()).isEqualTo(historyId);
        assertThat(result2.getCartProductHistoryId()).isEqualTo(historyId);
        verify(cartProductHistoryRepository, times(2)).findById(historyId);
    }
    // ========== HELPER METHODS FOR TEST DATA ==========
    private Cart createTestCart() {
        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("+1234567890");
        cart.setCreatedAt(testDateTime);
        cart.setUpdatedAt(testDateTime);
        return cart;
    }
    private CartProduct createTestCartProduct() {
        CartProduct cartProduct = new CartProduct();
        cartProduct.setCartProductId(1L);
        cartProduct.setCart(testCart);
        cartProduct.setCreatedAt(testDateTime);
        cartProduct.setUpdatedAt(testDateTime);
        return cartProduct;
    }
    private CartProductHistory createTestCartProductHistory(Long historyId) {
        CartProductHistory history = new CartProductHistory();
        history.setCartProductHistoryId(historyId);
        history.setCartProduct(testCartProduct);
        history.setProductId(100);
        history.setOldQuantity(5);
        history.setNewQuantity(10);
        history.setCreatedAt(testDateTime);
        history.setUpdatedAt(testDateTime);
        return history;
    }
    private CartProductHistoryRequestDto createTestRequestDto() {
        CartProductHistoryRequestDto dto = new CartProductHistoryRequestDto();
        dto.setCartProductId(1L);
        dto.setProductId(100);
        dto.setOldQuantity(5);
        dto.setNewQuantity(10);
        return dto;
    }
    private CartProductHistoryRequestDto createTestUpdateRequestDto() {
        CartProductHistoryRequestDto dto = new CartProductHistoryRequestDto();
        dto.setCartProductId(1L);
        dto.setProductId(100);
        dto.setOldQuantity(10);
        dto.setNewQuantity(15);
        return dto;
    }
}


