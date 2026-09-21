package com.synchrony.inclusion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.inclusion.domain.AuditLog;
import com.synchrony.inclusion.repository.AuditLogRepository;
import com.synchrony.inclusion.util.HashUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Writes immutable audit records. The service exposes only append and read
 * paths. There is no update or delete, which preserves the integrity of the
 * decision lineage that compliance auditors rely on.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public AuditLog record(String entityType, Long entityId, String action, String actor, Object detail) {
        String detailJson = toJson(detail);
        AuditLog entry = new AuditLog();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setActor(actor);
        entry.setDetailJson(detailJson);
        entry.setPayloadHash(HashUtil.sha256Hex(detailJson));
        return auditLogRepository.save(entry);
    }

    public List<AuditLog> recent() {
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc();
    }

    public List<AuditLog> forEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    private String toJson(Object detail) {
        if (detail == null) {
            return "{}";
        }
        if (detail instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
