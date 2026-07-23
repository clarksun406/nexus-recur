package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, String> {
    List<InvoiceLineItem> findByInvoiceId(String invoiceId);
}
