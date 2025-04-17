package com.webstore.controller;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Valid ProductRequestDto dto) {
        log.info("Creating new product: name={}, categoryId={}", dto.getProductName(), dto.getCategoryId());

        ProductResponseDto createdProduct = productService.createProduct(dto);
        log.info("Product created successfully with ID: {}", createdProduct.getProductId());

        if (log.isDebugEnabled()) {
            log.debug("Created product details: name={}, description={}",
                    createdProduct.getProductName(),
                    createdProduct.getProductDescription());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        log.info("Retrieving all products");

        List<ProductResponseDto> products = productService.getAllProducts();
        log.info("Retrieved {} products", products.size());

        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Integer id,
                                                            @RequestBody @Valid ProductRequestDto dto) {
        log.info("Updating product with ID: {}", id);
        if (log.isDebugEnabled()) {
            log.debug("Update details: name={}", dto.getProductName());
        }

        ProductResponseDto updated = productService.updateProduct(id, dto);
        log.info("Product with ID: {} updated successfully", id);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Integer id) {
        log.info("Retrieving product with ID: {}", id);

        ProductResponseDto product = productService.getProductById(id);
        log.info("Retrieved product: name={}", product.getProductName());

        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        log.info("Deleting product with ID: {}", id);

        productService.deleteProduct(id);
        log.info("Product with ID: {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}