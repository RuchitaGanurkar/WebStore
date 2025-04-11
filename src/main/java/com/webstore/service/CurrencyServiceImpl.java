package com.webstore.service;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.entity.Currency;
import com.webstore.repository.CurrencyRepository;
import com.webstore.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    public CurrencyResponseDto createCurrency(CurrencyRequestDto request) {
        Currency currency = Currency.builder()
                .currencyCode(request.getCurrencyCode())
                .currencyName(request.getCurrencyName())
                .currencySymbol(request.getCurrencySymbol())
                .build();
        return mapToDto(currencyRepository.save(currency));
    }

    @Override
    public CurrencyResponseDto updateCurrency(Integer id, CurrencyRequestDto request) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));

        currency.setCurrencyCode(request.getCurrencyCode());
        currency.setCurrencyName(request.getCurrencyName());
        currency.setCurrencySymbol(request.getCurrencySymbol());

        return mapToDto(currencyRepository.save(currency));
    }

    @Override
    public void deleteCurrency(Integer id) {
        currencyRepository.deleteById(id);
    }

    @Override
    public CurrencyResponseDto getById(Integer id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        return mapToDto(currency);
    }

    @Override
    public List<CurrencyResponseDto> getAll() {
        return currencyRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CurrencyResponseDto mapToDto(Currency currency) {
        CurrencyResponseDto dto = new CurrencyResponseDto();
        dto.setCurrencyId(currency.getCurrencyId());
        dto.setCurrencyCode(currency.getCurrencyCode());
        dto.setCurrencyName(currency.getCurrencyName());
        dto.setCurrencySymbol(currency.getCurrencySymbol());
        dto.setCreatedBy(currency.getCreatedBy());
        dto.setUpdatedBy(currency.getUpdatedBy());
        return dto;
    }
}
