package com.nexus.recur.infrastructure.webhook;

import com.nexus.recur.config.SubscriptionProperties;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "subscription.webhook-outbound-enabled", havingValue = "true", matchIfMissing = true)
public class RestWebhookHttpClient implements WebhookHttpClient {
    private final RestClient restClient;

    public RestWebhookHttpClient(SubscriptionProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getWebhookConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getWebhookReadTimeoutMs()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public WebhookResponse post(String url, String body, String signatureHeader) {
        try {
            var response = restClient.post()
                    .uri(url)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Signature", signatureHeader)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
            return new WebhookResponse(response.getStatusCode().value(), response.getBody());
        } catch (Exception ex) {
            return new WebhookResponse(0, ex.getMessage());
        }
    }
}
