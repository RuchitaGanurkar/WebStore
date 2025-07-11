package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartStatusRequestDto;
import com.webstore.dto.response.cart.CartStatusResponseDto;

import java.util.List;

public interface CartStatusService {
    CartStatusResponseDto getCartStatusById(Integer statusId);
    List<CartStatusResponseDto> getAllCartStatuses();
    CartStatusResponseDto createCartStatus(CartStatusRequestDto requestDto);
    CartStatusResponseDto updateCartStatus(Integer statusId, CartStatusRequestDto requestDto);
    void deleteCartStatus(Integer statusId);
    CartStatusResponseDto getCartStatusByName(String statusName);
}