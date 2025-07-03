package com.webstore.service.whatsapp.strategy.impl;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.list.ListActionStrategy;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductListSelectionStrategy implements ListActionStrategy {

    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final PaginationUtil paginationUtil;
    private final MessageFormatter formatter;

    @Override
    public boolean supports(String listId) {
        return listId.startsWith("prod_p");
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        try {
            String[] parts = listId.split("_");
            if (parts.length >= 4) {
                int productId = Integer.parseInt(parts[2].substring(1)); // i3
                String categoryEncoded = parts[3].substring(1); // c...

                String categoryName = paginationUtil.decodeFromBase64(categoryEncoded);
                ProductResponseDto product = productService.getProductById(productId);

                if (product != null) {
                    String price = productService.getProductPriceDisplay(productId);
                    String message = formatter.formatAddToCartMessage(product.getProductName(), price, categoryName);
                    messageSender.sendTextMessage(phoneNumberId, from, message);
                } else {
                    messageSender.sendTextMessage(phoneNumberId, from, "Product not found.");
                }
            }
        } catch (Exception e) {
            log.error("Error processing product listId: {}", listId, e);
            messageSender.sendTextMessage(phoneNumberId, from, "Unable to process selection.");
        }
    }
}
