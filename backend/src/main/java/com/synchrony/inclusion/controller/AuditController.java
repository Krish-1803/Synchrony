package com.synchrony.inclusion.controller;

import com.synchrony.inclusion.dto.AuditLogDto;
import com.synchrony.inclusion.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only access to the immutable audit trail. Restricted to credit officers.
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('CREDIT_OFFICER')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogDto>> recent() {
        return ResponseEntity.ok(auditService.recent().stream()
                .map(log -> new AuditLogDto(log.getId(), log.getEntityType(), log.getEntityId(),
                        log.getAction(), log.getActor(), log.getDetailJson(), log.getPayloadHash(),
                        log.getCreatedAt()))
                .toList());
    }

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<List<AuditLogDto>> forEntity(@PathVariable String type, @PathVariable Long id) {
        return ResponseEntity.ok(auditService.forEntity(type, id).stream()
                .map(log -> new AuditLogDto(log.getId(), log.getEntityType(), log.getEntityId(),
                        log.getAction(), log.getActor(), log.getDetailJson(), log.getPayloadHash(),
                        log.getCreatedAt()))
                .toList());
    }
}
