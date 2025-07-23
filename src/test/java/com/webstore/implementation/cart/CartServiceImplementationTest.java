package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.dto.response.cart.CartResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartStatus;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.cart.CartStatusNotFoundException;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.cart.CartStatusRepository;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCart_shouldReturnCartResponseDto() {
        CartRequestDto requestDto = new CartRequestDto();
        requestDto.setPhoneNumber(9876543210L);
        requestDto.setCatalogueCategoryId(1);
        requestDto.setStatusId(100);

        CartStatus status = new CartStatus();
        status.setStatusId(100);
        status.setStatusName(CartStatusType.ACTIVE);

        when(cartStatusRepository.findById(100)).thenReturn(Optional.of(status));

        Cart savedCart = new Cart();
        savedCart.setCartId(1L);
        savedCart.setPhoneNumber("9876543210");
        savedCart.setCatalogueCategoryId(1);
        savedCart.setStatus(status);
        savedCart.setCreatedAt(LocalDateTime.now());
        savedCart.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        CartResponseDto responseDto = cartService.createCart(requestDto);

        assertNotNull(responseDto);
        assertEquals(savedCart.getCartId(), responseDto.getCartId());
        assertEquals(Long.valueOf(savedCart.getPhoneNumber()), responseDto.getPhoneNumber());
    }

    @Test
    void getActiveCartByPhoneNumber_shouldReturnCart() {
        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("9876543210");
        cart.setCatalogueCategoryId(1);
        cart.setStatus(new CartStatus(1, CartStatusType.ACTIVE));
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findActiveCartByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(cart));

        CartResponseDto dto = cartService.getActiveCartByPhoneNumber(9876543210L);

        assertNotNull(dto);
        assertEquals(1L, dto.getCartId());
    }

    @Test
    void getActiveCartByPhoneNumber_shouldThrowIfNotFound() {
        when(cartRepository.findActiveCartByPhoneNumber("9876543210")).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.getActiveCartByPhoneNumber(9876543210L));
    }

    @Test
    void getCartById_shouldReturnCart() {
        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("9876543210");
        cart.setCatalogueCategoryId(1);
        cart.setStatus(new CartStatus(1, CartStatusType.ACTIVE));
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartResponseDto dto = cartService.getCartById(1L);

        assertEquals(1L, dto.getCartId());
    }

    @Test
    void getCartById_shouldThrowIfNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.getCartById(1L));
    }

    @Test
    void getCartsByStatus_shouldReturnList() {
        CartStatusType type = CartStatusType.ACTIVE;

        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("9876543210");
        cart.setCatalogueCategoryId(1);
        cart.setStatus(new CartStatus(1, type));
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        when(cartRepository.findByStatusStatusName(type)).thenReturn(List.of(cart));

        List<CartResponseDto> result = cartService.getCartsByStatus("active");

        assertEquals(1, result.size());
        assertEquals(cart.getCartId(), result.get(0).getCartId());
    }

    @Test
    void getCartsByStatus_shouldThrowOnInvalidStatus() {
        assertThrows(CartStatusNotFoundException.class, () -> cartService.getCartsByStatus("invalid_status"));
    }

    @Test
    void updateCartStatus_shouldUpdateAndReturnCart() {
        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("9876543210");
        cart.setCatalogueCategoryId(1);
        cart.setStatus(new CartStatus(1, CartStatusType.ACTIVE));
        cart.setCreatedAt(LocalDateTime.now());

        CartStatus newStatus = new CartStatus(2, CartStatusType.CHECKED_OUT);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartStatusRepository.findById(2)).thenReturn(Optional.of(newStatus));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDto response = cartService.updateCartStatus(1L, 2);

        assertEquals(1L, response.getCartId());
        assertEquals(2, response.getStatusId());
    }

    @Test
    void archiveCart_shouldUpdateStatusToArchived() {
        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("9876543210");
        cart.setCatalogueCategoryId(1);
        cart.setStatus(new CartStatus(1, CartStatusType.ACTIVE));

        CartStatus archived = new CartStatus(2, CartStatusType.ARCHIVED);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartStatusRepository.findByStatusName(CartStatusType.ARCHIVED)).thenReturn(Optional.of(archived));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.archiveCart(1L);

        assertTrue(result.contains("Cart archived successfully"));
    }

    @Test
    void archiveCart_shouldThrowIfCartNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.archiveCart(1L));
    }

    @Test
    void archiveCart_shouldThrowIfArchivedStatusNotFound() {
        Cart cart = new Cart();
        cart.setCartId(1L);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartStatusRepository.findByStatusName(CartStatusType.ARCHIVED)).thenReturn(Optional.empty());

        assertThrows(CartStatusNotFoundException.class, () -> cartService.archiveCart(1L));
    }
}
