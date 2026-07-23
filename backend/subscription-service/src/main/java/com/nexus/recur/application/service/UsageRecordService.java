package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.UsageRecord;
import com.nexus.recur.domain.repository.UsageRecordRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.UsageDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;
    private final IdGenerator idGenerator;

    public UsageRecordService(UsageRecordRepository usageRecordRepository, IdGenerator idGenerator) {
        this.usageRecordRepository = usageRecordRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public UsageRecordResponse report(ReportUsageRequest request) {
        if (usageRecordRepository.existsByIdempotencyKey(request.idempotencyKey())) {
            throw new BusinessException("DUPLICATE_USAGE", "Usage record with this idempotency key already exists");
        }
        UsageRecord record = new UsageRecord();
        record.setId(idGenerator.next("usr"));
        record.setSubscriptionId(request.subscriptionId());
        record.setPlanId(request.planId());
        record.setQuantity(request.quantity());
        record.setUnitName(request.unitName());
        record.setIdempotencyKey(request.idempotencyKey());
        record.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : OffsetDateTime.now());
        usageRecordRepository.save(record);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<UsageRecordResponse> list(String subscriptionId, int page, int limit) {
        return usageRecordRepository.findBySubscriptionId(subscriptionId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long sumForPeriod(String subscriptionId, OffsetDateTime start, OffsetDateTime end) {
        List<UsageRecord> records = usageRecordRepository.findBySubscriptionIdAndRecordedAtBetween(subscriptionId, start, end);
        return records.stream().mapToLong(UsageRecord::getQuantity).sum();
    }

    private UsageRecordResponse toResponse(UsageRecord r) {
        return new UsageRecordResponse(r.getId(), r.getSubscriptionId(), r.getPlanId(),
                r.getQuantity(), r.getUnitName(), r.getIdempotencyKey(), r.getRecordedAt(), r.getCreatedAt());
    }
}
