package com.webstore.service.whatsapp.business;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartBusinessServiceTest {

    private final CartBusinessService cartBusinessService = new CartBusinessService();

    @Test
    void testGetCartSummary() {
        String result = cartBusinessService.getCartSummary("9999999999");

        assertNotNull(result);
        assertTrue(result.contains("Your cart feature is coming soon"));
    }

    @Test
    void testAddProductToCart_NoException() {
        assertDoesNotThrow(() ->
                cartBusinessService.addProductToCart("9999999999", 101, 2));
    }
}
