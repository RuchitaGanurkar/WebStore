package com.webstore.implementation;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.Category;
import com.webstore.entity.Product;
import com.webstore.repository.CategoryRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service

public class ProductServiceImplementation implements ProductService {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    @Override
    @Transactional

    public void deleteProductById(Integer id) {
        log.info("Deleting product by ID (deleteProductById): {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        productRepository.delete(product);

        log.info("Product with ID: {} has been deleted via deleteProductById", id);
    }

    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product with name: {}", dto.getProductName());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCategory(category);
//        product.setCreatedBy(dto.getCreatedBy());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
//        product.setUpdatedBy(dto.getCreatedBy());

        Product saved = productRepository.save(product);
        log.info("Product created with ID: {}", saved.getProductId());

        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Integer id, ProductRequestDto dto) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());
//        product.setUpdatedBy(dto.getCreatedBy());

        Product updated = productRepository.save(product);
        log.info("Product with ID: {} updated successfully", id);

        return convertToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Integer id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        return convertToDto(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        productRepository.delete(product);

        log.info("Product with ID: {} has been deleted", id);
    }

    private ProductResponseDto convertToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());

        if (product.getCategory() != null) {
            Category category = product.getCategory();
            CategoryRequestDto categoryDto = new CategoryRequestDto();
            categoryDto.setCategoryId(category.getCategoryId());
            categoryDto.setCategoryName(category.getCategoryName());
            categoryDto.setCategoryDescription(category.getCategoryDescription());
//            categoryDto.setCreatedBy(category.getCreatedBy());
            dto.setCategory(categoryDto);
        }

        return dto;
    }
}
