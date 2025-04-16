package com.webstore.service;


import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;

public interface ProductPriceService {

    ProductPriceResponseDto createProductPrice(ProductPriceRequestDto request);

    ProductPriceResponseDto getProductPriceById(Integer id);

    ProductPriceResponseDto updateProductPrice(Integer id, Long priceAmount);

    void deleteProductPrice(Integer id);

}
