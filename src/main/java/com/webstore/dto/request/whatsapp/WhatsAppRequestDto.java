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
public class WhatsAppRequestDto {

    // Common fields for all message types
    private final String messaging_product = "whatsapp";
    private String to;
    private String type; // "text", "interactive", "template"

    // Text message fields
    private TextBody text;

    // Interactive message fields
    private Interactive interactive;

    // Template message fields
    private Template template;

    // Context and status fields
    private Context context;
    private String status;
    private String message_id;

    // ========== INNER CLASSES ==========

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

    // Interactive message structures
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
        private String button; // Button text that opens the list
        private List<Section> sections;
    }

    // Button message structures
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

    // List message structures
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String title;
        private List<Row> rows;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private String id;
        private String title;
        private String description;
    }

    // Template message structures
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Template {
        private String name;
        private Language language;
        private List<Component> components;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Language {
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Component {
        private String type;
        private List<Parameter> parameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String type;
        private String text;
    }

    // ========== FACTORY METHODS ==========

    public static WhatsAppRequestDto createTextMessage(String to, String messageText) {
        return WhatsAppRequestDto.builder()
                .to(to)
                .type("text")
                .text(TextBody.builder().body(messageText).build())
                .build();
    }

    public static WhatsAppRequestDto createButtonMessage(String to, String headerText, String bodyText,
                                                         String footerText, List<Button> buttons) {
        return WhatsAppRequestDto.builder()
                .to(to)
                .type("interactive")
                .interactive(Interactive.builder()
                        .type("button")
                        .header(Header.builder().type("text").text(headerText).build())
                        .body(Body.builder().text(bodyText).build())
                        .footer(footerText != null ? Footer.builder().text(footerText).build() : null)
                        .action(Action.builder().buttons(buttons).build())
                        .build())
                .build();
    }

    public static WhatsAppRequestDto createListMessage(String to, String headerText, String bodyText,
                                                       String footerText, String buttonText, List<Section> sections) {
        return WhatsAppRequestDto.builder()
                .to(to)
                .type("interactive")
                .interactive(Interactive.builder()
                        .type("list")
                        .header(Header.builder().type("text").text(headerText).build())
                        .body(Body.builder().text(bodyText).build())
                        .footer(footerText != null ? Footer.builder().text(footerText).build() : null)
                        .action(Action.builder().button(buttonText).sections(sections).build())
                        .build())
                .build();
    }

    public static WhatsAppRequestDto createTemplateMessage(String to, String templateName,
                                                           String languageCode, List<Component> components) {
        return WhatsAppRequestDto.builder()
                .to(to)
                .type("template")
                .template(Template.builder()
                        .name(templateName)
                        .language(Language.builder().code(languageCode).build())
                        .components(components)
                        .build())
                .build();
    }
}
