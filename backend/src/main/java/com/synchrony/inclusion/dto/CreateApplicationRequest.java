package com.synchrony.inclusion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateApplicationRequest(
        @NotBlank String productType,
        @NotNull @DecimalMin("100.0") BigDecimal requestedAmount
) {
}
