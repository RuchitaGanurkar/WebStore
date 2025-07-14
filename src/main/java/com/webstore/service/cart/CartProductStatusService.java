package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartProductStatusRequestDto;
import com.webstore.dto.response.cart.CartProductStatusResponseDto;

import java.util.List;

public interface CartProductStatusService {
    CartProductStatusResponseDto getCartProductStatusById(Integer statusId);
    List<CartProductStatusResponseDto> getAllCartProductStatuses();
    CartProductStatusResponseDto updateCartProductStatus(Integer statusId, CartProductStatusRequestDto requestDto);
    void deleteCartProductStatus(Integer statusId);
 }
