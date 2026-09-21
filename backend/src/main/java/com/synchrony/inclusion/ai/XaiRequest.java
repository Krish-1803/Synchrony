package com.synchrony.inclusion.ai;

import java.util.List;

/**
 * Structured, anonymized input for the Explainable AI orchestrator. It carries
 * only model outputs and non-identifying context. No PII is present by the time
 * a value of this type is built.
 *
 * @param decision         final decision label
 * @param score            hybrid credit score
 * @param riskTier         Synchrony risk band
 * @param pdPercent        probability of default as a percentage
 * @param productType      requested credit product
 * @param segment          non-identifying applicant segment
 * @param bankedStatus     banked, thin file or unbanked
 * @param positiveDrivers  top favorable feature labels
 * @param negativeDrivers  top unfavorable feature labels
 * @param recourseSteps    actionable recourse instructions
 */
public record XaiRequest(
        String decision,
        int score,
        String riskTier,
        double pdPercent,
        String productType,
        String segment,
        String bankedStatus,
        List<String> positiveDrivers,
        List<String> negativeDrivers,
        List<String> recourseSteps
) {
}
