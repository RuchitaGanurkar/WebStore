package com.webstore.controller;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/product-price")
public class ProductPriceController {

    private final ProductPriceService productPriceService;

    @PostMapping
    public ResponseEntity<ProductPriceResponseDto> createProductPrice(@RequestBody ProductPriceRequestDto request) {
        log.info("Creating new product price");
        if (log.isDebugEnabled()) {
            log.debug("Product price request: productId={}, priceAmount={}",
                    request.getProductId(),
                    request.getPriceAmount());
        }

        ProductPriceResponseDto response = productPriceService.createProductPrice(request);
        log.info("Product price created successfully with ID: {}", response.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> getProductPriceById(@PathVariable Integer id) {
        log.info("Retrieving product price with ID: {}", id);

        ProductPriceResponseDto response = productPriceService.getProductPriceById(id);
        log.info("Retrieved product price for productId: {}", response.getProductId());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductPriceResponseDto> updateProductPrice(
            @PathVariable Integer id,
            @RequestBody Long priceAmount) {
        log.info("Updating product price with ID: {}, new price amount: {}", id, priceAmount);

        ProductPriceResponseDto response = productPriceService.updateProductPrice(id, priceAmount);
        log.info("Product price updated successfully for productId: {}", response.getProductId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductPrice(@PathVariable Integer id) {
        log.info("Deleting product price with ID: {}", id);

        productPriceService.deleteProductPrice(id);
        log.info("Product price with ID: {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}