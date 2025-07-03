package com.webstore.service.whatsapp.flow;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.service.whatsapp.builder.MessageBuilderService;
import com.webstore.service.whatsapp.business.CategoryBusinessService;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.util.MessageFormatter;
import com.webstore.util.PaginationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryFlowServiceTest {

    @Mock
    private CategoryBusinessService categoryService;

    @Mock
    private ProductBusinessService productService;

    @Mock
    private WhatsAppMessageSender messageSender;

    @Mock
    private MessageBuilderService messageBuilder;

    @Mock
    private MessageFormatter formatter;

    @Mock
    private PaginationUtil paginationUtil;

    @InjectMocks
    private CategoryFlowService categoryFlowService;

    @Test
    void shouldSendCategoryButtons_whenShouldUseButtonsIsTrue() {
        when(categoryService.shouldUseButtonsForCategories()).thenReturn(true);
        when(categoryService.getTop3CategoryNames()).thenReturn(List.of("Fruits", "Vegetables", "Snacks"));
        when(formatter.truncateText(anyString(), anyInt())).thenReturn("Fruits", "Vegetables", "Snacks");

        WhatsAppRequestDto.Button mockButton = mock(WhatsAppRequestDto.Button.class);
        when(messageBuilder.createButton(anyString(), anyString())).thenReturn(mockButton);

        WhatsAppRequestDto mockRequest = mock(WhatsAppRequestDto.class);
        when(messageBuilder.buildButtonMessage(anyString(), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(mockRequest);

        categoryFlowService.sendCategorySelection("v1", "123", "456");

        verify(messageSender).sendMessage(eq("123"), eq(mockRequest), eq("Category buttons message"));
    }

    @Test
    void shouldSendCategoryList_whenShouldUseButtonsIsFalse() {
        when(categoryService.shouldUseButtonsForCategories()).thenReturn(false);
        when(categoryService.getAllCategoryNames()).thenReturn(List.of("Fruits"));
        when(categoryService.getCategoryIdByName(anyString())).thenReturn(1);
        when(productService.getProductNamesByCategory(1)).thenReturn(List.of("Apple"));

        PaginationUtil.PaginationResult<String> paginated =
                new PaginationUtil.PaginationResult<>(List.of("Fruits"), 1, 1, 1);

        when(paginationUtil.<String>paginate(anyList(), anyInt())).thenReturn(paginated); // ✅ Fix here

        when(messageBuilder.createRow(anyString(), anyString(), anyString()))
                .thenReturn(mock(WhatsAppRequestDto.Row.class));
        when(messageBuilder.createSection(anyString(), anyList()))
                .thenReturn(mock(WhatsAppRequestDto.Section.class));
        when(messageBuilder.buildListMessage(anyString(), anyString(), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(mock(WhatsAppRequestDto.class));

        categoryFlowService.sendCategorySelection("v1", "123", "456");

        verify(messageSender).sendMessage(eq("123"), any(), eq("Category list message with pagination"));
    }


    @Test
    void shouldSendTextMessage_whenCategoryListIsEmpty() {
        when(categoryService.shouldUseButtonsForCategories()).thenReturn(false);
        when(categoryService.getAllCategoryNames()).thenReturn(List.of());

        categoryFlowService.sendCategorySelection("v1", "123", "456");

        verify(messageSender).sendTextMessage(eq("123"), eq("456"), eq("No categories available at the moment."));
    }
}
