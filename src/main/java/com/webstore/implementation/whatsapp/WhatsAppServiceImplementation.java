package com.webstore.implementation.whatsapp;

import com.webstore.implementation.webhook.WebhookValidator;
import com.webstore.service.whatsapp.WhatsAppService;
import com.webstore.service.whatsapp.flow.ProductFlowService;
import com.webstore.service.whatsapp.flow.WelcomeFlowService;
import com.webstore.service.whatsapp.flow.CategoryFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppServiceImplementation implements WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppServiceImplementation.class);

    private final WebhookValidator webhookValidator;
    private final WelcomeFlowService welcomeFlowService;
    private final CategoryFlowService categoryFlowService;
    private final ProductFlowService productFlowService;

    public WhatsAppServiceImplementation(WebhookValidator webhookValidator,
                               WelcomeFlowService welcomeFlowService,
                               CategoryFlowService categoryFlowService,
                               ProductFlowService productFlowService) {
        this.webhookValidator = webhookValidator;
        this.welcomeFlowService = welcomeFlowService;
        this.categoryFlowService = categoryFlowService;
        this.productFlowService = productFlowService;
    }

    @Override
    public String verifyWebhook(String mode, String token, String challenge) {
        return webhookValidator.verifyWebhook(mode, token, challenge);
    }

    @Override
    public void sendWelcomeMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        welcomeFlowService.sendWelcomeMessage(version, phoneNumberId, recipientPhoneNumber);
    }

    @Override
    public void sendCategoryInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber) {
        categoryFlowService.sendCategorySelection(version, phoneNumberId, recipientPhoneNumber);
    }

    @Override
    public void sendProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String categoryName) {
        productFlowService.sendProductSelection(version, phoneNumberId, recipientPhoneNumber, categoryName);
    }

    @Override
    public void sendOneProductInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        productFlowService.sendProductDetails(version, phoneNumberId, recipientPhoneNumber, productName);
    }

    @Override
    public void showProductPriceInteractiveMessage(String version, String phoneNumberId, String recipientPhoneNumber, String productName) {
        productFlowService.sendProductPrice(version, phoneNumberId, recipientPhoneNumber, productName);
    }
}
