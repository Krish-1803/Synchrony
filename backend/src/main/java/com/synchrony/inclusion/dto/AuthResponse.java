package com.synchrony.inclusion.dto;

public record AuthResponse(
        String token,
        String username,
        String role,
        String fullName,
        long expiresInMinutes
) {
}
