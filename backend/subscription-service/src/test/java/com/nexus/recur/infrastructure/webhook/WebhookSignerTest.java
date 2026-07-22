package com.nexus.recur.infrastructure.webhook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignerTest {
    private final WebhookSigner signer = new WebhookSigner();

    @Test
    void generatesVerifiableSignature() {
        String payload = "{\"event\":\"test\"}";
        String secret = "my-secret";

        String signature = signer.sign(payload, secret);

        assertThat(signature).startsWith("t=");
        assertThat(signature).contains("v1=");
        assertThat(signer.verify(payload, signature, secret)).isTrue();
    }

    @Test
    void rejectsDifferentSecret() {
        String payload = "{\"event\":\"test\"}";
        String signature = signer.sign(payload, "secret-a");

        assertThat(signer.verify(payload, signature, "secret-b")).isFalse();
    }

    @Test
    void rejectsTamperedPayload() {
        String signature = signer.sign("{\"event\":\"original\"}", "secret");

        assertThat(signer.verify("{\"event\":\"tampered\"}", signature, "secret")).isFalse();
    }

    @Test
    void rejectsNullSignature() {
        assertThat(signer.verify("payload", null, "secret")).isFalse();
    }
}
