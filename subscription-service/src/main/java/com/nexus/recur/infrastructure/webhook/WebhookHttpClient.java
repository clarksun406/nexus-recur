package com.nexus.recur.infrastructure.webhook;

public interface WebhookHttpClient {
    WebhookResponse post(String url, String body, String signatureHeader);

    record WebhookResponse(int statusCode, String body) {}
}
