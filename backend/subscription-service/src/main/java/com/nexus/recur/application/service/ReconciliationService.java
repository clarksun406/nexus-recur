package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.InvoiceStatus;
import com.nexus.recur.domain.model.SubscriptionInvoice;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private final SubscriptionInvoiceRepository invoiceRepository;

    public ReconciliationService(SubscriptionInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public ReconciliationReport monthlyReport(String merchantId, int year, int month) {
        OffsetDateTime start = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.plusMonths(1);

        List<SubscriptionInvoice> invoices = (merchantId != null && !merchantId.isBlank())
                ? invoiceRepository.findByMerchantIdAndCreatedAtBetween(merchantId, start, end)
                : invoiceRepository.findByCreatedAtBetween(start, end);

        long totalInvoices = invoices.size();
        long paidInvoices = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.paid).count();
        long failedInvoices = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.failed).count();
        long pendingInvoices = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.pending).count();

        long grossCents = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.paid)
                .mapToLong(i -> i.getAmountCents()).sum();
        long taxCents = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.paid && i.getTaxAmountCents() != null)
                .mapToLong(SubscriptionInvoice::getTaxAmountCents).sum();
        long discountCents = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.paid && i.getDiscountAmountCents() != null)
                .mapToLong(SubscriptionInvoice::getDiscountAmountCents).sum();

        Map<String, Long> byCurrency = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.paid)
                .collect(Collectors.groupingBy(SubscriptionInvoice::getCurrency,
                        Collectors.summingLong(SubscriptionInvoice::getAmountCents)));

        BigDecimal successRate = totalInvoices > 0
                ? BigDecimal.valueOf(paidInvoices * 100.0 / totalInvoices).setScale(1, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ReconciliationReport(year, month, merchantId,
                totalInvoices, paidInvoices, failedInvoices, pendingInvoices,
                grossCents, taxCents, discountCents, netCents(grossCents, taxCents, discountCents),
                byCurrency, successRate);
    }

    @Transactional(readOnly = true)
    public String exportCsv(String merchantId, int year, int month) {
        OffsetDateTime start = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.plusMonths(1);

        List<SubscriptionInvoice> invoices = (merchantId != null && !merchantId.isBlank())
                ? invoiceRepository.findByMerchantIdAndCreatedAtBetween(merchantId, start, end)
                : invoiceRepository.findByCreatedAtBetween(start, end);

        StringBuilder sb = new StringBuilder();
        sb.append("invoice_id,subscription_id,merchant_id,customer_id,status,currency,amount_cents,subtotal_cents,tax_amount_cents,discount_amount_cents,total_cents,invoice_number,paid_at,created_at\n");

        for (SubscriptionInvoice inv : invoices) {
            sb.append(csv(inv.getId())).append(',')
              .append(csv(inv.getSubscriptionId())).append(',')
              .append(csv(inv.getMerchantId())).append(',')
              .append(csv(inv.getCustomerId())).append(',')
              .append(inv.getStatus()).append(',')
              .append(csv(inv.getCurrency())).append(',')
              .append(inv.getAmountCents()).append(',')
              .append(inv.getSubtotalCents() != null ? inv.getSubtotalCents() : "").append(',')
              .append(inv.getTaxAmountCents() != null ? inv.getTaxAmountCents() : "").append(',')
              .append(inv.getDiscountAmountCents() != null ? inv.getDiscountAmountCents() : "").append(',')
              .append(inv.getTotalCents() != null ? inv.getTotalCents() : "").append(',')
              .append(csv(inv.getInvoiceNumber())).append(',')
              .append(inv.getPaidAt() != null ? inv.getPaidAt().toString() : "").append(',')
              .append(inv.getCreatedAt().toString())
              .append('\n');
        }
        return sb.toString();
    }

    private long netCents(long gross, long tax, long discount) {
        return gross - discount;
    }

    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public record ReconciliationReport(
            int year, int month, String merchantId,
            long totalInvoices, long paidInvoices, long failedInvoices, long pendingInvoices,
            long grossCents, long taxCents, long discountCents, long netCents,
            Map<String, Long> revenueByCurrency, BigDecimal chargeSuccessRate) {}
}
