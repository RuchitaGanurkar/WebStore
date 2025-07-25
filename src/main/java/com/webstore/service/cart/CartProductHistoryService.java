package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public interface CartProductHistoryService {

    CartProductHistoryResponseDto createCartProductHistory(CartProductHistoryRequestDto requestDto);

    CartProductHistoryResponseDto getCartProductHistoryById(Long id);

    List<CartProductHistoryResponseDto> getHistoryByCartProductId(Long cartProductId);

    CartProductHistoryResponseDto getLatestHistoryByCartProductId(Long cartProductId);

    List<CartProductHistoryResponseDto> getHistoryByProductId(Integer productId);

    void deleteCartProductHistory(Long id);

    CartProductHistoryResponseDto updateCartProductHistory(@NotNull @Positive Long id, @Valid CartProductHistoryRequestDto dto);

    List<CartProductHistoryResponseDto> getAllCartProductHistory();
}