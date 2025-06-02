package com.webstore.service;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;

import java.util.List;

public interface CurrencyService {

    List<CurrencyResponseDto> getAllCurrencies(int page, int size);
    CurrencyResponseDto getCurrencyById(Integer id);
    CurrencyResponseDto getCurrencyByCode(String currencyCode);

    CurrencyResponseDto createCurrency(CurrencyRequestDto currencyDto);

    CurrencyResponseDto updateCurrency(Integer id, CurrencyRequestDto currencyDto);

    void deleteCurrency(Integer id);

}