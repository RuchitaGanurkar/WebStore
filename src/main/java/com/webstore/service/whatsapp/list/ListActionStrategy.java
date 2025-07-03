package com.webstore.service.whatsapp.list;

public interface ListActionStrategy {
    boolean supports(String listId);
    void handle(String phoneNumberId, String from, String listId);
}
