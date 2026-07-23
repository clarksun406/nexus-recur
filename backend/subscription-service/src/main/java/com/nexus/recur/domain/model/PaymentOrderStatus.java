package com.nexus.recur.domain.model;

public enum PaymentOrderStatus {
    pending_screening,
    pending_approval,
    approved,
    processing,
    completed,
    rejected,
    blocked,
    failed
}
