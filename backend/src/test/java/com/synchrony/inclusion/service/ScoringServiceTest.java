package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.scoring.Attribution;
import com.synchrony.inclusion.scoring.Bucket;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureVector;
import com.synchrony.inclusion.scoring.ScoreResult;
import com.synchrony.inclusion.scoring.ScoringProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService(new ScoringProperties());

    private FeatureVector uniformVector(double value) {
        FeatureVector vector = new FeatureVector();
        for (String key : FeatureCatalog.keys()) {
            vector.put(key, value);
        }
        return vector;
    }

    @Test
    void strongProfileApprovesInPrimeBand() {
        ScoreResult result = scoringService.score(uniformVector(0.9));

        assertThat(result.getHybridScore()).isGreaterThan(640);
        assertThat(result.getDecision()).isEqualTo(Decision.APPROVE);
        assertThat(result.getProbabilityOfDefault()).isLessThan(0.1);
    }

    @Test
    void weakProfileDeclinesInSubprimeBand() {
        ScoreResult result = scoringService.score(uniformVector(0.1));

        assertThat(result.getHybridScore()).isLessThan(580);
        assertThat(result.getDecision()).isEqualTo(Decision.DECLINE);
    }

    @Test
    void scoreStaysWithinSynchronyBand() {
        assertThat(scoringService.score(uniformVector(1.0)).getHybridScore()).isBetween(300, 850);
        assertThat(scoringService.score(uniformVector(0.0)).getHybridScore()).isBetween(300, 850);
    }

    @Test
    void unbankedApplicantReallocatesTraditionalWeightToAlternativeAndGnn() {
        FeatureVector vector = new FeatureVector();
        // No traditional bureau features linked. Strong alternative and network signals.
        vector.put(FeatureCatalog.ALT_MOBILE_MONEY_INFLOW, 0.9);
        vector.put(FeatureCatalog.ALT_BALANCE_STABILITY, 0.9);
        vector.put(FeatureCatalog.ALT_SETTLEMENT_VELOCITY, 0.9);
        vector.put(FeatureCatalog.ALT_COUNTERPARTY_DIVERSITY, 0.9);
        vector.put(FeatureCatalog.ALT_TELCO_TOPUP_CONSISTENCY, 0.9);
        vector.put(FeatureCatalog.ALT_UTILITY_PUNCTUALITY, 0.9);
        vector.put(FeatureCatalog.ALT_BEHAVIORAL_STABILITY, 0.9);
        vector.put(FeatureCatalog.GNN_NETWORK_STABILITY, 0.9);
        vector.put(FeatureCatalog.GNN_FRAUD_RING_DISTANCE, 0.9);

        ScoreResult result = scoringService.score(vector);

        assertThat(result.getBucketWeights().get(Bucket.TRADITIONAL)).isZero();
        assertThat(result.getBucketWeights().get(Bucket.ALTERNATIVE)
                + result.getBucketWeights().get(Bucket.GNN)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(result.getDecision()).isEqualTo(Decision.APPROVE);
    }

    @Test
    void attributionsAreAdditiveAndReconstructTheLogit() {
        ScoreResult result = scoringService.score(uniformVector(0.7));

        double sum = result.getAttributions().stream().mapToDouble(Attribution::contribution).sum();
        assertThat(sum).isCloseTo(result.getLogitZ(), org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void emptyVectorProducesNeutralMidpointScore() {
        ScoreResult result = scoringService.score(new FeatureVector());
        // No data means logit equals the intercept, which is zero, so p_good is 0.5.
        assertThat(result.getHybridScore()).isEqualTo(575);
    }
}
