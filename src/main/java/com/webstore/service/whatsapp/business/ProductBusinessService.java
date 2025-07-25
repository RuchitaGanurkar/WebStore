package com.webstore.service.whatsapp.business;

import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.entity.product.ProductPrice;
import com.webstore.repository.product.ProductPriceRepository;
import com.webstore.repository.product.ProductRepository;
import com.webstore.service.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class ProductBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(ProductBusinessService.class);

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

    // ============ EXISTING METHODS (UNCHANGED) ============

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

    // ============ NEW METHODS FOR CART FLOW ============

    /**
     * Get product price as BigDecimal (for cart calculations)
     * @param productId Product ID
     * @return Product price in BigDecimal format
     */
    public BigDecimal getProductPrice(Integer productId) {
        logger.debug("Getting product price for product ID: {}", productId);

        try {
            List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
            if (productPrices.isEmpty()) {
                logger.warn("No price found for product ID: {}", productId);
                return BigDecimal.ZERO;
            }

            // Find INR price first, fallback to first available price
            ProductPrice inrPrice = productPrices.stream()
                    .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                    .findFirst()
                    .orElse(productPrices.get(0));

            // Convert from smallest currency unit (paisa to rupees)
            BigDecimal priceInRupees = new BigDecimal(inrPrice.getPriceAmount()).divide(BigDecimal.valueOf(100));
            logger.debug("Product ID: {} price: ₹{}", productId, priceInRupees);

            return priceInRupees;

        } catch (Exception e) {
            logger.error("Error getting price for product ID: {}", productId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get available quantity for a product
     * @param productId Product ID
     * @return Available quantity
     */
    public Integer getAvailableQuantity(Integer productId) {
        logger.debug("Getting available quantity for product ID: {}", productId);

        try {
            // This would typically come from an inventory table
            // For now, implementing a basic check
            ProductResponseDto product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("Product not found with ID: {}", productId);
                return 0;
            }

            // TODO: Replace with actual inventory lookup
            // For demo purposes, return a default quantity
            // In real implementation, this should query inventory table
            Integer availableQuantity = getProductAvailabilityFromInventory(productId);

            logger.debug("Product ID: {} available quantity: {}", productId, availableQuantity);
            return availableQuantity;

        } catch (Exception e) {
            logger.error("Error getting available quantity for product ID: {}", productId, e);
            return 0;
        }
    }

    /**
     * Check if requested quantity is available for a product
     * @param productId Product ID
     * @param requestedQuantity Requested quantity
     * @return true if quantity is available, false otherwise
     */
    public boolean isQuantityAvailable(Integer productId, Integer requestedQuantity) {
        logger.debug("Checking quantity availability for product ID: {}, requested: {}", productId, requestedQuantity);

        try {
            if (requestedQuantity == null || requestedQuantity <= 0) {
                return false;
            }

            Integer availableQuantity = getAvailableQuantity(productId);
            boolean isAvailable = availableQuantity >= requestedQuantity;

            logger.debug("Product ID: {} quantity check - requested: {}, available: {}, result: {}",
                    productId, requestedQuantity, availableQuantity, isAvailable);

            return isAvailable;

        } catch (Exception e) {
            logger.error("Error checking quantity availability for product ID: {}", productId, e);
            return false;
        }
    }

    /**
     * Validate product exists and has minimum details
     * @param productId Product ID
     * @return true if product is valid, false otherwise
     */
    public boolean isProductValid(Integer productId) {
        logger.debug("Validating product ID: {}", productId);

        try {
            if (productId == null || productId <= 0) {
                return false;
            }

            ProductResponseDto product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("Product not found with ID: {}", productId);
                return false;
            }

            // Check if product has a valid name and price
            boolean hasValidName = product.getProductName() != null && !product.getProductName().trim().isEmpty();
            boolean hasValidPrice = !getProductPrices(productId).isEmpty();

            boolean isValid = hasValidName && hasValidPrice;
            logger.debug("Product ID: {} validation result: {}", productId, isValid);

            return isValid;

        } catch (Exception e) {
            logger.error("Error validating product ID: {}", productId, e);
            return false;
        }
    }

    /**
     * Get product price with currency symbol
     * @param productId Product ID
     * @return Formatted price string with currency symbol
     */
    public String getFormattedProductPrice(Integer productId) {
        logger.debug("Getting formatted price for product ID: {}", productId);

        try {
            BigDecimal price = getProductPrice(productId);
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                return "Price not available";
            }

            // Get currency symbol from price data
            List<ProductPrice> productPrices = productPriceRepository.findByProductProductId(productId);
            if (!productPrices.isEmpty()) {
                ProductPrice inrPrice = productPrices.stream()
                        .filter(pp -> "INR".equals(pp.getCurrency().getCurrencyCode()))
                        .findFirst()
                        .orElse(productPrices.get(0));

                String symbol = inrPrice.getCurrency().getCurrencySymbol();
                return String.format("%s %.2f", symbol, price);
            }

            return String.format("₹ %.2f", price);

        } catch (Exception e) {
            logger.error("Error getting formatted price for product ID: {}", productId, e);
            return "Price not available";
        }
    }

    /**
     * Calculate total price for product with quantity
     * @param productId Product ID
     * @param quantity Quantity
     * @return Total price
     */
    public BigDecimal calculateProductTotal(Integer productId, Integer quantity) {
        logger.debug("Calculating total for product ID: {}, quantity: {}", productId, quantity);

        try {
            if (quantity == null || quantity <= 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal unitPrice = getProductPrice(productId);
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

            logger.debug("Product ID: {} total calculation - unit price: ₹{}, quantity: {}, total: ₹{}",
                    productId, unitPrice, quantity, total);

            return total;

        } catch (Exception e) {
            logger.error("Error calculating total for product ID: {}, quantity: {}", productId, quantity, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get product summary for cart display
     * @param productId Product ID
     * @param quantity Quantity
     * @return Product summary string
     */
    public String getProductCartSummary(Integer productId, Integer quantity) {
        logger.debug("Getting cart summary for product ID: {}, quantity: {}", productId, quantity);

        try {
            ProductResponseDto product = productService.getProductById(productId);
            if (product == null) {
                return "Product not found";
            }

            BigDecimal unitPrice = getProductPrice(productId);
            BigDecimal total = calculateProductTotal(productId, quantity);

            return String.format("%s (x%d) - ₹%.2f",
                    product.getProductName(), quantity, total);

        } catch (Exception e) {
            logger.error("Error getting cart summary for product ID: {}, quantity: {}", productId, quantity, e);
            return "Error loading product details";
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Get product availability from inventory system
     * This is a placeholder method that should be replaced with actual inventory lookup
     * @param productId Product ID
     * @return Available quantity from inventory
     */
    private Integer getProductAvailabilityFromInventory(Integer productId) {
        // TODO: Replace with actual inventory service call
        // This is a placeholder implementation

        try {
            // For now, return a mock quantity based on product ID
            // In real implementation, this should query inventory table

            // Mock availability - in production this would be:
            // return inventoryService.getAvailableQuantity(productId);

            // For demo purposes, return different quantities for different products
            if (productId <= 10) {
                return 50; // High availability for first 10 products
            } else if (productId <= 50) {
                return 25; // Medium availability
            } else {
                return 10; // Lower availability for other products
            }

        } catch (Exception e) {
            logger.error("Error getting inventory for product ID: {}", productId, e);
            return 0;
        }
    }

    /**
     * Check if product is in stock
     * @param productId Product ID
     * @return true if in stock, false otherwise
     */
    public boolean isProductInStock(Integer productId) {
        Integer availableQuantity = getAvailableQuantity(productId);
        return availableQuantity != null && availableQuantity > 0;
    }

    /**
     * Get maximum quantity that can be ordered for a product
     * @param productId Product ID
     * @return Maximum orderable quantity
     */
    public Integer getMaxOrderableQuantity(Integer productId) {
        Integer availableQuantity = getAvailableQuantity(productId);

        // Business rule: Maximum 10 items per order per product
        int maxPerOrder = 10;

        if (availableQuantity == null || availableQuantity <= 0) {
            return 0;
        }

        return Math.min(availableQuantity, maxPerOrder);
    }

    /**
     * Validate product for cart operations
     * @param productId Product ID
     * @param quantity Requested quantity
     * @return Validation result with error message if any
     */
    public ProductValidationResult validateProductForCart(Integer productId, Integer quantity) {
        logger.debug("Validating product for cart - ID: {}, quantity: {}", productId, quantity);

        try {
            // Check if product exists
            if (!isProductValid(productId)) {
                return new ProductValidationResult(false, "Product not found or invalid");
            }

            // Check if product is in stock
            if (!isProductInStock(productId)) {
                return new ProductValidationResult(false, "Product is out of stock");
            }

            // Check if requested quantity is valid
            if (quantity == null || quantity <= 0) {
                return new ProductValidationResult(false, "Invalid quantity");
            }

            // Check if requested quantity is available
            if (!isQuantityAvailable(productId, quantity)) {
                Integer availableQuantity = getAvailableQuantity(productId);
                return new ProductValidationResult(false,
                        String.format("Only %d units available", availableQuantity));
            }

            // Check if quantity exceeds maximum orderable quantity
            Integer maxOrderable = getMaxOrderableQuantity(productId);
            if (quantity > maxOrderable) {
                return new ProductValidationResult(false,
                        String.format("Maximum %d units can be ordered", maxOrderable));
            }

            return new ProductValidationResult(true, "Valid");

        } catch (Exception e) {
            logger.error("Error validating product for cart - ID: {}, quantity: {}", productId, quantity, e);
            return new ProductValidationResult(false, "Validation error occurred");
        }
    }

    // Inner class for validation results
    public static class ProductValidationResult {
        private final boolean valid;
        private final String message;

        public ProductValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}