package com.synchrony.inclusion.dto;

public record SystemStatusResponse(
        boolean bedrockEnabled,
        boolean pgvectorEnabled,
        String modelVersion,
        int approveThreshold,
        int declineThreshold
) {
}
