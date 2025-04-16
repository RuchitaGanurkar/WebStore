package com.webstore.service;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.ProductResponseDto;

import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto);

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto updateProduct(Integer id, ProductRequestDto dto);

    ProductResponseDto getProductById(Integer id);

    void deleteProduct(Integer id);
}
