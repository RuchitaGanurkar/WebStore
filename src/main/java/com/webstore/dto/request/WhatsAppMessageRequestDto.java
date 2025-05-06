package com.webstore.dto.request;

import java.util.List;
import lombok.Data;

/**
 * DTO for sending messages to the WhatsApp Business API
 */
@Data
public class WhatsAppMessageRequestDto {
    private String messagingProduct = "whatsapp";
    private String recipientType = "individual";
    private String to;
    private String type;
    private TextMessage text;
    private Interactive interactive;

    @Data
    public static class TextMessage {
        private String body;
    }

    @Data
    public static class Interactive {
        private String type;
        private Header header;
        private Body body;
        private Footer footer;
        private Action action;
        private List<Button> buttons;

        @Data
        public static class Header {
            private String type;
            private String text;
        }

        @Data
        public static class Body {
            private String text;
        }

        @Data
        public static class Footer {
            private String text;
        }

        @Data
        public static class Action {
            private String button;
            private List<Section> sections;

            @Data
            public static class Section {
                private String title;
                private List<Row> rows;

                @Data
                public static class Row {
                    private String id;
                    private String title;
                    private String description;
                }
            }
        }

        @Data
        public static class Button {
            private String type;
            private Reply reply;

            @Data
            public static class Reply {
                private String id;
                private String title;
            }
        }
    }
}