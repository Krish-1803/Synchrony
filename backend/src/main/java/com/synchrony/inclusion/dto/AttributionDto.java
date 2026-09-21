package com.synchrony.inclusion.dto;

public record AttributionDto(
        String key,
        String label,
        double value,
        double contribution,
        boolean positive
) {
}
