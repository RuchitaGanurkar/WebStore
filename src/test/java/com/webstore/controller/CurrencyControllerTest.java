package com.webstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.CurrencyRequestDto;
import com.webstore.dto.response.CurrencyResponseDto;
import com.webstore.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
//import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {

    private static final String BASE_URL = "/api/currencies";

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

        requestDto = new CurrencyRequestDto("USD", "US Dollar", "$");

        responseDto = new CurrencyResponseDto();
        responseDto.setCurrencyId(1);
        responseDto.setCurrencyCode("USD");
        responseDto.setCurrencyName("US Dollar");
        responseDto.setCurrencySymbol("$");
    }

    @Test
    @DisplayName("Should return list of currencies")
    void testGetAllCurrencies() throws Exception {
        List<CurrencyResponseDto> currencies = List.of(responseDto);
        when(currencyService.getAllCurrencies(0, 5)).thenReturn(currencies);

        mockMvc.perform(get(BASE_URL)
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
    @DisplayName("Should return currency by ID")
    void testGetCurrencyById() throws Exception {
        when(currencyService.getCurrencyById(1)).thenReturn(responseDto);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencyName", is("US Dollar")))
                .andExpect(jsonPath("$.currencySymbol", is("$")));

        verify(currencyService, times(1)).getCurrencyById(1);
    }

    @Test
    @DisplayName("Should create a new currency")
    void testCreateCurrency() throws Exception {
        when(currencyService.createCurrency(ArgumentMatchers.any(CurrencyRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencyName", is("US Dollar")))
                .andExpect(jsonPath("$.currencySymbol", is("$")));

        verify(currencyService, times(1)).createCurrency(ArgumentMatchers.any(CurrencyRequestDto.class));
    }

    @Test
    @DisplayName("Should update currency by ID")
    void testUpdateCurrency() throws Exception {
        when(currencyService.updateCurrency(eq(1), ArgumentMatchers.any(CurrencyRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyId", is(1)))
                .andExpect(jsonPath("$.currencyCode", is("USD")))
                .andExpect(jsonPath("$.currencyName", is("US Dollar")))
                .andExpect(jsonPath("$.currencySymbol", is("$")));

        verify(currencyService, times(1)).updateCurrency(eq(1), ArgumentMatchers.any(CurrencyRequestDto.class));
    }

    @Test
    @DisplayName("Should delete currency by ID")
    void testDeleteCurrency() throws Exception {
        when(currencyService.deleteCurrency(1)).thenReturn("Currency deleted successfully");

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Currency deleted successfully"));

        verify(currencyService, times(1)).deleteCurrency(1);
    }


    // Optional: Add test cases for error scenarios (400, 404, etc.)
}
