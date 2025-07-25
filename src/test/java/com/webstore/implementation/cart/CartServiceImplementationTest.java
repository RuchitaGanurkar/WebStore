package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartStatus;
import com.webstore.entity.product.Catalogue;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.cart.CartStatusNotFoundException;
import com.webstore.exception.product.CatalogueNotFoundException;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.repository.product.CatalogueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplementationTest {

    @InjectMocks
    private CartServiceImplementation cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartStatusRepository cartStatusRepository;

    @Mock
    private CatalogueRepository catalogueRepository;

    private CartStatus activeStatus;
    private CartStatus archivedStatus;
    private Catalogue catalogue;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        activeStatus = new CartStatus();
        activeStatus.setStatusId(1);
        activeStatus.setStatusName(CartStatusType.ACTIVE);

        archivedStatus = new CartStatus();
        archivedStatus.setStatusId(4);
        archivedStatus.setStatusName(CartStatusType.ARCHIVED);

        catalogue = new Catalogue();
        catalogue.setCatalogueId(1);
        // Set other catalogue properties if needed

        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setPhoneNumber("9876543210");
        testCart.setCatalogue(catalogue);
        testCart.setStatus(activeStatus);
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createCart_shouldReturnCartResponseDto() {
        CartRequestDto requestDto = new CartRequestDto();
        requestDto.setPhoneNumber(9876543210L);
        requestDto.setCatalogueId(1); // Changed: Integer instead of Catalogue object
        requestDto.setStatusId(1);

        when(cartStatusRepository.findById(1)).thenReturn(Optional.of(activeStatus));
        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponseDto responseDto = cartService.createCart(requestDto);

        assertNotNull(responseDto);
        assertEquals(testCart.getCartId(), responseDto.getCartId());
        assertEquals(Long.valueOf(testCart.getPhoneNumber()), responseDto.getPhoneNumber());
        assertEquals(catalogue.getCatalogueId(), responseDto.getCatalogueId());
        assertEquals(activeStatus.getStatusId(), responseDto.getStatusId());

        verify(cartRepository).save(any(Cart.class));
        verify(catalogueRepository).findById(1);
        verify(cartStatusRepository).findById(1);
    }

    @Test
    void createCart_shouldThrowIfCatalogueNotFound() {
        CartRequestDto requestDto = new CartRequestDto();
        requestDto.setPhoneNumber(9876543210L);
        requestDto.setCatalogueId(999); // Changed: Integer instead of Catalogue object
        requestDto.setStatusId(1);

        when(cartStatusRepository.findById(1)).thenReturn(Optional.of(activeStatus));
        when(catalogueRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CatalogueNotFoundException.class, () -> cartService.createCart(requestDto));
    }

    @Test
    void createCart_shouldThrowIfCatalogueIdIsNull() {
        CartRequestDto requestDto = new CartRequestDto();
        requestDto.setPhoneNumber(9876543210L);
        requestDto.setCatalogueId(null);
        requestDto.setStatusId(1);

        // Mock the status repository call since it's checked first
        when(cartStatusRepository.findById(1)).thenReturn(Optional.of(activeStatus));

        assertThrows(CatalogueNotFoundException.class, () -> cartService.createCart(requestDto));
    }

    @Test
    void getActiveCartByPhoneNumber_shouldReturnCart() {
        when(cartRepository.findActiveCartByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(testCart));

        CartResponseDto dto = cartService.getActiveCartByPhoneNumber(9876543210L);

        assertNotNull(dto);
        assertEquals(1L, dto.getCartId());
        assertEquals(9876543210L, dto.getPhoneNumber());
        assertEquals(1, dto.getCatalogueId());
        assertEquals(1, dto.getStatusId());
    }

    @Test
    void getActiveCartByPhoneNumber_shouldThrowIfNotFound() {
        when(cartRepository.findActiveCartByPhoneNumber("9876543210")).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.getActiveCartByPhoneNumber(9876543210L));
    }

    @Test
    void getCartById_shouldReturnCart() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

        CartResponseDto dto = cartService.getCartById(1L);

        assertEquals(1L, dto.getCartId());
        assertEquals(9876543210L, dto.getPhoneNumber());
        assertEquals(1, dto.getCatalogueId());
        assertEquals(1, dto.getStatusId());
    }

    @Test
    void getCartById_shouldThrowIfNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.getCartById(1L));
    }

    @Test
    void getCartsByStatus_shouldReturnList() {
        when(cartRepository.findByStatusStatusName(CartStatusType.ACTIVE)).thenReturn(List.of(testCart));

        List<CartResponseDto> result = cartService.getCartsByStatus("active");

        assertEquals(1, result.size());
        assertEquals(testCart.getCartId(), result.get(0).getCartId());
        assertEquals(1, result.get(0).getCatalogueId());
    }

    @Test
    void getCartsByStatus_shouldThrowOnInvalidStatus() {
        assertThrows(CartStatusNotFoundException.class, () -> cartService.getCartsByStatus("invalid_status"));
    }

    @Test
    void updateCartStatus_shouldUpdateAndReturnCart() {
        CartStatus newStatus = new CartStatus();
        newStatus.setStatusId(2);
        newStatus.setStatusName(CartStatusType.CHECKED_OUT);

        Cart updatedCart = new Cart();
        updatedCart.setCartId(1L);
        updatedCart.setPhoneNumber("9876543210");
        updatedCart.setCatalogue(catalogue);
        updatedCart.setStatus(newStatus);
        updatedCart.setCreatedAt(testCart.getCreatedAt());
        updatedCart.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartStatusRepository.findById(2)).thenReturn(Optional.of(newStatus));
        when(cartRepository.save(any(Cart.class))).thenReturn(updatedCart);

        CartResponseDto response = cartService.updateCartStatus(1L, 2);

        assertEquals(1L, response.getCartId());
        assertEquals(2, response.getStatusId());
        assertEquals(1, response.getCatalogueId());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateCartStatus_shouldThrowIfCartNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.updateCartStatus(1L, 2));
    }

    @Test
    void updateCartStatus_shouldThrowIfStatusNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartStatusRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CartStatusNotFoundException.class, () -> cartService.updateCartStatus(1L, 999));
    }

    @Test
    void archiveCart_shouldUpdateStatusToArchived() {
        Cart archivedCart = new Cart();
        archivedCart.setCartId(1L);
        archivedCart.setPhoneNumber("9876543210");
        archivedCart.setCatalogue(catalogue);
        archivedCart.setStatus(archivedStatus);
        archivedCart.setCreatedAt(testCart.getCreatedAt());
        archivedCart.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartStatusRepository.findByStatusName(CartStatusType.ARCHIVED)).thenReturn(Optional.of(archivedStatus));
        when(cartRepository.save(any(Cart.class))).thenReturn(archivedCart);

        String result = cartService.archiveCart(1L);

        assertTrue(result.contains("Cart archived successfully, cart_id: 1"));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void archiveCart_shouldThrowIfCartNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.archiveCart(1L));
    }

    @Test
    void archiveCart_shouldThrowIfArchivedStatusNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartStatusRepository.findByStatusName(CartStatusType.ARCHIVED)).thenReturn(Optional.empty());

        assertThrows(CartStatusNotFoundException.class, () -> cartService.archiveCart(1L));
    }

    @Test
    void getCartsByCatalogueId_shouldReturnList() {
        when(cartRepository.findByCatalogueCatalogueId(1)).thenReturn(List.of(testCart));

        List<CartResponseDto> result = cartService.getCartsByCatalogueId(1);

        assertEquals(1, result.size());
        assertEquals(testCart.getCartId(), result.get(0).getCartId());
        assertEquals(1, result.get(0).getCatalogueId());
    }

    @Test
    void getCartsByCatalogueId_shouldReturnEmptyListIfNoneFound() {
        when(cartRepository.findByCatalogueCatalogueId(999)).thenReturn(Collections.emptyList());

        List<CartResponseDto> result = cartService.getCartsByCatalogueId(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void createCart_shouldThrowIfStatusNotFound() {
        CartRequestDto requestDto = new CartRequestDto();
        requestDto.setPhoneNumber(9876543210L);
        requestDto.setCatalogueId(1); // Changed: Integer instead of Catalogue object
        requestDto.setStatusId(999);

        when(catalogueRepository.findById(1)).thenReturn(Optional.of(catalogue));
        when(cartStatusRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CartStatusNotFoundException.class, () -> cartService.createCart(requestDto));
    }
}