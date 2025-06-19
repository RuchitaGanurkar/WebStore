package com.webstore.service.whatsapp.flow;

import com.webstore.dto.request.WhatsAppRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.ProductPrice;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductFlowService {

    private static final Logger logger = LoggerFactory.getLogger(ProductFlowService.class);

    private final CategoryBusinessService categoryService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;
    private final PaginationUtil paginationUtil;

    public ProductFlowService(CategoryBusinessService categoryService,
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

    public void sendProductSelection(String version, String phoneNumberId,
                                     String recipientPhoneNumber, String categoryName) {
        Integer categoryId = categoryService.getCategoryIdByName(categoryName);
        if (categoryId == null) {
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "Category not found: " + categoryName);
            return;
        }

        List<String> productNames = productService.getProductNamesByCategory(categoryId);
        logger.info("Fetched products for category {}: {}", categoryName, productNames);

        if (productNames.isEmpty()) {
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "No products found in " + categoryName + " category.");
            return;
        }

        if (productService.shouldUseButtonsForProducts(productNames)) {
            sendProductButtons(version, phoneNumberId, recipientPhoneNumber, categoryName, productNames);
        } else {
            sendProductList(version, phoneNumberId, recipientPhoneNumber, categoryName, 1);
        }
    }

    public void sendProductList(String version, String phoneNumberId,
                                String recipientPhoneNumber, String categoryName, int pageNumber) {
        logger.info("=== PRODUCT PAGINATION DEBUG ===");
        logger.info("Category Name: {}, Requested Page: {}", categoryName, pageNumber);

        Integer categoryId = categoryService.getCategoryIdByName(categoryName);
        if (categoryId == null) {
            logger.error("Category ID not found for category: {}", categoryName);
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "Category not found: " + categoryName);
            return;
        }

        List<String> allProducts = productService.getProductNamesByCategory(categoryId);
        logger.info("Total products fetched from database: {}", allProducts.size());

        if (allProducts.isEmpty()) {
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "No products found in " + categoryName + " category.");
            return;
        }

        PaginationUtil.PaginationResult<String> paginationResult =
                paginationUtil.paginate(allProducts, pageNumber);

        List<WhatsAppRequestDto.Row> rows = new ArrayList<>();

        // Add product items
        for (String productName : paginationResult.getItems()) {
            Integer productId = productService.getProductIdByName(productName);

            if (productId != null) {
                String priceDisplay = productService.getProductPriceDisplay(productId);
                String rowDescription = priceDisplay + " • Tap to add to cart";

                String encodedCategoryName = paginationUtil.encodeToBase64(categoryName);
                String rowId = String.format("prod_p%d_i%d_c%s", pageNumber, productId, encodedCategoryName);

                rows.add(messageBuilder.createRow(rowId, productName, "💰 " + rowDescription));
            }
        }

        // Add navigation options
        addProductNavigationRows(rows, paginationResult, categoryName);

        // Back to categories option
        rows.add(messageBuilder.createRow("back_to_categories", "⬅️ Back to Categories",
                "Browse other product categories"));

        String shortCategoryName = categoryName.length() > 15 ? categoryName.substring(0, 15) : categoryName;
        WhatsAppRequestDto.Section section = messageBuilder.createSection("🛒 " + shortCategoryName, rows);

        String headerText = formatter.truncateText("🛍️ Shop " + categoryName, 60);
        String bodyText = String.format("📄 Page %d of %d (%d total products)\n\nChoose a product to add to cart:",
                paginationResult.getCurrentPage(), paginationResult.getTotalPages(), paginationResult.getTotalItems());

        WhatsAppRequestDto requestBody = messageBuilder.buildListMessage(
                recipientPhoneNumber,
                headerText,
                bodyText,
                "💡 Select to add to cart",
                "View Products",
                List.of(section)
        );

        messageSender.sendMessage(phoneNumberId, requestBody, "Product list message with pagination");
    }

    private void sendProductButtons(String version, String phoneNumberId,
                                    String recipientPhoneNumber, String categoryName, List<String> productNames) {
        StringBuilder productListText = new StringBuilder();
        List<WhatsAppRequestDto.Button> buttons = new ArrayList<>();

        int productCount = Math.min(productNames.size(), 3);
        for (int i = 0; i < productCount; i++) {
            String productName = productNames.get(i);
            Integer productId = productService.getProductIdByName(productName);

            if (productId != null) {
                String priceDisplay = productService.getProductPriceDisplay(productId);

                String displayName = formatter.truncateText(productName, 30);
                productListText.append(String.format("%d. %s\n   💰 %s\n\n", i + 1, displayName, priceDisplay));

                String buttonTitle = formatter.truncateText(productName, 17) + "...";
                if (productName.length() <= 17) {
                    buttonTitle = productName;
                }
                buttons.add(messageBuilder.createButton("view_product_" + productId, buttonTitle));
            }
        }

        String headerText = formatter.truncateText("🛍️ " + categoryName + " Products", 60);

        WhatsAppRequestDto requestBody = messageBuilder.buildButtonMessage(
                recipientPhoneNumber,
                headerText,
                "💰 Here are the products with prices:\n\n" + productListText.toString(),
                "Tap to view details & add to cart",
                buttons
        );

        messageSender.sendMessage(phoneNumberId, requestBody, "Product buttons message");
    }

    public void sendProductDetails(String version, String phoneNumberId,
                                   String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productService.getProductIdByName(productName);
            if (productId == null) {
                messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                        "Product not found: " + productName);
                return;
            }

            ProductResponseDto product = productService.getProductById(productId);
            String priceInfo = productService.getProductPriceDisplay(productId);

            String productDetails = formatter.formatProductDetails(
                    product.getProductName(),
                    product.getProductDescription(),
                    product.getCategory() != null ? product.getCategory().getCategoryName() : "Unknown",
                    priceInfo
            );

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("add_cart_" + productId, "🛒 Add to Cart"),
                    messageBuilder.createButton("checkout_" + productId, "💳 Checkout"),
                    messageBuilder.createButton("back_to_products", "⬅️ Back")
            );

            WhatsAppRequestDto requestBody = messageBuilder.buildButtonMessage(
                    recipientPhoneNumber,
                    "Product Details",
                    productDetails,
                    "Choose your next action",
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, requestBody, "Product details message");

        } catch (Exception e) {
            logger.error("Failed to send product details: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "Sorry, there was an error retrieving product details.");
        }
    }

    public void sendProductPrice(String version, String phoneNumberId,
                                 String recipientPhoneNumber, String productName) {
        try {
            Integer productId = productService.getProductIdByName(productName);
            if (productId == null) {
                messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                        "Product not found: " + productName);
                return;
            }

            List<ProductPrice> productPrices = productService.getProductPrices(productId);
            if (productPrices.isEmpty()) {
                messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                        "No pricing information available for " + productName);
                return;
            }

            ProductPrice inrPrice = productPrices.stream()
                    .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                    .findFirst()
                    .orElse(productPrices.get(0));

            BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
            String priceDetails = String.format("💰 *Price Information*\n\n📦 Product: %s\n💵 Price: %s %.2f\n💱 Currency: %s\n\nWould you like to add this to your cart?",
                    productName, inrPrice.getCurrency().getCurrencySymbol(), priceInRupees, inrPrice.getCurrency().getCurrencyCode());

            List<WhatsAppRequestDto.Button> buttons = List.of(
                    messageBuilder.createButton("add_cart_" + productId, "Add to Cart"),
                    messageBuilder.createButton("back_to_product_" + productId, "Back to Product")
            );

            WhatsAppRequestDto requestBody = messageBuilder.buildButtonMessage(
                    recipientPhoneNumber,
                    "💰 Pricing Details",
                    priceDetails,
                    null,
                    buttons
            );

            messageSender.sendMessage(phoneNumberId, requestBody, "Product price message");

        } catch (Exception e) {
            logger.error("Failed to send product price: {}", e.getMessage(), e);
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber,
                    "Sorry, there was an error retrieving price information.");
        }
    }

    private void addProductNavigationRows(List<WhatsAppRequestDto.Row> rows,
                                          PaginationUtil.PaginationResult<?> paginationResult, String categoryName) {
        if (paginationResult.getTotalPages() > 1) {
            String encodedCategoryName = paginationUtil.encodeToBase64(categoryName);

            if (paginationResult.hasPrevious()) {
                String prevId = String.format("prev_prod_p%d_c%s", paginationResult.getCurrentPage() - 1, encodedCategoryName);
                rows.add(messageBuilder.createRow(prevId, "⬅️ Previous Page",
                        String.format("Go to page %d", paginationResult.getCurrentPage() - 1)));
            }

            if (paginationResult.hasNext()) {
                String nextId = String.format("next_prod_p%d_c%s", paginationResult.getCurrentPage() + 1, encodedCategoryName);
                rows.add(messageBuilder.createRow(nextId, "➡️ Next Page",
                        String.format("Go to page %d", paginationResult.getCurrentPage() + 1)));
            }
        }
    }
}
