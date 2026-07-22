package com.nexus.recur.infrastructure.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class WebhookSigner {

    public String sign(String payload, String secret) {
        long timestamp = System.currentTimeMillis() / 1000;
        String timestampStr = String.valueOf(timestamp);
        String signedPayload = timestampStr + "." + payload;
        String signature = hmacSha256(signedPayload, secret);
        return "t=" + timestampStr + ",v1=" + signature;
    }

    public boolean verify(String payload, String signatureHeader, String secret) {
        if (signatureHeader == null || !signatureHeader.startsWith("t=")) {
            return false;
        }
        String[] parts = signatureHeader.split(",");
        if (parts.length < 2) return false;
        String timestamp = parts[0].substring(2);
        String signature = parts[1].startsWith("v1=") ? parts[1].substring(3) : "";
        String signedPayload = timestamp + "." + payload;
        String expected = hmacSha256(signedPayload, secret);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("cannot compute webhook signature", ex);
        }
    }
}
