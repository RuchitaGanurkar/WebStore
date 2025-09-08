package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartProductHistoryRequestDto;
import com.webstore.dto.response.cart.CartProductHistoryResponseDto;
import java.util.List;

public interface CartProductHistoryService {

    CartProductHistoryResponseDto createCartProductHistory(CartProductHistoryRequestDto dto);

    List<CartProductHistoryResponseDto> getAllCartProductHistory();

    CartProductHistoryResponseDto getCartProductHistoryById(Long id);

    CartProductHistoryResponseDto updateCartProductHistory(Long id, CartProductHistoryRequestDto dto);

    void deleteCartProductHistory(Long id);

    List<CartProductHistoryResponseDto> getCartProductHistoriesByProductPriceId(Integer productPriceId);

}
