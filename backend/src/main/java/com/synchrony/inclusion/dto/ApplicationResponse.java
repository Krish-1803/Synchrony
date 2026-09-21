package com.synchrony.inclusion.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Summary view of a credit application, used in lists and confirmations.
 */
public record ApplicationResponse(
        Long id,
        Long applicantId,
        String applicantRef,
        String applicantSegment,
        String bankedStatus,
        String productType,
        BigDecimal requestedAmount,
        String status,
        Instant createdAt,
        Integer score,
        String riskTier,
        String decision,
        Integer linkedSources
) {
}
