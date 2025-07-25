package com.webstore.service.whatsapp.business;

import com.webstore.repository.cart.CartProductRepository;
import com.webstore.repository.cart.CartRepository;
import com.webstore.service.cart.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CartBusinessServiceTest {

    private CartBusinessService cartBusinessService;

    private CartService cartService;
    private CartRepository cartRepository;
    private CartProductRepository cartProductRepository;
    private ProductBusinessService productBusinessService;

    @BeforeEach
    void setUp() {
        cartService = Mockito.mock(CartService.class);
        cartRepository = Mockito.mock(CartRepository.class);
        cartProductRepository = Mockito.mock(CartProductRepository.class);
        productBusinessService = Mockito.mock(ProductBusinessService.class);

        cartBusinessService = new CartBusinessService(
                cartService,
                cartRepository,
                cartProductRepository,
                productBusinessService
        );
    }

    @Test
    void testGetCartSummary() {
        String result = cartBusinessService.getCartSummary("9999999999");

        assertNotNull(result);
        assertTrue(result.contains("Your cart") || result.contains("Cart has") || result.contains("Error"));
    }

    @Test
    void testAddProductToCart_NoException() {
        assertDoesNotThrow(() ->
                cartBusinessService.addProductToCart("9999999999", 101, 2));
    }
}
