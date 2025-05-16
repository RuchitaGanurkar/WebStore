package com.webstore.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WhatsAppMessageRequestDto {

    private String messaging_product = "whatsapp";
    private String to;
    private TextBody text;
    private Context context;
    private String status;
    private String message_id;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextBody {
        private String body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        private String message_id;
    }
}