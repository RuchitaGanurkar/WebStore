package com.webstore.service;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;

import java.util.List;

public interface ProductPriceService {
    ProductPriceResponseDto createProductPrice(ProductPriceRequestDto request);
    ProductPriceResponseDto updateProductPrice(Integer id, ProductPriceRequestDto request);
    void deleteProductPrice(Integer id);
    ProductPriceResponseDto getById(Integer id);
    List<ProductPriceResponseDto> getAll();
}
