package com.webstore.service.whatsapp.button.impl;

import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import org.springframework.stereotype.Service;

@Service
public class ProductViewStrategy implements ButtonActionStrategy {

    private final ProductBusinessService productService;
    private final ProductFlowService productFlowService;
    private final WhatsAppMessageSender messageSender;

    public ProductViewStrategy(ProductBusinessService productService,
                               ProductFlowService productFlowService,
                               WhatsAppMessageSender messageSender) {
        this.productService = productService;
        this.productFlowService = productFlowService;
        this.messageSender = messageSender;
    }

    @Override
    public boolean supports(String buttonId) {
        return buttonId.startsWith("view_product_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        try {
            Integer productId = Integer.parseInt(buttonId.replace("view_product_", ""));
            var product = productService.getProductById(productId);
            if (product != null) {
                productFlowService.sendProductDetails("v22.0", phoneNumberId, from, product.getProductName());
            } else {
                messageSender.sendTextMessage(phoneNumberId, from, "Product not found.");
            }
        } catch (Exception e) {
            messageSender.sendTextMessage(phoneNumberId, from, "Something went wrong.");
        }
    }
}
