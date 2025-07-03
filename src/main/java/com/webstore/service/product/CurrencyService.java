package com.webstore.service.product;

import com.webstore.dto.request.product.CurrencyRequestDto;
import com.webstore.dto.response.product.CurrencyResponseDto;

import java.util.List;

public interface CurrencyService {

    List<CurrencyResponseDto> getAllCurrencies(int page, int size);
    CurrencyResponseDto getCurrencyById(Integer id);
    CurrencyResponseDto getCurrencyByCode(String currencyCode);

    CurrencyResponseDto createCurrency(CurrencyRequestDto currencyDto);

    CurrencyResponseDto updateCurrency(Integer id, CurrencyRequestDto currencyDto);

    String deleteCurrency(Integer id);

}