package com.synchrony.inclusion.scoring;

import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.domain.enums.RiskTier;

import java.util.List;
import java.util.Map;

/**
 * Immutable output of one scoring run. Carries the score, the decision and
 * every intermediate value the audit trail and dashboards need.
 */
public class ScoreResult {

    private final int hybridScore;
    private final RiskTier riskTier;
    private final double probabilityOfDefault;
    private final double logitZ;
    private final Decision decision;
    private final Map<Bucket, Double> bucketWeights;
    private final List<Attribution> attributions;
    private final FeatureVector featureVector;
    private final String modelVersion;

    public ScoreResult(int hybridScore, RiskTier riskTier, double probabilityOfDefault, double logitZ,
                       Decision decision, Map<Bucket, Double> bucketWeights, List<Attribution> attributions,
                       FeatureVector featureVector, String modelVersion) {
        this.hybridScore = hybridScore;
        this.riskTier = riskTier;
        this.probabilityOfDefault = probabilityOfDefault;
        this.logitZ = logitZ;
        this.decision = decision;
        this.bucketWeights = bucketWeights;
        this.attributions = attributions;
        this.featureVector = featureVector;
        this.modelVersion = modelVersion;
    }

    public int getHybridScore() {
        return hybridScore;
    }

    public RiskTier getRiskTier() {
        return riskTier;
    }

    public double getProbabilityOfDefault() {
        return probabilityOfDefault;
    }

    public double getLogitZ() {
        return logitZ;
    }

    public Decision getDecision() {
        return decision;
    }

    public Map<Bucket, Double> getBucketWeights() {
        return bucketWeights;
    }

    public List<Attribution> getAttributions() {
        return attributions;
    }

    public FeatureVector getFeatureVector() {
        return featureVector;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Top positive risk drivers, most helpful first.
     */
    public List<Attribution> topPositiveDrivers(int limit) {
        return attributions.stream()
                .filter(a -> a.contribution() > 0)
                .sorted((a, b) -> Double.compare(b.contribution(), a.contribution()))
                .limit(limit)
                .toList();
    }

    /**
     * Top negative risk drivers, most harmful first.
     */
    public List<Attribution> topNegativeDrivers(int limit) {
        return attributions.stream()
                .filter(a -> a.contribution() < 0)
                .sorted((a, b) -> Double.compare(a.contribution(), b.contribution()))
                .limit(limit)
                .toList();
    }
}
