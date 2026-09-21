package com.synchrony.inclusion.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Full underwriting view for a credit officer. It exposes the complete feature
 * attribution, the bucket weighting, the explainable rationale, the recourse
 * plan and the nearest historical cases from the vector store.
 */
public record UnderwritingDetailResponse(
        ApplicationResponse application,
        String decision,
        int score,
        String riskTier,
        double pdPercent,
        double logitZ,
        String modelVersion,
        Map<String, Double> bucketWeights,
        Map<String, Double> featureVector,
        List<AttributionDto> attributions,
        List<AttributionDto> topPositiveDrivers,
        List<AttributionDto> topNegativeDrivers,
        String rationale,
        List<String> principalReasons,
        List<RecourseStepDto> recourse,
        String recourseSummary,
        boolean recourseFeasible,
        String explanationSource,
        boolean manualOverride,
        String overrideReason,
        List<VectorNeighborDto> similarCases,
        Instant createdAt
) {
}
