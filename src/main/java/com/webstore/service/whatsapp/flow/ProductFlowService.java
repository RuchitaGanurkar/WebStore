package com.webstore.service.whatsapp.flow;

import com.webstore.entity.ProductPrice;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.exception.CategoryNotFoundException;
import com.webstore.exception.ProductNotFoundException;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.strategy.ProductDisplayStrategy;
import com.webstore.service.whatsapp.strategy.impl.ProductListDisplayStrategy;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductFlowService {

    private static final Logger logger = LoggerFactory.getLogger(ProductFlowService.class);

    private final CategoryBusinessService categoryService;
    private final ProductBusinessService productService;
    private final WhatsAppMessageSender messageSender;
    private final List<ProductDisplayStrategy> productDisplayStrategies;
    private final MessageBuilderService messageBuilder;
    private final MessageFormatter formatter;
    private final PaginationUtil paginationUtil;

    public ProductFlowService(CategoryBusinessService categoryService,
                              ProductBusinessService productService,
                              WhatsAppMessageSender messageSender,
                              List<ProductDisplayStrategy> productDisplayStrategies,
                              MessageBuilderService messageBuilder,
                              MessageFormatter formatter,
                              PaginationUtil paginationUtil) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.messageSender = messageSender;
        this.productDisplayStrategies = productDisplayStrategies;
        this.messageBuilder = messageBuilder;
        this.formatter = formatter;
        this.paginationUtil = paginationUtil;
    }

    public void sendProductSelection(String version, String phoneNumberId,
                                     String recipientPhoneNumber, String categoryName) {

        Integer categoryId = categoryService.getCategoryIdByName(categoryName);
        if (categoryId == null) {
            throw new CategoryNotFoundException(categoryName);
        }

        List<String> productNames = productService.getProductNamesByCategory(categoryId);
        int productCount = productNames.size();

        logger.info("Fetched {} products for category '{}'", productCount, categoryName);

        productDisplayStrategies.stream()
                .filter(strategy -> strategy.supports(productCount))
                .findFirst()
                .ifPresentOrElse(
                        strategy -> strategy.display(version, phoneNumberId, recipientPhoneNumber, categoryName),
                        () -> messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber, "No products found.")
                );
    }

    public void sendProductDetails(String version, String phoneNumberId,
                                   String recipientPhoneNumber, String productName) {
        Integer productId = productService.getProductIdByName(productName);
        if (productId == null) {
            throw new ProductNotFoundException(productName);
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

        WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                recipientPhoneNumber,
                "Product Details",
                productDetails,
                "Choose your next action",
                buttons
        );

        messageSender.sendMessage(phoneNumberId, request, "Product details message");
    }

    public void sendPaginatedProductList(String version, String phoneNumberId, String recipientPhoneNumber,
                                         String categoryName, int pageNumber) {
        Integer categoryId = categoryService.getCategoryIdByName(categoryName);
        if (categoryId == null) {
            throw new CategoryNotFoundException(categoryName);
        }

        List<String> allProducts = productService.getProductNamesByCategory(categoryId);
        if (allProducts.isEmpty()) {
            messageSender.sendTextMessage(phoneNumberId, recipientPhoneNumber, "No products in category: " + categoryName);
            return;
        }

        // Only list strategy can handle this — fallback if needed
        productDisplayStrategies.stream()
                .filter(strategy -> strategy.supports(allProducts.size()))
                .filter(strategy -> strategy instanceof ProductListDisplayStrategy) // optional
                .findFirst()
                .ifPresent(strategy -> ((ProductListDisplayStrategy) strategy)
                        .display(version, phoneNumberId, recipientPhoneNumber, categoryName, pageNumber));
    }

    public void sendProductPrice(String version, String phoneNumberId,
                                 String recipientPhoneNumber, String productName) {
        Integer productId = productService.getProductIdByName(productName);
        if (productId == null) {
            throw new ProductNotFoundException(productName);
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
        String priceDetails = String.format(
                "💰 *Price Information*\n\n📦 Product: %s\n💵 Price: %s %.2f\n💱 Currency: %s\n\nWould you like to add this to your cart?",
                productName, inrPrice.getCurrency().getCurrencySymbol(), priceInRupees,
                inrPrice.getCurrency().getCurrencyCode());

        List<WhatsAppRequestDto.Button> buttons = List.of(
                messageBuilder.createButton("add_cart_" + productId, "Add to Cart"),
                messageBuilder.createButton("back_to_product_" + productId, "Back to Product")
        );

        WhatsAppRequestDto request = messageBuilder.buildButtonMessage(
                recipientPhoneNumber,
                "💰 Pricing Details",
                priceDetails,
                null,
                buttons
        );

        messageSender.sendMessage(phoneNumberId, request, "Product price message");
    }
}
