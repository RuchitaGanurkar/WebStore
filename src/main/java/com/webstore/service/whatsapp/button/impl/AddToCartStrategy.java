package com.webstore.service.whatsapp.button.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddToCartStrategy implements ButtonActionStrategy {

    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;

    @Override
    public boolean supports(String buttonId) {
        return buttonId.startsWith("add_cart_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        String productIdStr = buttonId.replace("add_cart_", "");
        try {
            Integer productId = Integer.parseInt(productIdStr);
            ProductResponseDto product = productService.getProductById(productId);
            if (product != null) {
                String echoMessage = String.format(
                        "🛒 *Button Selected: Add to Cart*\n\n📦 Product: %s\n\n✅ You selected to add this item to your cart!\n\nType 'categories' to continue shopping.",
                        product.getProductName()
                );
                messageSender.sendTextMessage(phoneNumberId, from, echoMessage);
            }
        } catch (Exception e) {
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was an error processing your Add to Cart selection.");
        }
    }


}
