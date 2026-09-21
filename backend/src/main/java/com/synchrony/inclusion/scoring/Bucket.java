package com.synchrony.inclusion.scoring;

/**
 * Feature groups in the hybrid model. Each group carries a default weight in
 * the unified logit. When an applicant has no traditional bureau data the
 * traditional weight drops to zero and the remaining groups are renormalized.
 */
public enum Bucket {

    TRADITIONAL(0.35),
    ALTERNATIVE(0.45),
    GNN(0.20);

    private final double defaultWeight;

    Bucket(double defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }
}
