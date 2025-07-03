package com.webstore.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.controller.product.CategoryController;
import com.webstore.dto.request.product.CategoryRequestDto;
import com.webstore.dto.response.product.CategoryResponseDto;
import com.webstore.service.product.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new CategoryResponseDto();
        sampleResponse.setCategoryId(1);
        sampleResponse.setCategoryName("Electronics");
        sampleResponse.setCategoryDescription("All electronic items");
    }

    @WithMockUser
    @Test
    void testGetAllCategories() throws Exception {
        List<CategoryResponseDto> list = Arrays.asList(sampleResponse);

        Mockito.when(categoryService.getAllCategories(0, 5)).thenReturn(list);

        mockMvc.perform(get("/api/categories?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryName", is("Electronics")));
    }

    @WithMockUser
    @Test
    void testGetCategoryById() throws Exception {
        Mockito.when(categoryService.getCategoryById(1)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName", is("Electronics")));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCreateCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto();
        requestDto.setCategoryName("Electronics");
        requestDto.setCategoryDescription("All electronic items");

        Mockito.when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName", is("Electronics")));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testUpdateCategory() throws Exception {
        CategoryRequestDto updateDto = new CategoryRequestDto();
        updateDto.setCategoryName("Updated Electronics");
        updateDto.setCategoryDescription("Updated Description");

        CategoryResponseDto updatedResponse = new CategoryResponseDto();
        updatedResponse.setCategoryId(1);
        updatedResponse.setCategoryName("Updated Electronics");
        updatedResponse.setCategoryDescription("Updated Description");

        Mockito.when(categoryService.updateCategory(eq(1), any(CategoryRequestDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName", is("Updated Electronics")));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testDeleteCategory() throws Exception {
        Mockito.doNothing().when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/categories/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
