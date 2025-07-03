package com.webstore.service.whatsapp.strategy.impl;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.MessageFormatter;
import com.webstore.service.whatsapp.strategy.ProductDisplayStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductButtonDisplayStrategy implements ProductDisplayStrategy {

    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;

    public ProductButtonDisplayStrategy(ProductBusinessService productService,
                                        WhatsAppMessageSender messageSender,
                                        MessageBuilderService messageBuilder,
                                        MessageFormatter formatter) {
        this.productService = productService;
        this.messageSender = messageSender;
        this.messageBuilder = messageBuilder;
        this.formatter = formatter;
    }

    @Override
    public boolean supports(int productCount) {
        return productCount <= 3;
    }

    @Override
    public void display(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        List<String> productNames = productService.getProductNamesByCategoryName(categoryName);
        List<WhatsAppRequestDto.Button> buttons = new ArrayList<>();
        StringBuilder productListText = new StringBuilder();

        int count = Math.min(productNames.size(), 3);
        for (int i = 0; i < count; i++) {
            String productName = productNames.get(i);
            Integer productId = productService.getProductIdByName(productName);
            String priceDisplay = productService.getProductPriceDisplay(productId);

            String displayName = formatter.truncateText(productName, 30);
            productListText.append(String.format("%d. %s\n💰 %s\n\n", i + 1, displayName, priceDisplay));

            String buttonTitle = productName.length() <= 17 ? productName : formatter.truncateText(productName, 17) + "...";
            buttons.add(messageBuilder.createButton("view_product_" + productId, buttonTitle));
        }

        WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                recipientPhoneNumber,
                formatter.truncateText("🛍️ " + categoryName + " Products", 60),
                "💰 Here are the products with prices:\n\n" + productListText,
                "Tap to view details & add to cart",
                buttons
        );

        messageSender.sendMessage(phoneNumberId, request, "Product buttons message");
    }
}
