package com.webstore.service.whatsapp.handler;

public interface InteractionHandler<T> {
    void handle(String phoneNumberId, String from, T data);
}
