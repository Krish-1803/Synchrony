package com.synchrony.inclusion.dto;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        String entityType,
        Long entityId,
        String action,
        String actor,
        String detailJson,
        String payloadHash,
        Instant createdAt
) {
}
