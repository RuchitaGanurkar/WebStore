package com.webstore.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppWebhookRequestDto {

    @JsonProperty("entry")
    private List<Entry> entry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        @JsonProperty("id")
        private String id;

        @JsonProperty("changes")
        private List<Change> changes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Change {
        @JsonProperty("value")
        private Value value;

        @JsonProperty("field")
        private String field;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;

        @JsonProperty("metadata")
        private Metadata metadata;

        @JsonProperty("messages")
        private List<Message> messages;

        @JsonProperty("contacts")
        private List<Contact> contacts;
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
    public static class Message {
        @JsonProperty("from")
        private String from;

        @JsonProperty("id")
        private String id;

        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("type")
        private String type;

        @JsonProperty("text")
        private Text text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Text {
        @JsonProperty("body")
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        @JsonProperty("profile")
        private Profile profile;

        @JsonProperty("wa_id")
        private String waId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        @JsonProperty("name")
        private String name;
    }
}