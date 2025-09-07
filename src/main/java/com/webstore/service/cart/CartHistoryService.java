package com.webstore.service.cart;

import com.webstore.dto.request.cart.CartHistoryRequestDto;
import com.webstore.dto.response.cart.CartHistoryResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CartHistoryService {

    CartHistoryResponseDto createCartHistory(CartHistoryRequestDto requestDto);

    CartHistoryResponseDto getCartHistoryById(Long id);

    List<CartHistoryResponseDto> getCartHistoriesByCartId(Long cartId);

    List<CartHistoryResponseDto> getCartHistoriesByCreatedBy(String createdBy);

    List<CartHistoryResponseDto> getCartHistoriesByUpdatedBy(String updatedBy);

    List<CartHistoryResponseDto> getCartHistoriesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<CartHistoryResponseDto> getCartHistoriesByOldStatus(Integer oldStatusId);

    List<CartHistoryResponseDto> getCartHistoriesByNewStatus(Integer newStatusId);

}






