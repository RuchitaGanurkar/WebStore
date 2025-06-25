package com.webstore.service.whatsapp.list.impl;

import com.webstore.service.whatsapp.flow.NavigationService;
import com.webstore.service.whatsapp.list.ListActionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListPageNavigationStrategy implements ListActionStrategy {

    private final NavigationService navigationService;

    @Override
    public boolean supports(String listId) {
        return listId.startsWith("next_") || listId.startsWith("prev_");
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        if (listId.contains("cat_page")) {
            navigationService.handleCategoryPageNavigation(phoneNumberId, from, listId);
        } else if (listId.contains("prod_p")) {
            navigationService.handleProductPageNavigation(phoneNumberId, from, listId);
        }
    }
}
