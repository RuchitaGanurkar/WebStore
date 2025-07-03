package com.webstore.service.whatsapp.list.impl;

import static com.webstore.constant.WhatsAppConstants.API_VERSION;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import com.webstore.service.whatsapp.list.ListActionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryListSelectionStrategy implements ListActionStrategy {

    private final CategoryBusinessService categoryService;
    private final ProductFlowService productFlowService;
    private final WhatsAppMessageSender messageSender;


    @Override
    public boolean supports(String listId) {
        return listId.startsWith("cat_page_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        try {
            String[] parts = listId.split("_");
            if (parts.length >= 5) {
                int page = Integer.parseInt(parts[2]);
                int item = Integer.parseInt(parts[4]);

                List<String> categories = categoryService.getAllCategoryNames();
                int index = (page - 1) * 7 + (item - 1);

                if (index >= 0 && index < categories.size()) {
                    String selectedCategory = categories.get(index);
                    messageSender.sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);
                    productFlowService.sendProductSelection(API_VERSION, phoneNumberId, from, selectedCategory);
                } else {
                    messageSender.sendTextMessage(phoneNumberId, from, "Invalid selection.");
                }
            } else {
                // 👇 Ensure malformed IDs are caught
                throw new IllegalArgumentException("Invalid listId format: " + listId);
            }
        } catch (Exception e) {
            log.error("Error processing category listId: {}", listId, e);
            messageSender.sendTextMessage(phoneNumberId, from, "Something went wrong. Try again.");
        }
    }
}
