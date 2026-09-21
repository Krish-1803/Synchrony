package com.synchrony.inclusion.domain;

import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.domain.enums.RiskTier;
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
 * The output of one scoring run against an application. Every numerical input,
 * the resolved decision, the feature attribution vector and the counterfactual
 * recourse plan are persisted so any historical decision can be reproduced.
 */
@Entity
@Table(name = "risk_assessment")
@Getter
@Setter
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "hybrid_score", nullable = false)
    private int hybridScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tier", nullable = false, length = 40)
    private RiskTier riskTier;

    /** Probability of default estimate in the range 0 to 1. */
    @Column(name = "pd_estimate", nullable = false)
    private double pdEstimate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Decision decision;

    @Column(name = "logit_z", nullable = false)
    private double logitZ;

    @Column(name = "weight_traditional", nullable = false)
    private double weightTraditional;

    @Column(name = "weight_gnn", nullable = false)
    private double weightGnn;

    @Column(name = "weight_alternative", nullable = false)
    private double weightAlternative;

    /** Normalized feature vector as JSON. */
    @Column(name = "feature_json", nullable = false, columnDefinition = "text")
    private String featureJson;

    /** SHAP style additive attributions as JSON. */
    @Column(name = "attribution_json", nullable = false, columnDefinition = "text")
    private String attributionJson;

    /** Counterfactual recourse plan as JSON. Empty for approvals. */
    @Column(name = "counterfactual_json", columnDefinition = "text")
    private String counterfactualJson;

    /** Dense embedding of the feature vector as a JSON array, kept for fallback similarity search. */
    @Column(name = "embedding_json", columnDefinition = "text")
    private String embeddingJson;

    /** Plain-language rationale produced by the Explainable AI orchestrator. */
    @Column(name = "rationale", columnDefinition = "text")
    private String rationale;

    /** True when a credit officer overrode the model decision. */
    @Column(name = "manual_override", nullable = false)
    private boolean manualOverride = false;

    @Column(name = "override_reason", length = 500)
    private String overrideReason;

    /**
     * Realized repayment outcome for fair lending monitoring. Null while the
     * outcome is still unknown. Populated for historical and seeded decisions.
     */
    @Column(name = "outcome_repaid")
    private Boolean outcomeRepaid;

    @Column(name = "model_version", nullable = false, length = 40)
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
