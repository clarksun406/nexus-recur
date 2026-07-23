package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.PaymentMethodStatus;
import com.nexus.recur.domain.model.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class PaymentMethodDtos {
    public record CreatePaymentMethodRequest(
            @NotNull PaymentMethodType type,
            String provider,
            String cardBrand,
            String cardLast4,
            Integer expMonth,
            Integer expYear,
            String billingAddressJson) {}

    public record PaymentMethodResponse(
            String id, String customerId, PaymentMethodType type,
            String provider, String cardBrand, String cardLast4,
            Integer expMonth, Integer expYear,
            PaymentMethodStatus status, boolean isDefault,
            OffsetDateTime createdAt) {}
}
