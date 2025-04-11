package com.webstore.service;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;

import java.util.List;

public interface CurrencyService {
    CurrencyResponseDto createCurrency(CurrencyRequestDto request);
    CurrencyResponseDto updateCurrency(Integer id, CurrencyRequestDto request);
    void deleteCurrency(Integer id);
    CurrencyResponseDto getById(Integer id);
    List<CurrencyResponseDto> getAll();
}
