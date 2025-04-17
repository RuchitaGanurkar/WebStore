package com.webstore.implementation;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.entity.Currency;
import com.webstore.repository.CurrencyRepository;
import com.webstore.service.CurrencyService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CurrencyService interface.
 *
 * Uses setter injection for dependencies following best practices.
 * Exception handling is standardized to use specific exception types
 * that will be caught by the GlobalExceptionHandler.
 */

@Setter
@RequiredArgsConstructor
@Service
public class CurrencyServiceImplementation implements CurrencyService {

    private CurrencyRepository currencyRepository;


    @Override
    public List<CurrencyResponseDto> getAllCurrencies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Currency> currencyPage = currencyRepository.findAll(pageable);

        return currencyPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyResponseDto getCurrencyById(Integer id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with id: " + id));
        return mapToResponseDto(currency);
    }

    @Override
    public CurrencyResponseDto createCurrency(CurrencyRequestDto currencyDto) {
        // Check if currency code already exists
        if (currencyRepository.existsByCurrencyCode(currencyDto.getCurrencyCode())) {
            throw new EntityExistsException("Currency with code " + currencyDto.getCurrencyCode() + " already exists");
        }

        Currency currency = new Currency();
        currency.setCurrencyCode(currencyDto.getCurrencyCode().toUpperCase());
        currency.setCurrencyName(currencyDto.getCurrencyName());
        currency.setCurrencySymbol(currencyDto.getCurrencySymbol());

        Currency savedCurrency = currencyRepository.save(currency);
        return mapToResponseDto(savedCurrency);
    }

    @Override
    public CurrencyResponseDto updateCurrency(Integer id, CurrencyRequestDto currencyDto) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with id: " + id));

        // Check if the new currency code already exists (if it's being changed)
        if (!currency.getCurrencyCode().equals(currencyDto.getCurrencyCode()) &&
                currencyRepository.existsByCurrencyCode(currencyDto.getCurrencyCode())) {
            throw new EntityExistsException("Currency with code " + currencyDto.getCurrencyCode() + " already exists");
        }

        currency.setCurrencyCode(currencyDto.getCurrencyCode().toUpperCase());
        currency.setCurrencyName(currencyDto.getCurrencyName());
        currency.setCurrencySymbol(currencyDto.getCurrencySymbol());

        Currency updatedCurrency = currencyRepository.save(currency);
        return mapToResponseDto(updatedCurrency);
    }

    @Override
    public void deleteCurrency(Integer id) {
        if (!currencyRepository.existsById(id)) {
            throw new EntityNotFoundException("Currency not found with id: " + id);
        }
        currencyRepository.deleteById(id);
    }

    @Override
    public CurrencyResponseDto getCurrencyByCode(String currencyCode) {
        Currency currency = currencyRepository.findByCurrencyCode(currencyCode.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with code: " + currencyCode));

        return mapToResponseDto(currency);
    }

    private CurrencyResponseDto mapToResponseDto(Currency currency) {
        CurrencyResponseDto responseDto = new CurrencyResponseDto();
        responseDto.setCurrencyId(currency.getCurrencyId());
        responseDto.setCurrencyCode(currency.getCurrencyCode());
        responseDto.setCurrencyName(currency.getCurrencyName());
        responseDto.setCurrencySymbol(currency.getCurrencySymbol());
        responseDto.setCreatedAt(currency.getCreatedAt());
        responseDto.setCreatedBy(currency.getCreatedBy());
        responseDto.setUpdatedAt(currency.getUpdatedAt());
        responseDto.setUpdatedBy(currency.getUpdatedBy());

        return responseDto;
    }
}