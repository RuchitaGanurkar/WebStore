package com.webstore.service.whatsapp.flow;

import com.webstore.service.whatsapp.business.CartBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import org.springframework.stereotype.Service;

@Service
public class CartFlowService {

    private final CartBusinessService cartService;
    private final WhatsAppMessageSender messageSender;

    public CartFlowService(CartBusinessService cartService, WhatsAppMessageSender messageSender) {
        this.cartService = cartService;
        this.messageSender = messageSender;
    }

    public void addToCart(String phoneNumberId, String from, Integer productId) {
        cartService.addProductToCart(from, productId, 1);
    }

    public void viewCart(String phoneNumberId, String from) {
        String cartSummary = cartService.getCartSummary(from);
        messageSender.sendTextMessage(phoneNumberId, from, cartSummary);
    }
}