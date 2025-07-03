package com.webstore.implementation;

import com.webstore.dto.request.product.CurrencyRequestDto;
import com.webstore.dto.response.product.CurrencyResponseDto;
import com.webstore.entity.product.Currency;
import com.webstore.implementation.product.CurrencyServiceImplementation;
import com.webstore.repository.product.CurrencyRepository;
import com.webstore.util.AuthUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceImplementationTest {

    @Mock private CurrencyRepository currencyRepository;
    @InjectMocks private CurrencyServiceImplementation currencyService;

    private Currency currency;
    private CurrencyRequestDto requestDto;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        currency = new Currency();
        currency.setCurrencyId(1);
        currency.setCurrencyCode("USD");
        currency.setCurrencyName("US Dollar");
        currency.setCurrencySymbol("$");
        currency.setCreatedBy(TEST_USER);
        currency.setUpdatedBy(TEST_USER);
        currency.setCreatedAt(LocalDateTime.now());
        currency.setUpdatedAt(LocalDateTime.now());

        requestDto = new CurrencyRequestDto();
        requestDto.setCurrencyCode("USD");
        requestDto.setCurrencyName("US Dollar");
        requestDto.setCurrencySymbol("$");
    }

    @Test
    void getAllCurrencies_ShouldReturnListOfCurrencies() {
        Page<Currency> currencyPage = new PageImpl<>(List.of(currency));
        when(currencyRepository.findAll(any(Pageable.class))).thenReturn(currencyPage);

        List<CurrencyResponseDto> result = currencyService.getAllCurrencies(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getCurrencyCode());

        verify(currencyRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void getCurrencyById_WhenExists_ShouldReturnCurrency() {
        when(currencyRepository.findById(1)).thenReturn(Optional.of(currency));

        CurrencyResponseDto result = currencyService.getCurrencyById(1);

        assertNotNull(result);
        assertEquals(1, result.getCurrencyId());
        assertEquals("USD", result.getCurrencyCode());

        verify(currencyRepository).findById(1);
    }

    @Test
    void getCurrencyById_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> currencyService.getCurrencyById(99));
        verify(currencyRepository).findById(99);
    }

    @Test
    void createCurrency_WhenNotExists_ShouldCreateCurrency() {
        when(currencyRepository.existsByCurrencyCode("USD")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUsername).thenReturn(TEST_USER);

            CurrencyResponseDto result = currencyService.createCurrency(requestDto);

            assertNotNull(result);
            assertEquals("USD", result.getCurrencyCode());
            verify(currencyRepository).existsByCurrencyCode("USD");
            verify(currencyRepository).save(any(Currency.class));
        }
    }

    @Test
    void createCurrency_WhenAlreadyExists_ShouldThrowException() {
        when(currencyRepository.existsByCurrencyCode("USD")).thenReturn(true);

        assertThrows(EntityExistsException.class, () -> currencyService.createCurrency(requestDto));

        verify(currencyRepository).existsByCurrencyCode("USD");
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void updateCurrency_WhenExists_ShouldUpdateCurrency() {
        when(currencyRepository.findById(1)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        requestDto.setCurrencyName("Updated Dollar");

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUsername).thenReturn(TEST_USER);

            CurrencyResponseDto result = currencyService.updateCurrency(1, requestDto);

            assertEquals("USD", result.getCurrencyCode());
            assertEquals("Updated Dollar", result.getCurrencyName());

            verify(currencyRepository).findById(1);
            verify(currencyRepository).save(any(Currency.class));
        }
    }

    @Test
    void updateCurrency_WhenCurrencyNotFound_ShouldThrowException() {
        when(currencyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> currencyService.updateCurrency(99, requestDto));

        verify(currencyRepository).findById(99);
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void updateCurrency_WhenNewCodeExists_ShouldThrowException() {
        Currency existingCurrency = new Currency();
        existingCurrency.setCurrencyId(1);
        existingCurrency.setCurrencyCode("USD");

        CurrencyRequestDto newCodeDto = new CurrencyRequestDto("EUR", "Euro", "€");

        when(currencyRepository.findById(1)).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.existsByCurrencyCode("EUR")).thenReturn(true);

        assertThrows(EntityExistsException.class, () -> currencyService.updateCurrency(1, newCodeDto));

        verify(currencyRepository).existsByCurrencyCode("EUR");
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void deleteCurrency_WhenExists_ShouldDelete() {
        when(currencyRepository.existsById(1)).thenReturn(true);
        doNothing().when(currencyRepository).deleteById(1);

        currencyService.deleteCurrency(1);

        verify(currencyRepository).existsById(1);
        verify(currencyRepository).deleteById(1);
    }

    @Test
    void deleteCurrency_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.existsById(99)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> currencyService.deleteCurrency(99));

        verify(currencyRepository).existsById(99);
        verify(currencyRepository, never()).deleteById(99);
    }

    @Test
    void getCurrencyByCode_WhenExists_ShouldReturnCurrency() {
        when(currencyRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(currency));

        CurrencyResponseDto result = currencyService.getCurrencyByCode("USD");

        assertNotNull(result);
        assertEquals("USD", result.getCurrencyCode());

        verify(currencyRepository).findByCurrencyCode("USD");
    }

    @Test
    void getCurrencyByCode_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findByCurrencyCode("XXX")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> currencyService.getCurrencyByCode("XXX"));
        verify(currencyRepository).findByCurrencyCode("XXX");
    }
}
