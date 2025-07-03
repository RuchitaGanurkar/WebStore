package com.webstore.service.whatsapp.button.impl;

import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.webstore.constant.WhatsAppConstants.API_VERSION;

@Component
@RequiredArgsConstructor
public class BackToProductStrategy implements ButtonActionStrategy {

    private final CategoryFlowService categoryFlowService;
    private final WhatsAppMessageSender messageSender;

    @Override
    public boolean supports(String buttonId) {
        return "back_to_products".equals(buttonId);
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        categoryFlowService.sendCategorySelection(API_VERSION, phoneNumberId, from);
    }


}
