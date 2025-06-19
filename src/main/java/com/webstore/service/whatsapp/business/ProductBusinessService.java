package com.webstore.service.whatsapp.business;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.ProductPrice;
import com.webstore.repository.ProductPriceRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductBusinessService {

    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductService productService;

    public ProductBusinessService(ProductRepository productRepository,
                                  ProductPriceRepository productPriceRepository,
                                  ProductService productService) {
        this.productRepository = productRepository;
        this.productPriceRepository = productPriceRepository;
        this.productService = productService;
    }

    public List<String> getProductNamesByCategory(Integer categoryId) {
        return productRepository.findProductNamesByCategoryId(categoryId);
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
        return productNames.size() <= 3;
    }
}