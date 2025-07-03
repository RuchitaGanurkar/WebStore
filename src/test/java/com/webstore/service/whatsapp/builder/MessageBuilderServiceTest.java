package com.webstore.service.whatsapp.builder;

import com.webstore.dto.request.whatsapp.WhatsAppRequestDto;
import com.webstore.util.MessageFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageBuilderServiceTest {

    private MessageFormatter formatter;
    private MessageBuilderService builder;

    @BeforeEach
    void setUp() {
        formatter = mock(MessageFormatter.class);
        builder = new MessageBuilderService(formatter);
    }

    @Test
    void testCreateButton() {
        when(formatter.truncateButtonTitle("View Cart")).thenReturn("View Cart");

        WhatsAppRequestDto.Button button = builder.createButton("btn_cart", "View Cart");

        assertEquals("reply", button.getType());
        assertNotNull(button.getReply());
        assertEquals("btn_cart", button.getReply().getId());
        assertEquals("View Cart", button.getReply().getTitle());
    }

    @Test
    void testCreateRow() {
        when(formatter.truncateRowTitle("Fruits")).thenReturn("Fruits");
        when(formatter.truncateRowDescription("Fresh fruits section")).thenReturn("Fresh fruits section");

        WhatsAppRequestDto.Row row = builder.createRow("row_1", "Fruits", "Fresh fruits section");

        assertEquals("row_1", row.getId());
        assertEquals("Fruits", row.getTitle());
        assertEquals("Fresh fruits section", row.getDescription());
    }

    @Test
    void testCreateSection() {
        WhatsAppRequestDto.Row row = WhatsAppRequestDto.Row.builder()
                .id("row_1").title("Fruits").description("Fresh").build();

        when(formatter.truncateSectionTitle("Categories")).thenReturn("Categories");

        WhatsAppRequestDto.Section section = builder.createSection("Categories", List.of(row));

        assertEquals("Categories", section.getTitle());
        assertEquals(1, section.getRows().size());
        assertEquals("row_1", section.getRows().get(0).getId());
    }

    @Test
    void testBuildTextMessage() {
        WhatsAppRequestDto message = builder.buildTextMessage("12345", "Hello!");

        assertEquals("12345", message.getTo());
        assertNotNull(message.getText());
        assertEquals("Hello!", message.getText().getBody());
        assertEquals("text", message.getType());
    }

    @Test
    void testBuildButtonMessage() {
        WhatsAppRequestDto.Button button = WhatsAppRequestDto.Button.builder()
                .type("reply")
                .reply(WhatsAppRequestDto.Reply.builder().id("btn_1").title("Yes").build())
                .build();

        WhatsAppRequestDto message = builder.buildButtonMessage("12345", "Header", "Body", "Footer", List.of(button));

        assertEquals("12345", message.getTo());
        assertEquals("interactive", message.getType());
        assertNotNull(message.getInteractive());
        assertEquals("button", message.getInteractive().getType());
        assertEquals("Header", message.getInteractive().getHeader().getText());
        assertEquals("Body", message.getInteractive().getBody().getText());
        assertEquals("Footer", message.getInteractive().getFooter().getText());
        assertEquals(1, message.getInteractive().getAction().getButtons().size());
    }

    @Test
    void testBuildListMessage() {
        WhatsAppRequestDto.Row row = WhatsAppRequestDto.Row.builder()
                .id("r1").title("Option 1").description("desc").build();

        WhatsAppRequestDto.Section section = WhatsAppRequestDto.Section.builder()
                .title("Sec").rows(List.of(row)).build();

        WhatsAppRequestDto message = builder.buildListMessage("12345", "Header", "Body", "Footer", "Choose", List.of(section));

        assertEquals("12345", message.getTo());
        assertEquals("interactive", message.getType());
        assertNotNull(message.getInteractive());
        assertEquals("list", message.getInteractive().getType());
        assertEquals("Header", message.getInteractive().getHeader().getText());
        assertEquals("Body", message.getInteractive().getBody().getText());
        assertEquals("Footer", message.getInteractive().getFooter().getText());
        assertEquals("Choose", message.getInteractive().getAction().getButton());
        assertEquals(1, message.getInteractive().getAction().getSections().size());
        assertEquals("Sec", message.getInteractive().getAction().getSections().get(0).getTitle());
    }
}
