package com.webstore.service.product;

import com.webstore.dto.request.product.ProductPriceRequestDto;
import com.webstore.dto.response.product.ProductPriceResponseDto;

import java.math.BigInteger;

public interface ProductPriceService {

    ProductPriceResponseDto createProductPrice(ProductPriceRequestDto request);

    ProductPriceResponseDto getProductPriceById(Integer id);

    ProductPriceResponseDto updateProductPrice(Integer id, BigInteger priceAmount);

    void deleteProductPrice(Integer id);
}
