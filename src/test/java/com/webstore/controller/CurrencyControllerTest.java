package com.webstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.service.CurrencyService;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CurrencyRequestDto requestDto;
    private CurrencyResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        requestDto = new CurrencyRequestDto();
        requestDto.setCurrencyCode("USD");
        requestDto.setCurrencyName("US Dollar");
        requestDto.setCurrencySymbol("$");

        responseDto = new CurrencyResponseDto();
        responseDto.setCurrencyId(1);
        responseDto.setCurrencyCode("USD");
        responseDto.setCurrencyName("US Dollar");
        responseDto.setCurrencySymbol("$");
        // You don't need to set the timestamps for testing unless specifically testing that functionality
    }

    @Test
    void testGetAllCurrencies() throws Exception {
        List<CurrencyResponseDto> currencies = Arrays.asList(responseDto);
        when(currencyService.getAllCurrencies(0, 5)).thenReturn(currencies);

        mockMvc.perform(get("/api/currencies")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].currencyId", is(1)))
                .andExpect(jsonPath("$[0].currencyCode", is("USD")))
                .andExpect(jsonPath("$[0].currencyName", is("US Dollar")))
                .andExpect(jsonPath("$[0].currencySymbol", is("$")));

        verify(currencyService, times(1)).getAllCurrencies(0, 5);
    }

    @Test
    void testGetCurrencyById() throws Exception {
        when(currencyService.getCurrencyById(1)).thenReturn(responseDto);

        mockMvc.perform(get("/api/currencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencyName", is("US Dollar")))
                .andExpect(jsonPath("$.currencySymbol", is("$")));

        verify(currencyService, times(1)).getCurrencyById(1);
    }

    @Test
    void testCreateCurrency() throws Exception {
        when(currencyService.createCurrency(any(CurrencyRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencyName", is("US Dollar")))
                .andExpect(jsonPath("$.currencySymbol", is("$")));

        verify(currencyService, times(1)).createCurrency(any(CurrencyRequestDto.class));
    }

    @Test
    void testUpdateCurrency() throws Exception {
        when(currencyService.updateCurrency(eq(1), any(CurrencyRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencyName", is("US Dollar")))
                .andExpect(jsonPath("$.currencySymbol", is("$")));

        verify(currencyService, times(1)).updateCurrency(eq(1), any(CurrencyRequestDto.class));
    }

    @Test
    void testDeleteCurrency() throws Exception {
        doNothing().when(currencyService).deleteCurrency(1);

        mockMvc.perform(delete("/api/currencies/1"))
                .andExpect(status().isNoContent());

        verify(currencyService, times(1)).deleteCurrency(1);
    }
}