package com.webstore.implementation.product;

import com.webstore.dto.request.product.CategoryRequestDto;
import com.webstore.dto.request.product.ProductRequestDto;
import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.entity.product.Category;
import com.webstore.entity.product.Product;
import com.webstore.repository.product.CategoryRepository;
import com.webstore.repository.product.ProductRepository;
import com.webstore.service.product.ProductService;
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
@RequiredArgsConstructor
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product with name: {}", dto.getProductName());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + dto.getCategoryId()));

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

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
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Integer id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        return convertToDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Integer id, ProductRequestDto dto) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + dto.getCategoryId()));

        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        log.info("Product with ID: {} updated successfully", id);

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

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
            dto.setCategory(categoryDto);
        }

        return dto;
    }
}
