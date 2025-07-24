package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;

import java.util.List;

public interface CartProductService {

    CartProductResponseDto createCartProduct(CartProductRequestDto requestDto);

    CartProductResponseDto getCartProductById(Long id);

    List<CartProductResponseDto> getCartProductsByCartId(Long cartId);

    List<CartProductResponseDto> getActiveCartProductsByPhoneNumber(String phoneNumber);

    Long countActiveCartProducts(Long cartId);

    void deleteCartProduct(Long id);
}
