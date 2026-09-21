package com.synchrony.inclusion.dto;

import java.time.Instant;
import java.util.List;

/**
 * Applicant-facing view of a decision. It shows the outcome, the plain-language
 * rationale, the main drivers and, for a decline, the path to approval. It never
 * exposes raw model internals such as logits or bucket weights.
 */
public record AssessmentResponse(
        Long applicationId,
        String decision,
        int score,
        String riskTier,
        double pdPercent,
        String rationale,
        List<String> principalReasons,
        List<String> positiveDrivers,
        List<RecourseStepDto> recourse,
        String recourseSummary,
        boolean recourseFeasible,
        String explanationSource,
        boolean manualOverride,
        Instant createdAt
) {
}
