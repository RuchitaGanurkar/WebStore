package com.webstore.service.whatsapp.flow;


import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.PaginationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NavigationServiceTest {

    @Mock
    private CategoryFlowService categoryFlowService;

    @Mock
    private ProductFlowService productFlowService;

    @Mock
    private PaginationUtil paginationUtil;

    @Mock
    private WhatsAppMessageSender messageSender;

    private NavigationService navigationService;

    @BeforeEach
    void setUp() {
        navigationService = new NavigationService(categoryFlowService, productFlowService, paginationUtil, messageSender);
    }

    @Test
    void testHandleCategoryPageNavigation_Valid() {
        navigationService.handleCategoryPageNavigation("123", "user", "next_cat_page_2");

        verify(categoryFlowService).sendCategoryList(any(), eq("123"), eq("user"), eq(2));
    }

    @Test
    void testHandleCategoryPageNavigation_Invalid() {
        navigationService.handleCategoryPageNavigation("123", "user", "invalid_page");

        verify(messageSender).sendTextMessage(eq("123"), eq("user"), contains("Unable to navigate categories"));
    }

    @Test
    void testHandleProductPageNavigation_Valid() {
        String encodedCategory = Base64.getEncoder().encodeToString("Books".getBytes()); // returns "Qm9va3M="

        when(paginationUtil.decodeFromBase64(encodedCategory)).thenReturn("Books");

        String listId = "next_prod_p2_c" + encodedCategory;
        navigationService.handleProductPageNavigation("123", "user", listId);

        verify(productFlowService).sendPaginatedProductList(any(), eq("123"), eq("user"), eq("Books"), eq(2));
    }


    @Test
    void testHandleProductPageNavigation_Invalid() {
        navigationService.handleProductPageNavigation("123", "user", "bad_format");

        verify(messageSender).sendTextMessage(eq("123"), eq("user"), contains("Unable to navigate product pages"));
    }
}

