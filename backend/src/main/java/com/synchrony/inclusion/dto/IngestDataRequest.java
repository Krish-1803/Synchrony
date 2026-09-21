package com.synchrony.inclusion.dto;

import com.synchrony.inclusion.domain.enums.DataSourceType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Links one alternative data stream to an application. The payload holds the raw
 * numeric metrics for that stream. Several streams are linked with repeated calls.
 */
public record IngestDataRequest(
        @NotNull DataSourceType sourceType,
        @NotNull Map<String, Object> payload
) {
}
