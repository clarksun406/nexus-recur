package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_pm_customer", columnList = "customerId")
})
public class PaymentMethod {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String customerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethodType type;
    @Column(length = 32)
    private String provider;
    @Column(length = 16)
    private String cardBrand;
    @Column(length = 4)
    private String cardLast4;
    private Integer expMonth;
    private Integer expYear;
    @Column(columnDefinition = "text")
    private String billingAddressJson;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethodStatus status = PaymentMethodStatus.active;
    private boolean isDefault;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public PaymentMethodType getType() { return type; }
    public void setType(PaymentMethodType type) { this.type = type; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    public Integer getExpMonth() { return expMonth; }
    public void setExpMonth(Integer expMonth) { this.expMonth = expMonth; }
    public Integer getExpYear() { return expYear; }
    public void setExpYear(Integer expYear) { this.expYear = expYear; }
    public String getBillingAddressJson() { return billingAddressJson; }
    public void setBillingAddressJson(String billingAddressJson) { this.billingAddressJson = billingAddressJson; }
    public PaymentMethodStatus getStatus() { return status; }
    public void setStatus(PaymentMethodStatus status) { this.status = status; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
