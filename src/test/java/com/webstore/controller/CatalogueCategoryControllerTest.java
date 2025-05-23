package com.webstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.CatalogueCategoryRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.service.CatalogueCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogueCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CatalogueCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "catalogueCategoryServiceImplementation") // VERY IMPORTANT!!
    private CatalogueCategoryService catalogueCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CatalogueCategoryRequestDto requestDto;
    private CatalogueCategoryResponseDto responseDto;

    @BeforeEach
    void setup() {
        requestDto = new CatalogueCategoryRequestDto();
        requestDto.setCatalogueId(1);
        requestDto.setCategoryId(2);

        responseDto = new CatalogueCategoryResponseDto();
        responseDto.setCatalogueCategoryId(1001);
        responseDto.setCatalogueId(1);
        responseDto.setCatalogueName("Electronics");
        responseDto.setCategoryId(2);
        responseDto.setCategoryName("Laptops");
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());
        responseDto.setCreatedBy("admin");
        responseDto.setUpdatedBy("admin");
    }

    @Test
    void testCreateMappingSuccess() throws Exception {
        Mockito.when(catalogueCategoryService.createCatalogueCategory(any(CatalogueCategoryRequestDto.class)))
                .thenReturn(ResponseEntity.ok("Catalogue-Category mapping created successfully."));

        mockMvc.perform(post("/api/catalogue-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Catalogue-Category mapping created successfully."));
    }

    @Test
    void testCreateMappingAlreadyExists() throws Exception {
        Mockito.when(catalogueCategoryService.createCatalogueCategory(any(CatalogueCategoryRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().body("Catalogue-Category mapping already exists."));

        mockMvc.perform(post("/api/catalogue-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Catalogue-Category mapping already exists."));
    }

    @Test
    void testGetAllMappingsEmptyList() throws Exception {
        Mockito.when(catalogueCategoryService.getAllCatalogueCategories())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/catalogue-categories"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAllMappingsWithData() throws Exception {
        Mockito.when(catalogueCategoryService.getAllCatalogueCategories())
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/catalogue-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].catalogueCategoryId").value(responseDto.getCatalogueCategoryId()))
                .andExpect(jsonPath("$[0].catalogueId").value(responseDto.getCatalogueId()))
                .andExpect(jsonPath("$[0].categoryId").value(responseDto.getCategoryId()))
                .andExpect(jsonPath("$[0].catalogueName").value(responseDto.getCatalogueName()))
                .andExpect(jsonPath("$[0].categoryName").value(responseDto.getCategoryName()))
                .andExpect(jsonPath("$[0].createdBy").value(responseDto.getCreatedBy()))
                .andExpect(jsonPath("$[0].updatedBy").value(responseDto.getUpdatedBy()));
    }
}
