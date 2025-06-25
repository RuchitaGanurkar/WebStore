package com.webstore.service.whatsapp.flow;

import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {

    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
    private static final String DEFAULT_VERSION = "v22.0";

    private final CategoryFlowService categoryFlowService;
    private final ProductFlowService productFlowService;
    private final PaginationUtil paginationUtil;
    private final WhatsAppMessageSender messageSender;

    public NavigationService(CategoryFlowService categoryFlowService,
                             ProductFlowService productFlowService,
                             PaginationUtil paginationUtil,
                             WhatsAppMessageSender messageSender) {
        this.categoryFlowService = categoryFlowService;
        this.productFlowService = productFlowService;
        this.paginationUtil = paginationUtil;
        this.messageSender = messageSender;
    }

    public void handleCategoryPageNavigation(String phoneNumberId, String from, String listId) {
        try {
            String pageStr = listId.replaceAll("(next_cat_page_|prev_cat_page_)", "");
            int pageNumber = Integer.parseInt(pageStr);

            categoryFlowService.sendCategoryList(DEFAULT_VERSION, phoneNumberId, from, pageNumber);
        } catch (Exception e) {
            logger.error("Error handling category page navigation: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, from, "⚠️ Unable to navigate categories.");
        }
    }

    public void handleProductPageNavigation(String phoneNumberId, String from, String listId) {
        logger.info("Handling product page navigation: {}", listId);
        try {
            String[] parts = listId.split("_");
            if (parts.length >= 4) {
                int pageNumber = Integer.parseInt(parts[2].substring(1)); // e.g. "p2" -> 2
                String encodedCategory = parts[3].substring(1); // e.g. "cXYZ" -> "XYZ"
                String categoryName = paginationUtil.decodeFromBase64(encodedCategory);

                productFlowService.sendPaginatedProductList(DEFAULT_VERSION, phoneNumberId, from, categoryName, pageNumber);
            } else {
                throw new IllegalArgumentException("Invalid product navigation ID format");
            }
        } catch (Exception e) {
            logger.error("Error handling product page navigation: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, from, "⚠️ Unable to navigate product pages.");
        }
    }
}
