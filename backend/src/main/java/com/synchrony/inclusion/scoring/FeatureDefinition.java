package com.synchrony.inclusion.scoring;

/**
 * Static metadata for one model feature.
 *
 * @param key           stable machine key
 * @param label         human-readable label for dashboards and adverse action notices
 * @param bucket        the feature group this belongs to
 * @param baseWeight    within-bucket weight, normalized so each bucket sums to 1.0
 * @param mutable       whether an applicant can realistically change this feature
 * @param baseline      population midpoint used as the SHAP reference value
 */
public record FeatureDefinition(
        String key,
        String label,
        Bucket bucket,
        double baseWeight,
        boolean mutable,
        double baseline
) {
    public FeatureDefinition(String key, String label, Bucket bucket, double baseWeight, boolean mutable) {
        this(key, label, bucket, baseWeight, mutable, 0.5);
    }
}
