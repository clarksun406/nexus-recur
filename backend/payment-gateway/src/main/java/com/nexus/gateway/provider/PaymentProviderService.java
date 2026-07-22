package com.nexus.gateway.provider;

public interface PaymentProviderService {
    PaymentProvider brand();

    ChargeResult charge(ChargeRequest request);

    RefundResult refund(RefundRequest request);

    boolean cancel(String providerTransactionId);
}
