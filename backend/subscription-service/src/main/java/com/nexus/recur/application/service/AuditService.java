package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.AuditLog;
import com.nexus.recur.domain.repository.AuditLogRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final IdGenerator idGenerator;

    public AuditService(AuditLogRepository auditLogRepository, IdGenerator idGenerator) {
        this.auditLogRepository = auditLogRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actorId, String actorRole, String action,
                    String resourceType, String resourceId,
                    String ipAddress, String userAgent, String details) {
        AuditLog entry = new AuditLog();
        entry.setId(idGenerator.next("aud"));
        entry.setActorId(actorId);
        entry.setActorRole(actorRole);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setIpAddress(ipAddress);
        entry.setUserAgent(userAgent);
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResult<AuditLog> query(String actorId, String resourceType, String resourceId,
                                       String action, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result;
        if (actorId != null) {
            result = auditLogRepository.findByActorId(actorId, pageRequest);
        } else if (resourceType != null && resourceId != null) {
            result = auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageRequest);
        } else if (action != null) {
            result = auditLogRepository.findByAction(action, pageRequest);
        } else {
            result = auditLogRepository.findAll(pageRequest);
        }
        return new PageResult<>(result.getContent(), page, limit, result.getTotalElements());
    }
}
