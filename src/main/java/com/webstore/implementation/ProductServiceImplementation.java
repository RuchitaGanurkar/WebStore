package com.webstore.implementation;

import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.Product;
import com.webstore.repository.ProductRepository;
import com.webstore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductServiceImplementation implements ProductService {
        private final ProductRepository productRepository;

        @Override
        @Transactional(readOnly = true)

        public List<ProductResponseDto> getAllProducts() {

            List<Product> products = productRepository.findAll();

            if (products.isEmpty()) {
                System.out.println("No products found in the database");
                return Collections.emptyList();
            }

            List<ProductResponseDto> responseDtos = new ArrayList<>();

            for (Product product : products) {
                ProductResponseDto dto = new ProductResponseDto();
                dto.setProductId(product.getProductId());
                dto.setProductName(product.getProductName());
                dto.setProductDescription(product.getProductDescription());
                dto.setCreatedAt(product.getCreatedAt());
                dto.setCreatedBy(product.getCreatedBy());
                dto.setUpdatedAt(product.getUpdatedAt());
                dto.setUpdatedBy(product.getUpdatedBy());
                dto.setPrices(new ArrayList<>());

                responseDtos.add(dto);
            }

            System.out.println("Fetching All Products");
            for (ProductResponseDto dto : responseDtos) {
                System.out.println("  {");
                System.out.println("    \"productId\": " + dto.getProductId() + ",");
                System.out.println("    \"productName\": \"" + dto.getProductName() + "\",");
                System.out.println("    \"productDescription\": \"" + dto.getProductDescription() + "\",");
                System.out.println("    \"createdAt\": \"" + dto.getCreatedAt() + "\",");
                System.out.println("    \"createdBy\": \"" + dto.getCreatedBy() + "\",");
                System.out.println("    \"updatedAt\": \"" + dto.getUpdatedAt() + "\",");
                System.out.println("    \"updatedBy\": \"" + dto.getUpdatedBy() + "\"");
                System.out.println("  }");
            }


            return responseDtos;
        }
    }