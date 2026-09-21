package com.synchrony.inclusion.dto;

import com.synchrony.inclusion.domain.enums.Decision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A credit officer's manual override of a model decision. A reason is required
 * and is written to the audit trail.
 */
public record OverrideRequest(
        @NotNull Decision decision,
        @NotBlank String reason
) {
}
