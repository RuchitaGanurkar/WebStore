package com.webstore.implementation;

import com.webstore.dto.request.product.ProductPriceRequestDto;
import com.webstore.dto.response.product.ProductPriceResponseDto;
import com.webstore.entity.product.Currency;
import com.webstore.entity.product.Product;
import com.webstore.entity.product.ProductPrice;
import com.webstore.implementation.product.ProductPriceServiceImplementation;
import com.webstore.repository.product.CurrencyRepository;
import com.webstore.repository.product.ProductPriceRepository;
import com.webstore.repository.product.ProductRepository;
import com.webstore.util.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPriceServiceImplementationTest {

    @Mock
    private ProductPriceRepository productPriceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private ProductPriceServiceImplementation productPriceService;

    private ProductPrice productPrice;
    private Product product;
    private Currency currency;
    private ProductPriceRequestDto requestDto;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        // Set up test product
        product = new Product();
        product.setProductId(1);
        product.setProductName("Test Product");
        product.setProductDescription("Test Description");

        // Set up test currency
        currency = new Currency();
        currency.setCurrencyId(1);
        currency.setCurrencyCode("USD");
        currency.setCurrencyName("US Dollar");
        currency.setCurrencySymbol("$");

        // Set up test product price
        productPrice = new ProductPrice();
        productPrice.setProductPriceId(1);
        productPrice.setProduct(product);
        productPrice.setCurrency(currency);
        productPrice.setPriceAmount(BigInteger.valueOf(1000));
        productPrice.setCreatedBy(TEST_USER);
        productPrice.setUpdatedBy(TEST_USER);
        productPrice.setCreatedAt(LocalDateTime.now());
        productPrice.setUpdatedAt(LocalDateTime.now());

        // Set up request DTO
        requestDto = new ProductPriceRequestDto();
        requestDto.setProductId(1);
        requestDto.setCurrencyId(1);
        requestDto.setPriceAmount(BigInteger.valueOf(1000));
    }

    @Test
    void createProductPrice_ShouldCreateProductPrice() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(currencyRepository.findById(1)).thenReturn(Optional.of(currency));
        when(productPriceRepository.save(any(ProductPrice.class))).thenReturn(productPrice);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUsername).thenReturn(TEST_USER);

            // Act
            ProductPriceResponseDto result = productPriceService.createProductPrice(requestDto);

            // Assert
            assertNotNull(result);
            assertEquals(productPrice.getProductPriceId(), result.getProductPriceId());
            assertEquals(product.getProductId(), result.getProductId());
            assertEquals(product.getProductName(), result.getProductName());
            assertEquals(currency.getCurrencyId(), result.getCurrencyId());
            assertEquals(currency.getCurrencyCode(), result.getCurrencyCode());
            assertEquals(currency.getCurrencySymbol(), result.getCurrencySymbol());
            assertEquals(productPrice.getPriceAmount(), result.getPriceAmount());

            verify(productRepository).findById(1);
            verify(currencyRepository).findById(1);
            verify(productPriceRepository).save(any(ProductPrice.class));
        }
    }

    @Test
    void createProductPrice_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> productPriceService.createProductPrice(requestDto));
        verify(productRepository).findById(1);
        verify(currencyRepository, never()).findById(anyInt());
        verify(productPriceRepository, never()).save(any(ProductPrice.class));
    }

    @Test
    void createProductPrice_WhenCurrencyNotFound_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(currencyRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> productPriceService.createProductPrice(requestDto));
        verify(productRepository).findById(1);
        verify(currencyRepository).findById(1);
        verify(productPriceRepository, never()).save(any(ProductPrice.class));
    }

    @Test
    void getProductPriceById_WhenProductPriceExists_ShouldReturnProductPrice() {
        // Arrange
        when(productPriceRepository.findById(1)).thenReturn(Optional.of(productPrice));

        // Act
        ProductPriceResponseDto result = productPriceService.getProductPriceById(1);

        // Assert
        assertNotNull(result);
        assertEquals(productPrice.getProductPriceId(), result.getProductPriceId());
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getProductName(), result.getProductName());
        assertEquals(currency.getCurrencyId(), result.getCurrencyId());
        assertEquals(currency.getCurrencyCode(), result.getCurrencyCode());
        assertEquals(currency.getCurrencySymbol(), result.getCurrencySymbol());
        assertEquals(productPrice.getPriceAmount(), result.getPriceAmount());

        verify(productPriceRepository).findById(1);
    }

    @Test
    void getProductPriceById_WhenProductPriceDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productPriceRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> productPriceService.getProductPriceById(99));
        verify(productPriceRepository).findById(99);
    }

    @Test
    void updateProductPrice_WhenProductPriceExists_ShouldUpdateProductPrice() {
        // Arrange
        when(productPriceRepository.findById(1)).thenReturn(Optional.of(productPrice));

        ProductPrice updatedProductPrice = new ProductPrice();
        updatedProductPrice.setProductPriceId(1);
        updatedProductPrice.setProduct(product);
        updatedProductPrice.setCurrency(currency);
        updatedProductPrice.setPriceAmount(BigInteger.valueOf(2000));
        updatedProductPrice.setCreatedBy(TEST_USER);
        updatedProductPrice.setUpdatedBy(TEST_USER);
        updatedProductPrice.setCreatedAt(productPrice.getCreatedAt());
        updatedProductPrice.setUpdatedAt(LocalDateTime.now());

        when(productPriceRepository.save(any(ProductPrice.class))).thenReturn(updatedProductPrice);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUsername).thenReturn(TEST_USER);

            // Act
            ProductPriceResponseDto result = productPriceService.updateProductPrice(1, BigInteger.valueOf(2000));

            // Assert
            assertNotNull(result);
            assertEquals(updatedProductPrice.getProductPriceId(), result.getProductPriceId());
            assertEquals(BigInteger.valueOf(2000), result.getPriceAmount());

            verify(productPriceRepository).findById(1);
            verify(productPriceRepository).save(any(ProductPrice.class));
        }
    }

    @Test
    void updateProductPrice_WhenProductPriceDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productPriceRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> productPriceService.updateProductPrice(99, BigInteger.valueOf(2000)));
        verify(productPriceRepository).findById(99);
        verify(productPriceRepository, never()).save(any(ProductPrice.class));
    }

    @Test
    void deleteProductPrice_WhenProductPriceExists_ShouldDeleteProductPrice() {
        // Arrange
        when(productPriceRepository.existsById(1)).thenReturn(true);
        doNothing().when(productPriceRepository).deleteById(1);

        // Act
        productPriceService.deleteProductPrice(1);

        // Assert
        verify(productPriceRepository).existsById(1);
        verify(productPriceRepository).deleteById(1);
    }

    @Test
    void deleteProductPrice_WhenProductPriceDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productPriceRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> productPriceService.deleteProductPrice(99));
        verify(productPriceRepository).existsById(99);
        verify(productPriceRepository, never()).deleteById(99);
    }

}