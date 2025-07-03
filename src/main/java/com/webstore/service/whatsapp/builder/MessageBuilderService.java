package com.webstore.service.whatsapp.builder;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.util.MessageFormatter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageBuilderService {

    private final MessageFormatter formatter;

    public MessageBuilderService(MessageFormatter formatter) {
        this.formatter = formatter;
    }

    public WhatsAppRequestDto.Button createButton(String id, String title) {
        return WhatsAppRequestDto.Button.builder()
                .type("reply")
                .reply(WhatsAppRequestDto.Reply.builder()
                        .id(id)
                        .title(formatter.truncateButtonTitle(title))
                        .build())
                .build();
    }

    public WhatsAppRequestDto.Row createRow(String id, String title, String description) {
        return WhatsAppRequestDto.Row.builder()
                .id(id)
                .title(formatter.truncateRowTitle(title))
                .description(formatter.truncateRowDescription(description))
                .build();
    }

    public WhatsAppRequestDto.Section createSection(String title, List<WhatsAppRequestDto.Row> rows) {
        return WhatsAppRequestDto.Section.builder()
                .title(formatter.truncateSectionTitle(title))
                .rows(rows)
                .build();
    }

    public WhatsAppRequestDto buildTextMessage(String to, String messageText) {
        return WhatsAppRequestDto.createTextMessage(to, messageText);
    }

    public WhatsAppRequestDto buildButtonMessage(String to, String headerText, String bodyText,
                                                 String footerText, List<WhatsAppRequestDto.Button> buttons) {
        return WhatsAppRequestDto.createButtonMessage(to, headerText, bodyText, footerText, buttons);
    }

    public WhatsAppRequestDto buildListMessage(String to, String headerText, String bodyText,
                                               String footerText, String buttonText, List<WhatsAppRequestDto.Section> sections) {
        return WhatsAppRequestDto.createListMessage(to, headerText, bodyText, footerText, buttonText, sections);
    }
}