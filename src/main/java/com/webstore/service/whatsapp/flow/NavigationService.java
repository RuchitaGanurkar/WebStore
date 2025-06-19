package com.webstore.service.whatsapp.flow;

import com.webstore.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {

    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);

    private final CategoryFlowService categoryFlowService;
    private final ProductFlowService productFlowService;
    private final PaginationUtil paginationUtil;

    public NavigationService(CategoryFlowService categoryFlowService,
                             ProductFlowService productFlowService,
                             PaginationUtil paginationUtil) {
        this.categoryFlowService = categoryFlowService;
        this.productFlowService = productFlowService;
        this.paginationUtil = paginationUtil;
    }

    public void handleCategoryPageNavigation(String phoneNumberId, String from, String listId) {
        try {
            String pageStr = listId.replaceAll("(next_cat_page_|prev_cat_page_)", "");
            int pageNumber = Integer.parseInt(pageStr);

            categoryFlowService.sendCategoryList("v22.0", phoneNumberId, from, pageNumber);
        } catch (Exception e) {
            logger.error("Error handling category page navigation: {}", e.getMessage());
        }
    }

    public void handleProductPageNavigation(String phoneNumberId, String from, String listId) {
        logger.info("=== PRODUCT NAVIGATION DEBUG ===");
        logger.info("Handling product page navigation: {}", listId);

        try {
            String[] parts = listId.split("_");
            logger.info("Navigation split parts: {}", java.util.Arrays.toString(parts));

            if (parts.length >= 4) {
                int pageNumber = Integer.parseInt(parts[2].substring(1)); // Remove 'p' prefix
                String encodedCategory = parts[3].substring(1); // Remove 'c' prefix
                String categoryName = paginationUtil.decodeFromBase64(encodedCategory);

                logger.info("Navigation parsed - Page: {}, Category: {}", pageNumber, categoryName);

                productFlowService.sendProductList("v22.0", phoneNumberId, from, categoryName, pageNumber);
            }
        } catch (Exception e) {
            logger.error("Error handling product page navigation: {}", e.getMessage(), e);
        }
    }
}