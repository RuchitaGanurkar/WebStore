package com.webstore.service.whatsapp.strategy.impl;

import com.webstore.dto.request.WhatsAppRequestDto;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.strategy.ProductDisplayStrategy;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductListDisplayStrategy implements ProductDisplayStrategy {

    private final ProductBusinessService productService;
    private final CategoryBusinessService categoryService;
    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;
    private final PaginationUtil paginationUtil;

    public ProductListDisplayStrategy(ProductBusinessService productService,
                                      CategoryBusinessService categoryService,
                                      WhatsAppMessageSender messageSender,
                                      MessageBuilderService messageBuilder,
                                      MessageFormatter formatter,
                                      PaginationUtil paginationUtil) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.messageSender = messageSender;
        this.messageBuilder = messageBuilder;
        this.formatter = formatter;
        this.paginationUtil = paginationUtil;
    }

    @Override
    public boolean supports(int productCount) {
        return productCount > 3;
    }

    @Override
    public void display(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        // Default to page 1 if no page provided
        display(version, phoneNumberId, recipientPhoneNumber, categoryName, 1);
    }

    // ✅ Overloaded method for paginated display
    public void display(String version, String phoneNumberId, String recipientPhoneNumber,
                        String categoryName, int pageNumber) {

        Integer categoryId = categoryService.getCategoryIdByName(categoryName);
        List<String> products = productService.getProductNamesByCategory(categoryId);

        PaginationUtil.PaginationResult<String> paginated = paginationUtil.paginate(products, pageNumber);
        List<WhatsAppRequestDto.Row> rows = new ArrayList<>();

        // Add product rows
        for (String productName : paginated.getItems()) {
            Integer productId = productService.getProductIdByName(productName);
            String priceDisplay = productService.getProductPriceDisplay(productId);
            String rowId = String.format("prod_p%d_i%d_c%s", pageNumber, productId, paginationUtil.encodeToBase64(categoryName));
            rows.add(messageBuilder.createRow(rowId, productName, "💰 " + priceDisplay + " • Tap to add to cart"));
        }

        // Navigation rows
        if (paginated.hasPrevious()) {
            String prevId = String.format("prev_prod_p%d_c%s", pageNumber - 1, paginationUtil.encodeToBase64(categoryName));
            rows.add(messageBuilder.createRow(prevId, "⬅️ Previous Page", String.format("Go to page %d", pageNumber - 1)));
        }

        if (paginated.hasNext()) {
            String nextId = String.format("next_prod_p%d_c%s", pageNumber + 1, paginationUtil.encodeToBase64(categoryName));
            rows.add(messageBuilder.createRow(nextId, "➡️ Next Page", String.format("Go to page %d", pageNumber + 1)));
        }

        // Back to categories
        rows.add(messageBuilder.createRow("back_to_categories", "⬅️ Back to Categories", "Browse other categories"));

        WhatsAppRequestDto.Section section = messageBuilder.createSection("🛒 " + categoryName, rows);

        WhatsAppRequestDto request = messageBuilder.buildListMessage(
                recipientPhoneNumber,
                formatter.truncateText("🛍️ Shop " + categoryName, 60),
                String.format("📄 Page %d of %d (%d total products)\n\nChoose a product to add to cart:",
                        paginated.getCurrentPage(), paginated.getTotalPages(), paginated.getTotalItems()),
                "💡 Select to add to cart",
                "View Products",
                List.of(section)
        );

        messageSender.sendMessage(phoneNumberId, request, "Product list page " + pageNumber);
    }
}
