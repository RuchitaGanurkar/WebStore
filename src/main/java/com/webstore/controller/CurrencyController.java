package com.webstore.controller;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.service.CurrencyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

//  page=0, size=2 gives you currencies 1-5
//  page=1, size=2 gives you currencies 6-10
//  page=2, size=2 gives you currencies 11-15

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDto>> getAllCurrencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<CurrencyResponseDto> currencies = currencyService.getAllCurrencies(page, size);
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> getCurrencyById(@PathVariable Integer id) {
        return ResponseEntity.ok(currencyService.getCurrencyById(id));
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
    public ResponseEntity<Void> deleteCurrency(@PathVariable Integer id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }
}