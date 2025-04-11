package com.webstore.controller;

import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping
    public ResponseEntity<CurrencyResponseDto> create(@Valid @RequestBody CurrencyRequestDto request) {
        return ResponseEntity.ok(currencyService.createCurrency(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> update(@PathVariable Integer id,
                                                      @Valid @RequestBody CurrencyRequestDto request) {
        return ResponseEntity.ok(currencyService.updateCurrency(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(currencyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDto>> getAll() {
        return ResponseEntity.ok(currencyService.getAll());
    }
}
