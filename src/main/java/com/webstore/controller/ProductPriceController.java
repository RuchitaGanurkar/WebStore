package com.webstore.controller;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/product-price")
public class ProductPriceController {

    private final ProductPriceService productPriceService;

    @PostMapping
    public ResponseEntity<ProductPriceResponseDto> createProductPrice(@RequestBody ProductPriceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productPriceService.createProductPrice(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> getProductPriceById(@PathVariable Integer id) {
        return ResponseEntity.ok(productPriceService.getProductPriceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> updateProductPrice(
            @PathVariable Integer id,
            @RequestBody Long priceAmount) {
         return ResponseEntity.ok(productPriceService.updateProductPrice(id, BigInteger.valueOf(priceAmount)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductPrice(@PathVariable Integer id) {
        productPriceService.deleteProductPrice(id);
        return ResponseEntity.noContent().build();
    }
}