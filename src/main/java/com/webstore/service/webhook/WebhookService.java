package com.webstore.service.webhook;

import com.webstore.dto.request.WebhookRequestDto;

public interface WebhookService {
    void processIncomingMessage(WebhookRequestDto webhookData);
}
