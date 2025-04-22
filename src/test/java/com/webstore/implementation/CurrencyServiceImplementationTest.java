package com.webstore.implementation;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.entity.Currency;
import com.webstore.repository.CurrencyRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceImplementationTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyServiceImplementation currencyService;

    private Currency currency;
    private CurrencyRequestDto requestDto;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        // Set up test currency
        currency = new Currency();
        currency.setCurrencyId(1);
        currency.setCurrencyCode("USD");
        currency.setCurrencyName("US Dollar");
        currency.setCurrencySymbol("$");
        currency.setCreatedBy(TEST_USER);
        currency.setUpdatedBy(TEST_USER);
        currency.setCreatedAt(LocalDateTime.now());
        currency.setUpdatedAt(LocalDateTime.now());

        // Set up request DTO
        requestDto = new CurrencyRequestDto();
        requestDto.setCurrencyCode("USD");
        requestDto.setCurrencyName("US Dollar");
        requestDto.setCurrencySymbol("$");
    }

    @Test
    void getAllCurrencies_ShouldReturnListOfCurrencies() {
        // Arrange
        List<Currency> currencies = Arrays.asList(currency);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        when(currencyRepository.findAll(any(Pageable.class))).thenReturn(currencyPage);

        // Act
        List<CurrencyResponseDto> result = currencyService.getAllCurrencies(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(currency.getCurrencyId(), result.get(0).getCurrencyId());
        assertEquals(currency.getCurrencyCode(), result.get(0).getCurrencyCode());
        assertEquals(currency.getCurrencyName(), result.get(0).getCurrencyName());
        assertEquals(currency.getCurrencySymbol(), result.get(0).getCurrencySymbol());

        verify(currencyRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void getCurrencyById_WhenCurrencyExists_ShouldReturnCurrency() {
        // Arrange
        when(currencyRepository.findById(1)).thenReturn(Optional.of(currency));

        // Act
        CurrencyResponseDto result = currencyService.getCurrencyById(1);

        // Assert
        assertNotNull(result);
        assertEquals(currency.getCurrencyId(), result.getCurrencyId());
        assertEquals(currency.getCurrencyCode(), result.getCurrencyCode());

        verify(currencyRepository).findById(1);
    }

    @Test
    void getCurrencyById_WhenCurrencyDoesNotExist_ShouldThrowException() {
        // Arrange
        when(currencyRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> currencyService.getCurrencyById(99));
        verify(currencyRepository).findById(99);
    }

    @Test
    void createCurrency_WhenCurrencyDoesNotExist_ShouldCreateCurrency() {
        // Arrange
        when(currencyRepository.existsByCurrencyCode("USD")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUsername).thenReturn(TEST_USER);

            // Act
            CurrencyResponseDto result = currencyService.createCurrency(requestDto);

            // Assert
            assertNotNull(result);
            assertEquals(currency.getCurrencyId(), result.getCurrencyId());
            assertEquals(currency.getCurrencyCode(), result.getCurrencyCode());

            verify(currencyRepository).existsByCurrencyCode("USD");
            verify(currencyRepository).save(any(Currency.class));
        }
    }

    @Test
    void createCurrency_WhenCurrencyExists_ShouldThrowException() {
        // Arrange
        when(currencyRepository.existsByCurrencyCode("USD")).thenReturn(true);

        // Act & Assert
        assertThrows(EntityExistsException.class, () -> currencyService.createCurrency(requestDto));
        verify(currencyRepository).existsByCurrencyCode("USD");
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void updateCurrency_WhenCurrencyExists_ShouldUpdateCurrency() {
        // Arrange
        when(currencyRepository.findById(1)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        // Modify the request DTO to simulate an update
        requestDto.setCurrencyName("Updated US Dollar");

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUsername).thenReturn(TEST_USER);

            // Act
            CurrencyResponseDto result = currencyService.updateCurrency(1, requestDto);

            // Assert
            assertNotNull(result);
            assertEquals(currency.getCurrencyId(), result.getCurrencyId());
            assertEquals(currency.getCurrencyCode(), result.getCurrencyCode());

            verify(currencyRepository).findById(1);
            verify(currencyRepository).save(any(Currency.class));
        }
    }

    @Test
    void updateCurrency_WhenCurrencyDoesNotExist_ShouldThrowException() {
        // Arrange
        when(currencyRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> currencyService.updateCurrency(99, requestDto));
        verify(currencyRepository).findById(99);
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void updateCurrency_WhenNewCodeAlreadyExists_ShouldThrowException() {
        // Arrange
        Currency existingCurrency = new Currency();
        existingCurrency.setCurrencyId(1);
        existingCurrency.setCurrencyCode("USD");

        // New request with different code
        CurrencyRequestDto newCodeRequest = new CurrencyRequestDto();
        newCodeRequest.setCurrencyCode("EUR");
        newCodeRequest.setCurrencyName("Euro");
        newCodeRequest.setCurrencySymbol("€");

        when(currencyRepository.findById(1)).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.existsByCurrencyCode("EUR")).thenReturn(true);

        // Act & Assert
        assertThrows(EntityExistsException.class, () -> currencyService.updateCurrency(1, newCodeRequest));
        verify(currencyRepository).findById(1);
        verify(currencyRepository).existsByCurrencyCode("EUR");
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void deleteCurrency_WhenCurrencyExists_ShouldDeleteCurrency() {
        // Arrange
        when(currencyRepository.existsById(1)).thenReturn(true);
        doNothing().when(currencyRepository).deleteById(1);

        // Act
        currencyService.deleteCurrency(1);

        // Assert
        verify(currencyRepository).existsById(1);
        verify(currencyRepository).deleteById(1);
    }

    @Test
    void deleteCurrency_WhenCurrencyDoesNotExist_ShouldThrowException() {
        // Arrange
        when(currencyRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> currencyService.deleteCurrency(99));
        verify(currencyRepository).existsById(99);
        verify(currencyRepository, never()).deleteById(99);
    }

    @Test
    void getCurrencyByCode_WhenCurrencyExists_ShouldReturnCurrency() {
        // Arrange
        when(currencyRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(currency));

        // Act
        CurrencyResponseDto result = currencyService.getCurrencyByCode("USD");

        // Assert
        assertNotNull(result);
        assertEquals(currency.getCurrencyId(), result.getCurrencyId());
        assertEquals(currency.getCurrencyCode(), result.getCurrencyCode());

        verify(currencyRepository).findByCurrencyCode("USD");
    }

    @Test
    void getCurrencyByCode_WhenCurrencyDoesNotExist_ShouldThrowException() {
        // Arrange
        when(currencyRepository.findByCurrencyCode("XXX")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> currencyService.getCurrencyByCode("XXX"));
        verify(currencyRepository).findByCurrencyCode("XXX");
    }
}