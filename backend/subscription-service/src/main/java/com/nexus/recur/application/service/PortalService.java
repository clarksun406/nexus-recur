package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.*;
import com.nexus.recur.domain.repository.PortalTokenRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class PortalService {

    private static final int TOKEN_EXPIRY_MINUTES = 5;
    private static final int SESSION_EXPIRY_MINUTES = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PortalTokenRepository portalTokenRepository;
    private final CustomerService customerService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final PaymentMethodService paymentMethodService;
    private final SubscriptionService subscriptionService;
    private final IdGenerator idGenerator;

    public PortalService(PortalTokenRepository portalTokenRepository,
                         CustomerService customerService,
                         SubscriptionRepository subscriptionRepository,
                         SubscriptionInvoiceRepository invoiceRepository,
                         PaymentMethodService paymentMethodService,
                         SubscriptionService subscriptionService,
                         IdGenerator idGenerator) {
        this.portalTokenRepository = portalTokenRepository;
        this.customerService = customerService;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
        this.subscriptionService = subscriptionService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public String generateMagicLink(String customerId) {
        customerService.getEntity(customerId);

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PortalToken portalToken = new PortalToken();
        portalToken.setId(idGenerator.next("ptk"));
        portalToken.setCustomerId(customerId);
        portalToken.setToken(token);
        portalToken.setExpiresAt(OffsetDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        portalTokenRepository.save(portalToken);

        return "/portal/auth?token=" + token;
    }

    @Transactional
    public PortalSession authenticate(String token) {
        PortalToken portalToken = portalTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid portal token"));

        if (portalToken.isUsed()) {
            throw new BusinessException("TOKEN_USED", "Portal token has already been used");
        }
        if (portalToken.isExpired()) {
            throw new BusinessException("TOKEN_EXPIRED", "Portal token has expired");
        }

        portalToken.setUsedAt(OffsetDateTime.now());
        portalTokenRepository.save(portalToken);

        String sessionId = idGenerator.next("ses");
        OffsetDateTime sessionExpiry = OffsetDateTime.now().plusMinutes(SESSION_EXPIRY_MINUTES);

        return new PortalSession(sessionId, portalToken.getCustomerId(), sessionExpiry);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptions(String customerId) {
        return subscriptionRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionInvoice> getInvoices(String customerId, int page, int limit) {
        List<Subscription> subs = subscriptionRepository.findByCustomerId(customerId);
        if (subs.isEmpty()) return Page.empty();
        String subId = subs.get(0).getId();
        return invoiceRepository.findBySubscriptionId(subId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)));
    }

    @Transactional
    public void cancelSubscription(String customerId, String subscriptionId, String reason) {
        validateOwnership(customerId, subscriptionId);
        subscriptionService.cancel(subscriptionId,
                new com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.CancelSubscriptionRequest(false, reason));
    }

    @Transactional
    public void resumeSubscription(String customerId, String subscriptionId) {
        validateOwnership(customerId, subscriptionId);
        subscriptionService.resume(subscriptionId);
    }

    private Subscription validateOwnership(String customerId, String subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException("SUBSCRIPTION_NOT_FOUND", "Subscription not found"));
        if (!customerId.equals(sub.getCustomerId())) {
            throw new BusinessException("FORBIDDEN", "Subscription does not belong to this customer");
        }
        return sub;
    }

    public record PortalSession(String sessionId, String customerId, OffsetDateTime expiresAt) {}
}
