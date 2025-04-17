package com.webstore.controller;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.service.ProductPriceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@Slf4j
@RestController
@RequestMapping("api/product-price")
public class ProductPriceController {

    private ProductPriceService productPriceService;

    @Autowired
    public void setProductPriceService(ProductPriceService productPriceService) {
        this.productPriceService = productPriceService;
    }

    @PostMapping
    public ResponseEntity<ProductPriceResponseDto> createProductPrice(@Valid @RequestBody ProductPriceRequestDto request) {
        log.info("Received request to create product price for productId={}, currencyId={}",
                request.getProductId(), request.getCurrencyId());

        ProductPriceResponseDto response = productPriceService.createProductPrice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> getProductPriceById(@PathVariable Integer id) {
        log.info("Fetching product price with id={}", id);
        return ResponseEntity.ok(productPriceService.getProductPriceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> updateProductPrice(
            @PathVariable Integer id,
            @RequestBody Long priceAmount) {

        if (priceAmount == null) {
            log.warn("Update failed: priceAmount is null for productPriceId={}", id);
            return ResponseEntity.badRequest().build();
        }

        log.info("Updating product price with id={} to new amount={}", id, priceAmount);
        return ResponseEntity.ok(productPriceService.updateProductPrice(id, BigInteger.valueOf(priceAmount)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductPrice(@PathVariable Integer id) {
        log.info("Deleting product price with id={}", id);
        productPriceService.deleteProductPrice(id);
        return ResponseEntity.noContent().build();
    }
}
