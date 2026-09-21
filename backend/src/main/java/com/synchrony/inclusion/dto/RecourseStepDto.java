package com.synchrony.inclusion.dto;

public record RecourseStepDto(
        String key,
        String label,
        double currentValue,
        double targetValue,
        double percentileShift,
        String description
) {
}
