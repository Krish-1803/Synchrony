package com.synchrony.inclusion.domain;

import com.synchrony.inclusion.domain.enums.DataSourceType;
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
 * A single linked alternative data stream for an applicant. The raw payload is
 * stored as JSON. A SHA-256 hash of the payload is retained so auditors can
 * verify that a decision was reproduced from the exact input it consumed.
 */
@Entity
@Table(name = "alternative_data_record")
@Getter
@Setter
public class AlternativeDataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private DataSourceType sourceType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt = Instant.now();
}
