package com.webstore.util;

import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {

    private static final int MAX_SECTION_TITLE_LENGTH = 24;
    private static final int MAX_ROW_TITLE_LENGTH = 24;
    private static final int MAX_ROW_DESCRIPTION_LENGTH = 72;
    private static final int MAX_BUTTON_TITLE_LENGTH = 20;

    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public String truncateSectionTitle(String title) {
        return truncateText(title, MAX_SECTION_TITLE_LENGTH);
    }

    public String truncateRowTitle(String title) {
        return truncateText(title, MAX_ROW_TITLE_LENGTH);
    }

    public String truncateRowDescription(String description) {
        return truncateText(description, MAX_ROW_DESCRIPTION_LENGTH);
    }

    public String truncateButtonTitle(String title) {
        return truncateText(title, MAX_BUTTON_TITLE_LENGTH);
    }

    public String formatProductDetails(String productName, String description, String categoryName, String price) {
        return String.format("📦 *%s*\n\n📝 Description: %s\n🏷️ Category: %s\n💰 Price: %s\n\nReady to purchase?",
                productName,
                description != null ? description : "No description available",
                categoryName != null ? categoryName : "Unknown",
                price);
    }

    public String formatAddToCartMessage(String productName, String price, String categoryName) {
        return String.format(
                "🛒 *Added to Cart!*\n\n" +
                        "📦 Product: %s\n" +
                        "💰 Price: %s\n" +
                        "🏷️ Category: %s\n\n" +
                        "✅ Item added successfully!\n\n" +
                        "Type 'categories' to continue shopping or 'cart' to view your cart.",
                productName, price, categoryName
        );
    }
}