package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.enums.DataSourceType;
import com.synchrony.inclusion.scoring.Bucket;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureVector;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureExtractionServiceTest {

    private final FeatureExtractionService service = new FeatureExtractionService();
    private static final Offset<Double> TOL = Offset.offset(1e-6);

    @Test
    void convertsMobileMoneyMetricsToNormalizedFeatures() {
        Map<DataSourceType, Map<String, Object>> payloads = new HashMap<>();
        payloads.put(DataSourceType.MOBILE_MONEY, Map.of(
                "inflow", 3000,
                "outflow", 1000,
                "avgDailyBalance", 500,
                "peakBalance", 1000,
                "settlementDays", 6,
                "counterpartyCount", 25));

        FeatureVector vector = service.extract(payloads);

        assertThat(vector.value(FeatureCatalog.ALT_MOBILE_MONEY_INFLOW)).isCloseTo(0.75, TOL);
        assertThat(vector.value(FeatureCatalog.ALT_BALANCE_STABILITY)).isCloseTo(0.5, TOL);
        assertThat(vector.value(FeatureCatalog.ALT_SETTLEMENT_VELOCITY)).isCloseTo(0.8, TOL);
        assertThat(vector.value(FeatureCatalog.ALT_COUNTERPARTY_DIVERSITY)).isCloseTo(0.5, TOL);
        assertThat(vector.hasBucket(Bucket.ALTERNATIVE)).isTrue();
        assertThat(vector.hasBucket(Bucket.TRADITIONAL)).isFalse();
    }

    @Test
    void utilityPunctualityIsRatioOfOnTimePayments() {
        Map<DataSourceType, Map<String, Object>> payloads = Map.of(
                DataSourceType.UTILITY_PAYMENTS, Map.of("onTimePayments", 20, "totalPayments", 25));

        FeatureVector vector = service.extract(payloads);

        assertThat(vector.value(FeatureCatalog.ALT_UTILITY_PUNCTUALITY)).isCloseTo(0.8, TOL);
    }

    @Test
    void bureauUtilizationIsInvertedSoLowUtilizationIsFavorable() {
        Map<DataSourceType, Map<String, Object>> payloads = Map.of(
                DataSourceType.BUREAU_TRADELINE, Map.of("tradeLineCount", 4, "utilization", 0.25));

        FeatureVector vector = service.extract(payloads);

        assertThat(vector.value(FeatureCatalog.TRAD_TRADELINE_DEPTH)).isCloseTo(0.5, TOL);
        assertThat(vector.value(FeatureCatalog.TRAD_UTILIZATION_HEALTH)).isCloseTo(0.75, TOL);
        assertThat(vector.hasBucket(Bucket.TRADITIONAL)).isTrue();
    }

    @Test
    void fraudProximityIsInvertedIntoDistanceFromFraudClusters() {
        Map<DataSourceType, Map<String, Object>> payloads = Map.of(
                DataSourceType.TRANSACTION_GRAPH, Map.of("networkStability", 0.8, "fraudProximity", 0.2));

        FeatureVector vector = service.extract(payloads);

        assertThat(vector.value(FeatureCatalog.GNN_NETWORK_STABILITY)).isCloseTo(0.8, TOL);
        assertThat(vector.value(FeatureCatalog.GNN_FRAUD_RING_DISTANCE)).isCloseTo(0.8, TOL);
        assertThat(vector.hasBucket(Bucket.GNN)).isTrue();
    }

    @Test
    void behavioralStabilityAveragesAvailableSignals() {
        Map<DataSourceType, Map<String, Object>> payloads = Map.of(
                DataSourceType.BEHAVIORAL_SDK,
                Map.of("typingStability", 0.8, "appDiversityCount", 20, "sessionRegularity", 0.8));

        FeatureVector vector = service.extract(payloads);

        // (0.8 + 0.5 + 0.8) / 3
        assertThat(vector.value(FeatureCatalog.ALT_BEHAVIORAL_STABILITY)).isCloseTo(0.7, TOL);
    }

    @Test
    void missingFeaturesImputeTheBaseline() {
        FeatureVector vector = service.extract(Map.of());
        assertThat(vector.value(FeatureCatalog.ALT_UTILITY_PUNCTUALITY)).isCloseTo(0.5, TOL);
        assertThat(vector.isPresent(FeatureCatalog.ALT_UTILITY_PUNCTUALITY)).isFalse();
    }
}
