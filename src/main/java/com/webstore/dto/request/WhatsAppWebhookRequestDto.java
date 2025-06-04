package com.webstore.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppWebhookRequestDto {
    private String object;
    private List<Entry> entry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private String id;
        private List<Change> changes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Change {
        private Value value;
        private String field;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;
        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        @JsonProperty("display_phone_number")
        private String displayPhoneNumber;
        @JsonProperty("phone_number_id")
        private String phoneNumberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private Profile profile;
        @JsonProperty("wa_id")
        private String waId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;
        private Text text;
        private Interactive interactive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Text {
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interactive {
        private String type;
        @JsonProperty("button_reply")
        private ButtonReply buttonReply;
        @JsonProperty("list_reply")
        private ListReply listReply;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ButtonReply {
        private String id;
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListReply {
        private String id;
        private String title;
        private String description;
    }
}