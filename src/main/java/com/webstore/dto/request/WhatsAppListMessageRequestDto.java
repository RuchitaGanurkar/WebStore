
package com.webstore.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class WhatsAppListMessageRequestDto {
    private String messagingProduct = "whatsapp";
    private String to;
    private String type = "interactive";
    private Interactive interactive;

    @Data
    public static class Interactive {
        private String type = "list";
        private Header header;
        private Body body;
        private Footer footer;
        private Action action;
    }

    @Data
    public static class Header {
        private String type = "text";
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
    }

    @Data
    public static class Section {
        private String title;
        private List<Row> rows;
    }

    @Data
    public static class Row {
        private String id;
        private String title;
        private String description;
    }
}
