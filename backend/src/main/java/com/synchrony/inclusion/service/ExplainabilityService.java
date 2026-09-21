package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.scoring.Bucket;
import com.synchrony.inclusion.scoring.CounterfactualPlan;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureDefinition;
import com.synchrony.inclusion.scoring.FeatureVector;
import com.synchrony.inclusion.scoring.RecourseStep;
import com.synchrony.inclusion.scoring.ScoreResult;
import com.synchrony.inclusion.scoring.ScoringProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Turns a score result into an actionable recourse plan.
 *
 * <p>SHAP style attributions already explain why a decision was reached. They do
 * not tell a declined applicant what to change. This service solves the
 * counterfactual optimization from the research brief: find the minimal, feasible
 * perturbation vector that lifts the applicant to the approval logit.</p>
 *
 * <pre>
 *   a* = argmin cost(a | x)  subject to  f(x + a) >= approval logit
 * </pre>
 *
 * <p>Cost is the Max Percentile Shift, approximated here with a uniform empirical
 * CDF so cost equals the sum of absolute normalized feature changes. Because each
 * feature's marginal logit gain per unit change is constant, allocating change to
 * the highest gain-per-cost mutable features first is the exact minimizer.
 * Immutable features are held fixed.</p>
 */
@Service
public class ExplainabilityService {

    private final ScoringProperties properties;

    public ExplainabilityService(ScoringProperties properties) {
        this.properties = properties;
    }

    public CounterfactualPlan buildRecourse(ScoreResult result) {
        if (result.getDecision() == Decision.APPROVE) {
            return CounterfactualPlan.none();
        }

        double targetLogit = logitForScore(properties.getApproveThreshold());
        double requiredGain = targetLogit - result.getLogitZ();
        if (requiredGain <= 0) {
            return CounterfactualPlan.none();
        }

        FeatureVector vector = result.getFeatureVector();
        Map<Bucket, Double> bucketWeights = result.getBucketWeights();

        List<Candidate> candidates = new ArrayList<>();
        for (FeatureDefinition def : FeatureCatalog.all()) {
            if (!def.mutable()) {
                continue;
            }
            double weight = bucketWeights.getOrDefault(def.bucket(), 0.0);
            double slope = properties.getGamma() * weight * def.baseWeight();
            double current = vector.value(def.key());
            double headroom = 1.0 - current;
            if (slope > 0 && headroom > 1e-6) {
                candidates.add(new Candidate(def, current, slope, headroom));
            }
        }
        candidates.sort(Comparator.comparingDouble((Candidate c) -> c.slope).reversed());

        List<RecourseStep> steps = new ArrayList<>();
        double remaining = requiredGain;
        double achieved = 0.0;
        double totalShift = 0.0;

        for (Candidate c : candidates) {
            if (remaining <= 1e-9) {
                break;
            }
            double maxGain = c.slope * c.headroom;
            double take = Math.min(remaining, maxGain);
            double deltaX = take / c.slope;
            double target = c.current + deltaX;

            steps.add(new RecourseStep(
                    c.def.key(),
                    c.def.label(),
                    round(c.current),
                    round(target),
                    round(deltaX),
                    describe(c.def.label(), c.current, target)
            ));

            remaining -= take;
            achieved += take;
            totalShift += deltaX;
        }

        boolean feasible = remaining <= 1e-6;
        return new CounterfactualPlan(feasible, round(requiredGain), round(achieved), round(totalShift), steps);
    }

    /**
     * Inverse of the score mapping. Recovers the logit that produces a given
     * hybrid score, used as the counterfactual approval target.
     */
    double logitForScore(int score) {
        double pGood = (score - 300.0) / 550.0;
        pGood = Math.max(1e-6, Math.min(1.0 - 1e-6, pGood));
        return Math.log(pGood / (1.0 - pGood));
    }

    private String describe(String label, double current, double target) {
        int currentPct = (int) Math.round(current * 100);
        int targetPct = (int) Math.round(target * 100);
        return "Raise " + lowerFirst(label) + " from " + currentPct + "% to " + targetPct + "%.";
    }

    private String lowerFirst(String label) {
        if (label.isEmpty()) {
            return label;
        }
        return Character.toLowerCase(label.charAt(0)) + label.substring(1);
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }

    private record Candidate(FeatureDefinition def, double current, double slope, double headroom) {
    }
}
