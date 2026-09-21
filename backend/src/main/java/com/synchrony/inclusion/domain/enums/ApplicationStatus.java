package com.synchrony.inclusion.domain.enums;

/**
 * Lifecycle states for a credit application inside the origination flow.
 */
public enum ApplicationStatus {
    DRAFT,
    DATA_LINKED,
    EVALUATED,
    APPROVED,
    DECLINED,
    OVERRIDDEN
}
