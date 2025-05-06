package com.webstore.dto.request;

import java.util.List;
import lombok.Data;

/**
 * DTO for incoming webhook requests from the WhatsApp Business API
 */
@Data
public class WhatsAppWebhookRequestDto {
    private String object;
    private List<Entry> entry;

    @Data
    public static class Entry {
        private String id;
        private List<Change> changes;

        @Data
        public static class Change {
            private Value value;
            private String field;

            @Data
            public static class Value {
                private String messagingProduct;
                private Metadata metadata;
                private List<Contact> contacts;
                private List<Message> messages;

                @Data
                public static class Metadata {
                    private String displayPhoneNumber;
                    private String phoneNumberId;
                }

                @Data
                public static class Contact {
                    private String waId;
                    private String phoneNumber;
                    private Profile profile;

                    @Data
                    public static class Profile {
                        private String name;
                    }
                }

                @Data
                public static class Message {
                    private String from;
                    private String id;
                    private String timestamp;
                    private String type;
                    private Text text;
                    private Interactive interactive;

                    @Data
                    public static class Text {
                        private String body;
                    }

                    @Data
                    public static class Interactive {
                        private String type;
                        private ButtonReply buttonReply;
                        private ListReply listReply;

                        @Data
                        public static class ButtonReply {
                            private String id;
                            private String title;
                        }

                        @Data
                        public static class ListReply {
                            private String id;
                            private String title;
                            private String description;
                        }
                    }
                }
            }
        }
    }
}