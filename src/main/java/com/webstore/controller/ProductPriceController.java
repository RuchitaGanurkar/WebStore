package com.webstore.controller;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.service.ProductPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-prices")
@RequiredArgsConstructor
public class ProductPriceController {

    private final ProductPriceService productPriceService;

    @PostMapping
    public ResponseEntity<ProductPriceResponseDto> create(@Valid @RequestBody ProductPriceRequestDto request) {
        return ResponseEntity.ok(productPriceService.createProductPrice(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> update(@PathVariable Integer id,
                                                          @Valid @RequestBody ProductPriceRequestDto request) {
        return ResponseEntity.ok(productPriceService.updateProductPrice(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productPriceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductPriceResponseDto>> getAll() {
        return ResponseEntity.ok(productPriceService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productPriceService.deleteProductPrice(id);
        return ResponseEntity.noContent().build();
    }
}
