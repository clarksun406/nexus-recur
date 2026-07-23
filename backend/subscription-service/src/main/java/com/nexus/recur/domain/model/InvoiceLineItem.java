package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invoice_line_items", indexes = {
        @Index(name = "idx_ili_invoice", columnList = "invoiceId")
})
public class InvoiceLineItem {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String invoiceId;
    @Column(length = 256)
    private String description;
    private int quantity = 1;
    @Column(nullable = false)
    private int unitPriceCents;
    @Column(nullable = false)
    private int amountCents;
    private int taxAmountCents;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    @Column(length = 32)
    private String planId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LineItemType lineType;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(int unitPriceCents) { this.unitPriceCents = unitPriceCents; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public int getTaxAmountCents() { return taxAmountCents; }
    public void setTaxAmountCents(int taxAmountCents) { this.taxAmountCents = taxAmountCents; }
    public OffsetDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(OffsetDateTime periodStart) { this.periodStart = periodStart; }
    public OffsetDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(OffsetDateTime periodEnd) { this.periodEnd = periodEnd; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public LineItemType getLineType() { return lineType; }
    public void setLineType(LineItemType lineType) { this.lineType = lineType; }
}
