package com.webstore.service.whatsapp.list.impl;

import com.webstore.service.whatsapp.flow.CategoryFlowService;
import com.webstore.service.whatsapp.list.ListActionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackToCategoriesStrategy implements ListActionStrategy {

    private final CategoryFlowService categoryFlowService;

    @Override
    public boolean supports(String listId) {
        return "back_to_categories".equals(listId);
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        categoryFlowService.sendCategorySelection("v22.0", phoneNumberId, from);
    }
}
