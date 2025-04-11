package com.webstore.service;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto dto);
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(Integer id);
    ProductResponseDto updateProduct(Integer id, ProductRequestDto dto);
    void deleteProduct(Integer id);
}
