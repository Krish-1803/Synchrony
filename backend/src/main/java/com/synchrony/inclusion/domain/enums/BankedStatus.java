package com.synchrony.inclusion.domain.enums;

/**
 * Describes how much traditional bureau history an applicant carries.
 *
 * <p>THIN_FILE applicants have a small number of trade lines. UNBANKED
 * applicants have no bureau record, so the scoring engine reallocates weight
 * away from traditional features toward alternative data and network signals.</p>
 */
public enum BankedStatus {
    BANKED,
    THIN_FILE,
    UNBANKED
}
