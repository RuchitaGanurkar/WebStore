package com.webstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;
import com.webstore.service.ProductPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ProductPriceControllerTest {

    @Mock
    private ProductPriceService productPriceService;

    @InjectMocks
    private ProductPriceController productPriceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProductPriceRequestDto requestDto;
    private ProductPriceResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productPriceController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        requestDto = new ProductPriceRequestDto();
        requestDto.setProductId(1);
        requestDto.setCurrencyId(1);
        requestDto.setPriceAmount(BigInteger.valueOf(1000));

        responseDto = new ProductPriceResponseDto();
        responseDto.setProductPriceId(1);
        responseDto.setProductId(1);
        responseDto.setProductName("Test Product");
        responseDto.setCurrencyId(1);
        responseDto.setCurrencyCode("USD");
        responseDto.setCurrencySymbol("$");
        responseDto.setPriceAmount(BigInteger.valueOf(1000));
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setCreatedBy("admin");
        responseDto.setUpdatedAt(LocalDateTime.now());
        responseDto.setUpdatedBy("admin");
    }

    @Test
    void testCreateProductPrice() throws Exception {
        when(productPriceService.createProductPrice(any(ProductPriceRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/product-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productPriceId", is(1)))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.productName", is("Test Product")))
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencySymbol", is("$")))
                .andExpect(jsonPath("$.priceAmount", is(1000)));

        verify(productPriceService, times(1)).createProductPrice(any(ProductPriceRequestDto.class));
    }

    @Test
    void testGetProductPriceById() throws Exception {
        when(productPriceService.getProductPriceById(1)).thenReturn(responseDto);

        mockMvc.perform(get("/api/product-price/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productPriceId", is(1)))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.productName", is("Test Product")))
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencySymbol", is("$")))
                .andExpect(jsonPath("$.priceAmount", is(1000)));

        verify(productPriceService, times(1)).getProductPriceById(1);
    }

    @Test
    void testUpdateProductPrice() throws Exception {
        ProductPriceResponseDto updatedResponse = new ProductPriceResponseDto();
        updatedResponse.setProductPriceId(1);
        updatedResponse.setProductId(1);
        updatedResponse.setProductName("Test Product");
        updatedResponse.setCurrencyId(1);
        updatedResponse.setCurrencyCode("USD");
        updatedResponse.setCurrencySymbol("$");
        updatedResponse.setPriceAmount(BigInteger.valueOf(2000));
        updatedResponse.setCreatedAt(LocalDateTime.now());
        updatedResponse.setCreatedBy("admin");
        updatedResponse.setUpdatedAt(LocalDateTime.now());
        updatedResponse.setUpdatedBy("admin");

        when(productPriceService.updateProductPrice(eq(1), eq(BigInteger.valueOf(2000))))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/product-price/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productPriceId", is(1)))
                .andExpect(jsonPath("$.priceAmount", is(2000)));

        verify(productPriceService, times(1)).updateProductPrice(eq(1), eq(BigInteger.valueOf(2000)));
    }

    @Test
    void testUpdateProductPriceWithNullValue() throws Exception {
        mockMvc.perform(put("/api/product-price/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        verify(productPriceService, never()).updateProductPrice(anyInt(), any(BigInteger.class));
    }

    @Test
    void testDeleteProductPrice() throws Exception {
        doNothing().when(productPriceService).deleteProductPrice(1);

        mockMvc.perform(delete("/api/product-price/1"))
                .andExpect(status().isNoContent());

        verify(productPriceService, times(1)).deleteProductPrice(1);
    }
}