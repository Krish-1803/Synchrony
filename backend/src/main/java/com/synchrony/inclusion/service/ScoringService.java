package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.domain.enums.RiskTier;
import com.synchrony.inclusion.scoring.Attribution;
import com.synchrony.inclusion.scoring.Bucket;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureDefinition;
import com.synchrony.inclusion.scoring.FeatureVector;
import com.synchrony.inclusion.scoring.ScoreResult;
import com.synchrony.inclusion.scoring.ScoringProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The hybrid scoring engine. It combines traditional bureau features,
 * alternative data telemetry and graph network signals into a single logit,
 * then maps that logit onto the Synchrony 300 to 850 score band.
 *
 * <p>The model is an additive scorecard, which makes every contribution exactly
 * explainable. For a linear model the SHAP value of a feature reduces to
 * {@code weight * (value - baseline)}, so the attributions this engine emits
 * satisfy the SHAP local accuracy and consistency properties by construction.</p>
 *
 * <p>Score formula, matching the research brief:</p>
 * <pre>
 *   z          = intercept + sum_i gamma * w_bucket(i) * baseWeight_i * (x_i - baseline_i)
 *   p_good     = sigmoid(z)
 *   S_hybrid   = 300 + 550 * p_good
 *   PD         = 1 - p_good
 * </pre>
 */
@Service
public class ScoringService {

    private final ScoringProperties properties;

    public ScoringService(ScoringProperties properties) {
        this.properties = properties;
    }

    public ScoreResult score(FeatureVector featureVector) {
        Map<Bucket, Double> bucketWeights = resolveBucketWeights(featureVector);

        List<Attribution> attributions = new ArrayList<>();
        double z = properties.getIntercept();
        for (FeatureDefinition def : FeatureCatalog.all()) {
            double weight = bucketWeights.getOrDefault(def.bucket(), 0.0);
            double value = featureVector.value(def.key());
            double contribution = properties.getGamma() * weight * def.baseWeight() * (value - def.baseline());
            z += contribution;
            attributions.add(new Attribution(def.key(), def.label(), value, contribution));
        }

        double pGood = sigmoid(z);
        int score = (int) Math.round(300.0 + 550.0 * pGood);
        double pd = 1.0 - pGood;
        RiskTier tier = RiskTier.fromScore(score);
        Decision decision = resolveDecision(score);

        return new ScoreResult(score, tier, pd, z, decision, bucketWeights, attributions,
                featureVector, properties.getModelVersion());
    }

    /**
     * Resolves the effective weight for each bucket. Buckets with no linked data
     * drop to zero and the remaining buckets are renormalized so the present
     * weights sum to one. This is the dynamic imputation path for unbanked
     * applicants where the traditional weight becomes zero.
     */
    Map<Bucket, Double> resolveBucketWeights(FeatureVector featureVector) {
        Map<Bucket, Double> weights = new EnumMap<>(Bucket.class);
        double total = 0.0;
        for (Bucket bucket : Bucket.values()) {
            if (featureVector.hasBucket(bucket)) {
                weights.put(bucket, bucket.getDefaultWeight());
                total += bucket.getDefaultWeight();
            } else {
                weights.put(bucket, 0.0);
            }
        }
        if (total > 0.0) {
            for (Bucket bucket : Bucket.values()) {
                weights.put(bucket, weights.get(bucket) / total);
            }
        }
        return weights;
    }

    private Decision resolveDecision(int score) {
        if (score >= properties.getApproveThreshold()) {
            return Decision.APPROVE;
        }
        if (score < properties.getDeclineThreshold()) {
            return Decision.DECLINE;
        }
        return Decision.REFER;
    }

    static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}
