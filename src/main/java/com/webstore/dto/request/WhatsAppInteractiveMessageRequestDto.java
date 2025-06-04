package com.webstore.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppInteractiveMessageRequestDto {
    private final String messaging_product = "whatsapp";
    private final String recipient_type = "individual";
    private String to;
    private final String type = "interactive";
    private Interactive interactive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interactive {
        private String type; // "button" or "list"
        private Header header;
        private Body body;
        private Footer footer;
        private Action action;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String type; // "text"
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Footer {
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        // For Button Messages
        private List<Button> buttons;

        // For List Messages
        private String button; // Button text that opens the list (e.g., "View Categories")
        private List<Section> sections;
    }

    // Button Message Classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Button {
        private String type; // "reply"
        private Reply reply;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reply {
        private String id;
        private String title;
    }

    // List Message Classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String title; // Section header
        private List<Row> rows; // Up to 10 rows per section
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private String id; // Unique identifier for the row
        private String title; // Main text (up to 24 characters)
        private String description; // Subtitle text (up to 72 characters)
    }
}
