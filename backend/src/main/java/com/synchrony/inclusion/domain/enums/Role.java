package com.synchrony.inclusion.domain.enums;

/**
 * Access roles used by the Role-Based Access Control layer.
 *
 * <p>APPLICANT can link data streams and view their own decision and recourse.
 * CREDIT_OFFICER can review any applicant, inspect feature attribution and
 * apply manual overrides from the underwriting dashboard.</p>
 */
public enum Role {
    APPLICANT,
    CREDIT_OFFICER
}
