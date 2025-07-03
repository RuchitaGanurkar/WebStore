package com.webstore.service.whatsapp.flow;


import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryFlowService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryFlowService.class);

    private final CategoryBusinessService categoryService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;
    private final PaginationUtil paginationUtil;

    public CategoryFlowService(CategoryBusinessService categoryService,
                               ProductBusinessService productService,
                               WhatsAppMessageSender messageSender,
                               MessageBuilderService messageBuilder,
                               MessageFormatter formatter,
                               PaginationUtil paginationUtil) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.messageSender = messageSender;
        this.messageBuilder = messageBuilder;
        this.formatter = formatter;
        this.paginationUtil = paginationUtil;
    }

    public void sendCategorySelection(String version, String phoneNumberId, String recipientPhoneNumber) {
        if (categoryService.shouldUseButtonsForCategories()) {
            sendCategoryButtons(version, phoneNumberId, recipientPhoneNumber);
        } else {
            sendCategoryList(version, phoneNumberId, recipientPhoneNumber, 1);
        }
    }

    public void sendCategoryList(String version, String phoneNumberId, String recipientPhoneNumber, int pageNumber) {
        List<String> allCategories = categoryService.getAllCategoryNames();
        logger.info("Fetched all categories for pagination: {} categories, page: {}", allCategories.size(), pageNumber);

        if (allCategories.isEmpty()) {
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "No categories available at the moment.");
            return;
        }

        PaginationUtil.PaginationResult<String> paginationResult =
                paginationUtil.paginate(allCategories, pageNumber);

        List<WhatsAppRequestDto.Row> rows = new ArrayList<>();

        // Add category items
        for (int i = 0; i < paginationResult.getItems().size(); i++) {
            String categoryName = paginationResult.getItems().get(i);
            Integer categoryId = categoryService.getCategoryIdByName(categoryName);

            int productCount = 0;
            if (categoryId != null) {
                productCount = productService.getProductNamesByCategory(categoryId).size();
            }

            String rowDescription = String.format("%d products available", productCount);
            int actualIndex = (pageNumber - 1) * 7 + i + 1;

            rows.add(messageBuilder.createRow(
                    String.format("cat_page_%d_item_%d", pageNumber, actualIndex),
                    categoryName,
                    "🔢 " + rowDescription
            ));
        }

        // Add navigation options
        addNavigationRows(rows, paginationResult, "cat");

        WhatsAppRequestDto.Section section = messageBuilder.createSection("🏪 Categories", rows);

        String bodyText = String.format("📄 Page %d of %d (%d total categories)\n\nChoose a category to explore:",
                paginationResult.getCurrentPage(), paginationResult.getTotalPages(), paginationResult.getTotalItems());

        WhatsAppRequestDto requestBody = messageBuilder.buildListMessage(
                recipientPhoneNumber,
                "🛍️ WebStore",
                bodyText,
                "Tap to browse products",
                "Browse Categories",
                List.of(section)
        );

        messageSender.sendMessage(phoneNumberId, requestBody, "Category list message with pagination");
    }

    private void sendCategoryButtons(String version, String phoneNumberId, String recipientPhoneNumber) {
        List<String> categories = categoryService.getTop3CategoryNames();
        logger.info("Fetched top 3 categories: {}", categories);

        if (categories.isEmpty()) {
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "No categories available at the moment.");
            return;
        }

        List<WhatsAppRequestDto.Button> buttons = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            String buttonTitle = formatter.truncateText(categories.get(i), 20);
            buttons.add(messageBuilder.createButton("cat_" + (i + 1), buttonTitle));
        }

        if (categoryService.getTotalCategoryCount() > 3) {
            buttons.add(messageBuilder.createButton("cat_see_all", "See all options"));
        }

        String bodyText = "Choose a category to explore:\n" +
                categories.stream()
                        .map(cat -> "• " + formatter.truncateText(cat, 30))
                        .collect(Collectors.joining("\n"));

        WhatsAppRequestDto requestBody = messageBuilder.buildButtonMessage(
                recipientPhoneNumber,
                "🏪 WebStore Categories",
                bodyText,
                "Select any category",
                buttons
        );

        messageSender.sendMessage(phoneNumberId, requestBody, "Category buttons message");
    }

    private void addNavigationRows(List<WhatsAppRequestDto.Row> rows,
                                   PaginationUtil.PaginationResult<?> paginationResult, String type) {
        if (paginationResult.getTotalPages() > 1) {
            if (paginationResult.hasPrevious()) {
                rows.add(messageBuilder.createRow(
                        String.format("prev_%s_page_%d", type, paginationResult.getCurrentPage() - 1),
                        "⬅️ Previous Page",
                        String.format("Go to page %d", paginationResult.getCurrentPage() - 1)
                ));
            }

            if (paginationResult.hasNext()) {
                rows.add(messageBuilder.createRow(
                        String.format("next_%s_page_%d", type, paginationResult.getCurrentPage() + 1),
                        "➡️ Next Page",
                        String.format("Go to page %d", paginationResult.getCurrentPage() + 1)
                ));
            }
        }
    }
}