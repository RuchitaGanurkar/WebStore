package com.webstore.dto.response;

import java.util.List;
import lombok.Data;

/**
 * DTO for handling responses from the WhatsApp Business API after sending a message
 */
@Data
public class WhatsAppMessageResponseDto {
    private String messagingProduct;
    private List<Contact> contacts;
    private List<Message> messages;

    @Data
    public static class Contact {
        private String input;
        private String waId;
    }

    @Data
    public static class Message {
        private String id;
    }
}