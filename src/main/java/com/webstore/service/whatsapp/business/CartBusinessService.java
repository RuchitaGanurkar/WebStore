package com.webstore.service.whatsapp.business;

import org.springframework.stereotype.Service;

@Service
public class CartBusinessService {

    public void addProductToCart(String userPhone, Integer productId, Integer quantity) {

    }

    public String getCartSummary(String userPhone) {
        return "🛒 Your cart feature is coming soon! For now, type 'categories' to continue shopping.";
    }
}
