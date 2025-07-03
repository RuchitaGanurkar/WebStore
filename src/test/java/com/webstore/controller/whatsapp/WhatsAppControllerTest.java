package com.webstore.controller.whatsapp;

import com.webstore.dto.request.WebhookRequestDto;
import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.implementation.webhook.WebhookValidator;
import com.webstore.service.WhatsAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WhatsAppControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private WebhookValidator webhookValidator;

    @InjectMocks
    private WhatsAppController whatsAppController;

    @BeforeEach
    void setup() {
        whatsAppService = mock(WhatsAppService.class);
        webhookValidator = mock(WebhookValidator.class);
        whatsAppController = new WhatsAppController(whatsAppService, webhookValidator);
        mockMvc = MockMvcBuilders.standaloneSetup(whatsAppController).build();
    }

    @Test
    void testReceiveMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ }")) // Simplified dummy payload
                .andExpect(status().isOk());

        verify(webhookValidator, times(1)).processIncomingMessage(any(WebhookRequestDto.class));
    }

    @Test
    void testVerifyWebhook_successful() throws Exception {
        when(whatsAppService.verifyWebhook("subscribe", "valid_token", "1234")).thenReturn("1234");

        mockMvc.perform(get("/")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "valid_token")
                        .param("hub.challenge", "1234"))
                .andExpect(status().isOk())
                .andExpect(content().string("1234"));
    }

    @Test
    void testVerifyWebhook_forbidden() throws Exception {
        when(whatsAppService.verifyWebhook("subscribe", "invalid_token", "1234")).thenReturn(null);

        mockMvc.perform(get("/")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "invalid_token")
                        .param("hub.challenge", "1234"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSendWelcomeMessage_shouldReturnOk() throws Exception {
        String json = "{\"to\": \"1234567890\"}";

        mockMvc.perform(post("/v1/abc/send-welcome/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome message sent successfully"));

        verify(whatsAppService).sendWelcomeMessage("v1", "abc", "1234567890");
    }

    @Test
    void testSendCategoryMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/v1/abc/send-categories/messages")
                        .param("phone", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(content().string("All categories details sent successfully"));

        verify(whatsAppService).sendCategoryInteractiveMessage("v1", "abc", "1234567890");
    }

    @Test
    void testSendProductMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/v1/abc/send-products/messages")
                        .param("phone", "1234567890")
                        .param("categoryName", "Fruits"))
                .andExpect(status().isOk())
                .andExpect(content().string("All products details sent successfully"));

        verify(whatsAppService).sendProductInteractiveMessage("v1", "abc", "1234567890", "Fruits");
    }

    @Test
    void testSendProductDetailsMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/v1/abc/send-product-details/messages")
                        .param("phone", "1234567890")
                        .param("productName", "Apple"))
                .andExpect(status().isOk())
                .andExpect(content().string("Single product detail sent successfully"));

        verify(whatsAppService).sendOneProductInteractiveMessage("v1", "abc", "1234567890", "Apple");
    }

    @Test
    void testSendPricingMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/v1/abc/send-pricing/messages")
                        .param("phone", "1234567890")
                        .param("productName", "Apple"))
                .andExpect(status().isOk())
                .andExpect(content().string("Products pricing details sent successfully"));

        verify(whatsAppService).showProductPriceInteractiveMessage("v1", "abc", "1234567890", "Apple");
    }
}
