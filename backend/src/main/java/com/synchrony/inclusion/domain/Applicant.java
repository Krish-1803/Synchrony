package com.synchrony.inclusion.domain;

import com.synchrony.inclusion.domain.enums.BankedStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * An applicant profile. The {@code anonymizedRef} is the only identifier that
 * leaves the service boundary toward the LLM layer. The optional
 * {@code protectedClass} field is stored for fair lending monitoring only and
 * is never read by the scoring engine.
 */
@Entity
@Table(name = "applicant")
@Getter
@Setter
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login that owns this applicant profile. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "display_name", length = 160)
    private String displayName;

    /** Stable pseudonymous token used in place of PII for downstream processing. */
    @Column(name = "anonymized_ref", nullable = false, unique = true, length = 64)
    private String anonymizedRef;

    @Column(length = 120)
    private String segment;

    @Enumerated(EnumType.STRING)
    @Column(name = "banked_status", nullable = false, length = 30)
    private BankedStatus bankedStatus = BankedStatus.THIN_FILE;

    /**
     * Held for fair lending audits only. Excluded from every feature vector and
     * screened out before model inference. Never used to price or decide.
     */
    @Column(name = "protected_class", length = 60)
    private String protectedClass;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
