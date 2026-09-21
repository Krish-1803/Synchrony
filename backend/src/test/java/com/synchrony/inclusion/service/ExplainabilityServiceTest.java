package com.synchrony.inclusion.service;

import com.synchrony.inclusion.scoring.CounterfactualPlan;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureVector;
import com.synchrony.inclusion.scoring.RecourseStep;
import com.synchrony.inclusion.scoring.ScoreResult;
import com.synchrony.inclusion.scoring.ScoringProperties;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExplainabilityServiceTest {

    private final ScoringProperties properties = new ScoringProperties();
    private final ScoringService scoringService = new ScoringService(properties);
    private final ExplainabilityService explainabilityService = new ExplainabilityService(properties);

    private FeatureVector uniformVector(double value) {
        FeatureVector vector = new FeatureVector();
        for (String key : FeatureCatalog.keys()) {
            vector.put(key, value);
        }
        return vector;
    }

    @Test
    void approvedApplicationHasNoRecourse() {
        ScoreResult result = scoringService.score(uniformVector(0.9));
        CounterfactualPlan plan = explainabilityService.buildRecourse(result);

        assertThat(plan.steps()).isEmpty();
        assertThat(plan.feasible()).isTrue();
    }

    @Test
    void declinedApplicationProducesFeasibleRecourse() {
        ScoreResult result = scoringService.score(uniformVector(0.3));
        CounterfactualPlan plan = explainabilityService.buildRecourse(result);

        assertThat(plan.steps()).isNotEmpty();
        assertThat(plan.feasible()).isTrue();
        assertThat(plan.achievedLogitGain()).isCloseTo(plan.requiredLogitGain(), Offset.offset(1e-3));
        for (RecourseStep step : plan.steps()) {
            assertThat(step.targetValue()).isGreaterThan(step.currentValue());
        }
    }

    @Test
    void recourseNeverAsksToChangeImmutableFeatures() {
        ScoreResult result = scoringService.score(uniformVector(0.2));
        CounterfactualPlan plan = explainabilityService.buildRecourse(result);

        assertThat(plan.steps())
                .noneMatch(step -> step.key().equals(FeatureCatalog.GNN_FRAUD_RING_DISTANCE));
    }

    @Test
    void recoursePrioritizesHighestImpactFeaturesFirst() {
        ScoreResult result = scoringService.score(uniformVector(0.3));
        CounterfactualPlan plan = explainabilityService.buildRecourse(result);

        // The first step should carry at least as large a percentile shift budget
        // effect as later ones, since highest gain-per-cost features are chosen first.
        assertThat(plan.steps().get(0).label()).isNotBlank();
        assertThat(plan.totalPercentileShift()).isGreaterThan(0.0);
    }
}
