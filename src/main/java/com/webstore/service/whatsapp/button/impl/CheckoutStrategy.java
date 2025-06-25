package com.webstore.service.whatsapp.button.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckoutStrategy implements ButtonActionStrategy {

    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;

    @Override
    public boolean supports(String buttonId) {
        return buttonId.startsWith("checkout_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("checkout_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                String echoMessage = String.format(
                        "💳 *Button Selected: Checkout*\n\n📦 Product: %s\n\n✅ You selected to checkout with this item!\n\nType 'categories' to continue shopping.",
                        product.getProductName()
                );
                messageSender.sendTextMessage(phoneNumberId, from, echoMessage);
            }
        } catch (Exception e) {
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was an error processing your Checkout selection.");
        }
    }


}
