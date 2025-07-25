package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.entity.product.Catalogue;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.cart.CartProductNotFoundException;
import com.webstore.exception.cart.CartProductStatusNotFoundException;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.cart.CartProductStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartProductServiceImplementationTest {

    @InjectMocks
    private CartProductServiceImplementation cartProductService;

    @Mock
    private CartProductRepository cartProductRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartProductStatusRepository statusRepository;

    private Cart cart;
    private CartProductStatus status;
    private CartProduct cartProduct;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setCartId(1L);
        cart.setPhoneNumber("9876543210");

        // ✅ Corrected catalogue mock setup
        Catalogue catalogue = new Catalogue();
        catalogue.setCatalogueId(100);
        cart.setCatalogue(catalogue);

        status = new CartProductStatus();
        status.setStatusId(1);
        status.setStatusName(CartProductStatusType.ADDED);

        cartProduct = new CartProduct();
        cartProduct.setCartProductId(10L);
        cartProduct.setCart(cart);
        cartProduct.setStatus(status);
        cartProduct.setCreatedAt(LocalDateTime.now());
        cartProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateCartProduct_Success() {
        CartProductRequestDto requestDto = new CartProductRequestDto();
        requestDto.setCartId(1L);
        requestDto.setStatusId(1);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(statusRepository.findById(1)).thenReturn(Optional.of(status));
        when(cartProductRepository.save(any())).thenReturn(cartProduct);

        CartProductResponseDto response = cartProductService.createCartProduct(requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getCartProductId()).isEqualTo(10L);
        verify(cartProductRepository, times(1)).save(any());
    }

    @Test
    void testCreateCartProduct_CartNotFound() {
        CartProductRequestDto requestDto = new CartProductRequestDto();
        requestDto.setCartId(2L);
        requestDto.setStatusId(1);

        when(cartRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartProductService.createCartProduct(requestDto))
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    void testCreateCartProduct_StatusNotFound() {
        CartProductRequestDto requestDto = new CartProductRequestDto();
        requestDto.setCartId(1L);
        requestDto.setStatusId(99);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(statusRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartProductService.createCartProduct(requestDto))
                .isInstanceOf(CartProductStatusNotFoundException.class);
    }

    @Test
    void testGetCartProductById_Success() {
        when(cartProductRepository.findById(10L)).thenReturn(Optional.of(cartProduct));

        CartProductResponseDto response = cartProductService.getCartProductById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getCartProductId()).isEqualTo(10L);
    }

    @Test
    void testGetCartProductById_NotFound() {
        when(cartProductRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartProductService.getCartProductById(999L))
                .isInstanceOf(CartProductNotFoundException.class);
    }

    @Test
    void testGetCartProductsByCartId() {
        when(statusRepository.findByStatusName("ADDED")).thenReturn(Optional.of(status));
        when(cartProductRepository.findByCartCartIdAndStatus(1L, status)).thenReturn(List.of(cartProduct));

        List<CartProductResponseDto> response = cartProductService.getCartProductsByCartId(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCartProductId()).isEqualTo(10L);
    }

    @Test
    void testDeleteCartProduct_Success() {
        when(cartProductRepository.findById(10L)).thenReturn(Optional.of(cartProduct));

        cartProductService.deleteCartProduct(10L);

        verify(cartProductRepository, times(1)).delete(cartProduct);
    }

    @Test
    void testDeleteCartProduct_NotFound() {
        when(cartProductRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartProductService.deleteCartProduct(99L))
                .isInstanceOf(CartProductNotFoundException.class);
    }

    @Test
    void testGetActiveCartProductsByPhoneNumber() {
        when(cartProductRepository.findActiveProductsByPhoneNumber("9876543210")).thenReturn(List.of(cartProduct));

        List<CartProductResponseDto> result = cartProductService.getActiveCartProductsByPhoneNumber("9876543210");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCartId()).isEqualTo(cart.getCartId());
    }

    @Test
    void testCountActiveCartProducts() {
        when(cartProductRepository.countActiveProductsInCart(1L)).thenReturn(5L);

        Long count = cartProductService.countActiveCartProducts(1L);

        assertThat(count).isEqualTo(5L);
    }
}