package com.nexus.gateway.provider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProviderDispatcher {

    private final Map<PaymentProvider, PaymentProviderService> providers;

    public PaymentProviderDispatcher(List<PaymentProviderService> providerServices) {
        this.providers = providerServices.stream()
                .collect(Collectors.toMap(PaymentProviderService::brand, Function.identity()));
    }

    public ChargeResult charge(PaymentProvider provider, ChargeRequest request) {
        return resolve(provider).charge(request);
    }

    public RefundResult refund(PaymentProvider provider, RefundRequest request) {
        return resolve(provider).refund(request);
    }

    public boolean cancel(PaymentProvider provider, String providerTransactionId) {
        return resolve(provider).cancel(providerTransactionId);
    }

    private PaymentProviderService resolve(PaymentProvider provider) {
        PaymentProviderService service = providers.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("No provider registered for: " + provider);
        }
        return service;
    }
}
