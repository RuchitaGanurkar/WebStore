package com.webstore.implementation;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.entity.Currency;
import com.webstore.entity.Product;
import com.webstore.entity.ProductPrice;
import com.webstore.repository.*;
import com.webstore.service.ProductPriceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Slf4j
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
        log.info("Creating product price for productId={} and currencyId={}", request.getProductId(), request.getCurrencyId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + request.getProductId()));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with id: " + request.getCurrencyId()));

        Optional<ProductPrice> existingPrice = productPriceRepository
                .findByProductProductIdAndCurrencyCurrencyId(request.getProductId(), request.getCurrencyId());

        if (existingPrice.isPresent()) {
            throw new IllegalArgumentException("Price already exists for this product and currency combination");
        }

        if (request.getPriceAmount() == null || request.getPriceAmount().compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount must be non-null and non-negative");
        }

        ProductPrice productPrice = new ProductPrice();
        productPrice.setProduct(product);
        productPrice.setCurrency(currency);
        productPrice.setPriceAmount(request.getPriceAmount());

        ProductPrice savedProductPrice = productPriceRepository.save(productPrice);
        log.info("Product price created with id={}", savedProductPrice.getProductPriceId());

        return mapToResponseDto(savedProductPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPriceResponseDto getProductPriceById(Integer id) {
        log.info("Fetching product price with id={}", id);

        ProductPrice productPrice = productPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product price not found with id: " + id));

        return mapToResponseDto(productPrice);
    }

    @Override
    @Transactional
    public ProductPriceResponseDto updateProductPrice(Integer id, BigInteger priceAmount) {
        log.info("Updating product price with id={} to new amount={}", id, priceAmount);

        if (priceAmount == null || priceAmount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount must be non-null and non-negative");
        }

        ProductPrice productPrice = productPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product price not found with id: " + id));

        productPrice.setPriceAmount(priceAmount);
        ProductPrice updatedProductPrice = productPriceRepository.save(productPrice);

        log.info("Updated price for productPriceId={} successfully", id);
        return mapToResponseDto(updatedProductPrice);
    }


    @Override
    @Transactional
    public void deleteProductPrice(Integer id) {
        if (!productPriceRepository.existsById(id)) {
            throw new EntityNotFoundException("Product price not found with id: " + id);
        }

        productPriceRepository.deleteById(id);

        log.info("Product price with id={} has been deleted", id);
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
        log.info("Formatted price for display: {}", formattedPrice);
        responseDto.setFormattedPrice(formattedPrice);

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
