package com.webstore.controller.product;

import com.webstore.dto.request.product.CurrencyRequestDto;
import com.webstore.dto.response.product.CurrencyResponseDto;
import com.webstore.service.product.CurrencyService;
import jakarta.validation.Valid;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
@RestController
@RequestMapping("api/currencies")
public class CurrencyController {

    private CurrencyService currencyService;

    @Autowired
    @Qualifier("currencyServiceImplementation") // Specify exact implementation
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDto>> getAllCurrencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<CurrencyResponseDto> currencies = currencyService.getAllCurrencies(page, size);
        if (currencies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> getCurrencyById(@PathVariable Integer id) {
        CurrencyResponseDto currency = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(currency);
    }

    @PostMapping
    public ResponseEntity<CurrencyResponseDto> createCurrency(
            @RequestBody @Valid CurrencyRequestDto currencyDto) {
        CurrencyResponseDto createdCurrency = currencyService.createCurrency(currencyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> updateCurrency(
            @PathVariable Integer id,
            @RequestBody @Valid CurrencyRequestDto currencyDto) {
        CurrencyResponseDto updatedCurrency = currencyService.updateCurrency(id, currencyDto);
        return ResponseEntity.ok(updatedCurrency);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCurrency(@PathVariable Integer id) {
        String message = currencyService.deleteCurrency(id);
        return ResponseEntity.ok(message);
    }
}
