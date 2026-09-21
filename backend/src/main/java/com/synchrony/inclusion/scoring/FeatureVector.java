package com.synchrony.inclusion.scoring;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A normalized feature vector for one applicant. Values are held in the range
 * 0.0 to 1.0. Features that were never linked are imputed to their baseline and
 * flagged so the explainability layer never claims an imputed value as a real
 * signal.
 */
public class FeatureVector {

    private final Map<String, Double> values = new LinkedHashMap<>();
    private final Set<String> presentFeatures = new TreeSet<>();
    private final Set<Bucket> presentBuckets = new TreeSet<>();

    /**
     * Records a feature that was derived from real linked data.
     */
    public void put(String key, double normalizedValue) {
        FeatureDefinition definition = FeatureCatalog.get(key);
        values.put(key, clamp(normalizedValue));
        presentFeatures.add(key);
        presentBuckets.add(definition.bucket());
    }

    /**
     * Returns the value for a feature, imputing the baseline when the feature
     * was not linked.
     */
    public double value(String key) {
        FeatureDefinition definition = FeatureCatalog.get(key);
        return values.getOrDefault(key, definition.baseline());
    }

    public boolean isPresent(String key) {
        return presentFeatures.contains(key);
    }

    public boolean hasBucket(Bucket bucket) {
        return presentBuckets.contains(bucket);
    }

    public Set<String> presentFeatures() {
        return Set.copyOf(presentFeatures);
    }

    /**
     * Full ordered snapshot of every catalog feature, with imputed baselines
     * filled in. Useful for persistence and embeddings.
     */
    public Map<String, Double> asOrderedMap() {
        Map<String, Double> snapshot = new LinkedHashMap<>();
        for (String key : FeatureCatalog.keys()) {
            snapshot.put(key, value(key));
        }
        return snapshot;
    }

    public static double clamp(double v) {
        if (Double.isNaN(v)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, v));
    }
}
