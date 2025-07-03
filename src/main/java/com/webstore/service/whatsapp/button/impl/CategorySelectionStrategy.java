package com.webstore.service.whatsapp.button.impl;

import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import org.springframework.stereotype.Service;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

import java.util.List;

@Service
public class CategorySelectionStrategy implements ButtonActionStrategy {

    private final CategoryFlowService categoryFlowService;
    private final CategoryBusinessService categoryService;
    private final ProductFlowService productFlowService;
    private final WhatsAppMessageSender messageSender;

    public CategorySelectionStrategy(CategoryFlowService categoryFlowService,
                                     CategoryBusinessService categoryService,
                                     ProductFlowService productFlowService,
                                     WhatsAppMessageSender messageSender) {
        this.categoryFlowService = categoryFlowService;
        this.categoryService = categoryService;
        this.productFlowService = productFlowService;
        this.messageSender = messageSender;
    }

    @Override
    public boolean supports(String buttonId) {
        return buttonId.startsWith("cat_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        String categoryIdStr = buttonId.replace("cat_", "");

        if ("see_all".equals(categoryIdStr)) {
            categoryFlowService.sendCategoryList(API_VERSION, phoneNumberId, from, 1);
            return;
        }

        try {
            int categoryNumber = Integer.parseInt(categoryIdStr);
            List<String> categories = categoryService.getTop3CategoryNames();

            if (categoryNumber > 0 && categoryNumber <= categories.size()) {
                String selectedCategory = categories.get(categoryNumber - 1);
                messageSender.sendTextMessage(phoneNumberId, from, "✅ Selected: " + selectedCategory);
                productFlowService.sendProductSelection("v22.0", phoneNumberId, from, selectedCategory);
            }
        } catch (NumberFormatException e) {
            messageSender.sendTextMessage(phoneNumberId, from,
                    "Sorry, there was a problem processing your selection.");
        }
    }
}
