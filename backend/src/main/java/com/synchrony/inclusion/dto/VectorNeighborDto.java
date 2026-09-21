package com.synchrony.inclusion.dto;

public record VectorNeighborDto(
        Long applicationId,
        String applicantRef,
        String decision,
        int score,
        double distance,
        double similarity
) {
}
