package com.synchrony.inclusion.dto;

import com.synchrony.inclusion.domain.enums.BankedStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-service applicant registration. Officers are provisioned separately by an
 * administrator, so this endpoint only ever creates applicant accounts.
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 120) String username,
        @NotBlank @Size(min = 8, max = 120) String password,
        @NotBlank String fullName,
        String displayName,
        String segment,
        BankedStatus bankedStatus,
        String protectedClass
) {
}
