package com.webstore.service.whatsapp.business;

import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.entity.product.ProductPrice;
import com.webstore.repository.product.ProductPriceRepository;
import com.webstore.repository.product.ProductRepository;
import com.webstore.service.product.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class ProductBusinessService {

    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductService productService;
    private final CategoryBusinessService categoryBusinessService;

    public ProductBusinessService(ProductRepository productRepository,
                                  ProductPriceRepository productPriceRepository,
                                  ProductService productService,
                                  CategoryBusinessService categoryBusinessService) {
        this.productRepository = productRepository;
        this.productPriceRepository = productPriceRepository;
        this.productService = productService;
        this.categoryBusinessService = categoryBusinessService;
    }

    // ✅ Get product names by category ID
    public List<String> getProductNamesByCategory(Integer categoryId) {
        if (categoryId == null) return Collections.emptyList();
        return productRepository.findProductNamesByCategoryId(categoryId);
    }

    // ✅ Get product names by category NAME
    public List<String> getProductNamesByCategoryName(String categoryName) {
        Integer categoryId = categoryBusinessService.getCategoryIdByName(categoryName);
        if (categoryId == null) return Collections.emptyList();
        return getProductNamesByCategory(categoryId);
    }

    public Integer getProductIdByName(String productName) {
        return productRepository.findProductIdByProductName(productName);
    }

    public ProductResponseDto getProductById(Integer productId) {
        return productService.getProductById(productId);
    }

    public String getProductPriceDisplay(Integer productId) {
        List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
        if (productPrices.isEmpty()) {
            return "Price not available";
        }

        ProductPrice inrPrice = productPrices.stream()
                .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                .findFirst()
                .orElse(productPrices.get(0));

        BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
        return String.format("%s %.2f", inrPrice.getCurrency().getCurrencySymbol(), priceInRupees);
    }

    public List<ProductPrice> getProductPrices(Integer productId) {
        return productPriceRepository.findByProductProductId(productId);
    }

    public boolean shouldUseButtonsForProducts(List<String> productNames) {
        return productNames != null && productNames.size() <= 3;
    }
}
