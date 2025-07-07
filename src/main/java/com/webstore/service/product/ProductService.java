package com.webstore.service.product;

import com.webstore.dto.request.product.ProductRequestDto;
import com.webstore.dto.response.product.ProductResponseDto;

import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto);

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto updateProduct(Integer id, ProductRequestDto dto);

    ProductResponseDto getProductById(Integer id);

    void deleteProduct(Integer id);

//    @Transactional
//    void deleteProductById(Integer id);
}
