package com.webstore.service;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.entity.Currency;
import com.webstore.entity.Product;
import com.webstore.entity.ProductPrice;
import com.webstore.repository.CurrencyRepository;
import com.webstore.repository.ProductPriceRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductPriceServiceImpl implements ProductPriceService {

    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    public ProductPriceResponseDto createProductPrice(ProductPriceRequestDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new RuntimeException("Currency not found"));

        ProductPrice price = ProductPrice.builder()
                .product(product)
                .currency(currency)
                .priceAmount(request.getPriceAmount())
                .build();

        ProductPrice saved = productPriceRepository.save(price);
        return mapToDto(saved);
    }

    @Override
    public ProductPriceResponseDto updateProductPrice(Integer id, ProductPriceRequestDto request) {
        ProductPrice price = productPriceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductPrice not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new RuntimeException("Currency not found"));

        price.setProduct(product);
        price.setCurrency(currency);
        price.setPriceAmount(request.getPriceAmount());

        return mapToDto(productPriceRepository.save(price));
    }

    @Override
    public void deleteProductPrice(Integer id) {
        productPriceRepository.deleteById(id);
    }

    @Override
    public ProductPriceResponseDto getById(Integer id) {
        ProductPrice price = productPriceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductPrice not found"));

        return mapToDto(price);
    }

    @Override
    public List<ProductPriceResponseDto> getAll() {
        return productPriceRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductPriceResponseDto mapToDto(ProductPrice price) {
        ProductPriceResponseDto dto = new ProductPriceResponseDto();
        dto.setProductPriceId(price.getProductPriceId());
        dto.setProductId(price.getProduct().getProductId());
        dto.setCurrencyId(price.getCurrency().getCurrencyId());
        dto.setPriceAmount(price.getPriceAmount());
        dto.setCreatedBy(price.getCreatedBy());
        dto.setUpdatedBy(price.getUpdatedBy());
        return dto;
    }
}
