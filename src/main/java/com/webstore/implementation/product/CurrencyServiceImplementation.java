package com.webstore.implementation.product;

import com.webstore.dto.request.product.CurrencyRequestDto;
import com.webstore.dto.response.product.CurrencyResponseDto;
import com.webstore.entity.product.Currency;
import com.webstore.repository.product.CurrencyRepository;
import com.webstore.service.product.CurrencyService;
import com.webstore.util.AuthUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("currencyServiceImplementation") // Qualifier name used in Controller
public class CurrencyServiceImplementation implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyServiceImplementation(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

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
        if (currencyRepository.existsByCurrencyCode(currencyDto.getCurrencyCode())) {
            throw new EntityExistsException("Currency with code " + currencyDto.getCurrencyCode() + " already exists");
        }

        Currency currency = new Currency();
        currency.setCurrencyCode(currencyDto.getCurrencyCode().toUpperCase());
        currency.setCurrencyName(currencyDto.getCurrencyName());
        currency.setCurrencySymbol(currencyDto.getCurrencySymbol());

        String currentUser = AuthUtils.getCurrentUsername();
        currency.setCreatedBy(currentUser);
        currency.setUpdatedBy(currentUser);

        return mapToResponseDto(currencyRepository.save(currency));
    }

    @Override
    public CurrencyResponseDto updateCurrency(Integer id, CurrencyRequestDto currencyDto) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with id: " + id));

        if (!currency.getCurrencyCode().equalsIgnoreCase(currencyDto.getCurrencyCode()) &&
                currencyRepository.existsByCurrencyCode(currencyDto.getCurrencyCode())) {
            throw new EntityExistsException("Currency with code " + currencyDto.getCurrencyCode() + " already exists");
        }

        currency.setCurrencyCode(currencyDto.getCurrencyCode().toUpperCase());
        currency.setCurrencyName(currencyDto.getCurrencyName());
        currency.setCurrencySymbol(currencyDto.getCurrencySymbol());
        currency.setUpdatedBy(AuthUtils.getCurrentUsername());

        return mapToResponseDto(currencyRepository.save(currency));
    }

    @Override
    public String deleteCurrency(Integer id) {
        if (!currencyRepository.existsById(id)) {
            throw new EntityNotFoundException("Currency not found with id: " + id);
        }
        currencyRepository.deleteById(id);
        return "Currency deleted successfully";
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

