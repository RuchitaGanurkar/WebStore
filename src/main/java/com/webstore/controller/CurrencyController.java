package com.webstore.controller;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDto>> getAllCurrencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        log.info("Retrieving currencies - page: {}, size: {}", page, size);

        List<CurrencyResponseDto> currencies = currencyService.getAllCurrencies(page, size);
        log.info("Retrieved {} currencies", currencies.size());

        if (log.isDebugEnabled() && !currencies.isEmpty()) {
            log.debug("Retrieved currencies: {}",
                    currencies.stream()
                            .map(c -> c.getCurrencyCode() + "(" + c.getCurrencyName() + ")")
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }

        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> getCurrencyById(@PathVariable Integer id) {
        log.info("Retrieving currency with ID: {}", id);

        CurrencyResponseDto currency = currencyService.getCurrencyById(id);
        log.info("Retrieved currency: {} ({})", currency.getCurrencyName(), currency.getCurrencyCode());

        return ResponseEntity.ok(currency);
    }

    @PostMapping
    public ResponseEntity<CurrencyResponseDto> createCurrency(
            @RequestBody @Valid CurrencyRequestDto currencyDto) {
        log.info("Creating new currency: {} ({})", currencyDto.getCurrencyName(), currencyDto.getCurrencyCode());

        CurrencyResponseDto createdCurrency = currencyService.createCurrency(currencyDto);
        log.info("Currency created successfully with ID: {}", createdCurrency.getCurrencyId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> updateCurrency(
            @PathVariable Integer id,
            @RequestBody @Valid CurrencyRequestDto currencyDto) {
        log.info("Updating currency with ID: {}", id);
        log.debug("Update details: name={}, code={}, symbol={}",
                currencyDto.getCurrencyName(), currencyDto.getCurrencyCode(), currencyDto.getCurrencySymbol());

        CurrencyResponseDto updatedCurrency = currencyService.updateCurrency(id, currencyDto);
        log.info("Currency with ID: {} updated successfully", id);

        return ResponseEntity.ok(updatedCurrency);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Integer id) {
        log.info("Deleting currency with ID: {}", id);

        currencyService.deleteCurrency(id);
        log.info("Currency with ID: {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}