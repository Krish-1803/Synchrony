package com.synchrony.inclusion.dto;

public record MeResponse(
        String username,
        String role,
        String fullName,
        Long applicantId
) {
}
