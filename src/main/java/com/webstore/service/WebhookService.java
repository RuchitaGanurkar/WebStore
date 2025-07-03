package com.webstore.service;

import com.webstore.dto.request.WebhookRequestDto;

public interface WebhookService {
    void processIncomingMessage(WebhookRequestDto webhookData);
}
