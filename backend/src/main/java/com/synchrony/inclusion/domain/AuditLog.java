package com.synchrony.inclusion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Append-only audit record. The service exposes no update or delete path for
 * this table. Each entry carries a SHA-256 hash of the referenced payload so a
 * compliance auditor can reproduce and verify any historical credit decision.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "actor", length = 120)
    private String actor;

    @Column(name = "detail_json", columnDefinition = "text")
    private String detailJson;

    @Column(name = "payload_hash", length = 64)
    private String payloadHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
