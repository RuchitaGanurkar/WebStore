package com.webstore.implementation;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.entity.*;
import com.webstore.repository.*;
import com.webstore.service.ProductPriceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductPriceServiceImplementation implements ProductPriceService {

    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductPriceResponseDto createProductPrice(ProductPriceRequestDto request) {
        System.out.printf("Creating product price for productId: %s and currencyId: %s%n",
                request.getProductId(), request.getCurrencyId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + request.getProductId()));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with id: " + request.getCurrencyId()));

        Optional<ProductPrice> existingPrice = productPriceRepository
                .findByProductProductIdAndCurrencyCurrencyId(request.getProductId(), request.getCurrencyId());

        if (existingPrice.isPresent()) {
            throw new IllegalArgumentException("Price already exists for this product and currency combination");
        }

        if (request.getPriceAmount().compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative");
        }

        ProductPrice productPrice = new ProductPrice();
        productPrice.setProduct(product);
        productPrice.setCurrency(currency);
        productPrice.setPriceAmount(request.getPriceAmount());

        ProductPrice savedProductPrice = productPriceRepository.save(productPrice);
        System.out.printf("Product price created with id: %s%n", savedProductPrice.getProductPriceId());

        return mapToResponseDto(savedProductPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPriceResponseDto getProductPriceById(Integer id) {
        System.out.printf("Fetching product price with id: %s%n", id);

        ProductPrice productPrice = productPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product price not found with id: " + id));

        return mapToResponseDto(productPrice);
    }

    @Override
    @Transactional
    public ProductPriceResponseDto updateProductPrice(Integer id, BigInteger priceAmount) {
        System.out.printf("Updating product price with id: %s to new amount: %s%n", id, priceAmount);

        if (priceAmount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative");
        }

        ProductPrice productPrice = productPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product price not found with id: " + id));

        productPrice.setPriceAmount(priceAmount);
        ProductPrice updatedProductPrice = productPriceRepository.save(productPrice);

        return mapToResponseDto(updatedProductPrice);
    }

    @Override
    @Transactional
    public void deleteProductPrice(Integer id) {
        System.out.printf("Deleting product price with id: %s%n", id);

        ProductPrice productPrice = productPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product price not found with id: " + id));

        productPriceRepository.delete(productPrice);

        System.out.printf("Product price with id: %s has been deleted%n", id);
    }

    private ProductPriceResponseDto mapToResponseDto(ProductPrice productPrice) {
        ProductPriceResponseDto responseDto = new ProductPriceResponseDto();
        responseDto.setProductPriceId(productPrice.getProductPriceId());
        responseDto.setProductId(productPrice.getProduct().getProductId());
        responseDto.setProductName(productPrice.getProduct().getProductName());
        responseDto.setCurrencyId(productPrice.getCurrency().getCurrencyId());
        responseDto.setCurrencyCode(productPrice.getCurrency().getCurrencyCode());
        responseDto.setCurrencySymbol(productPrice.getCurrency().getCurrencySymbol());
        responseDto.setPriceAmount(productPrice.getPriceAmount());

        String formattedPrice = formatPrice(productPrice.getPriceAmount(), productPrice.getCurrency().getCurrencySymbol());
        System.out.printf("Formatted price for display: %s%n", formattedPrice);

        responseDto.setCreatedAt(productPrice.getCreatedAt());
        responseDto.setCreatedBy(productPrice.getCreatedBy());
        responseDto.setUpdatedAt(productPrice.getUpdatedAt());
        responseDto.setUpdatedBy(productPrice.getUpdatedBy());

        return responseDto;
    }

    // Utility method for formatting paise to rupees string
    private String formatPrice(BigInteger amountInPaise, String currencySymbol) {
        BigDecimal amountInRupees = new BigDecimal(amountInPaise).divide(BigDecimal.valueOf(100));
        return currencySymbol + amountInRupees.toPlainString();
    }
}
