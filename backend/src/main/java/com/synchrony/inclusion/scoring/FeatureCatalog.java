package com.synchrony.inclusion.scoring;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The single source of truth for model features. Feature keys, labels, group
 * membership and within-group weights all live here so the scoring engine, the
 * explainability layer and the dashboards stay in step.
 *
 * <p>Every feature is normalized so that a value of 1.0 is the most favorable
 * (lowest risk) and 0.0 is the least favorable. Within each bucket the base
 * weights sum to 1.0, which lets the bucket weight control group influence.</p>
 */
public final class FeatureCatalog {

    // Traditional bureau features
    public static final String TRAD_TRADELINE_DEPTH = "trad_tradeline_depth";
    public static final String TRAD_UTILIZATION_HEALTH = "trad_utilization_health";

    // Alternative data features
    public static final String ALT_MOBILE_MONEY_INFLOW = "alt_mobile_money_inflow";
    public static final String ALT_BALANCE_STABILITY = "alt_balance_stability";
    public static final String ALT_SETTLEMENT_VELOCITY = "alt_settlement_velocity";
    public static final String ALT_COUNTERPARTY_DIVERSITY = "alt_counterparty_diversity";
    public static final String ALT_TELCO_TOPUP_CONSISTENCY = "alt_telco_topup_consistency";
    public static final String ALT_UTILITY_PUNCTUALITY = "alt_utility_punctuality";
    public static final String ALT_BEHAVIORAL_STABILITY = "alt_behavioral_stability";

    // Graph neural network features
    public static final String GNN_NETWORK_STABILITY = "gnn_network_stability";
    public static final String GNN_FRAUD_RING_DISTANCE = "gnn_fraud_ring_distance";

    private static final List<FeatureDefinition> DEFINITIONS = List.of(
            new FeatureDefinition(TRAD_TRADELINE_DEPTH, "Trade line depth", Bucket.TRADITIONAL, 0.50, true),
            new FeatureDefinition(TRAD_UTILIZATION_HEALTH, "Credit utilization health", Bucket.TRADITIONAL, 0.50, true),

            new FeatureDefinition(ALT_MOBILE_MONEY_INFLOW, "Mobile money inflow ratio", Bucket.ALTERNATIVE, 0.18, true),
            new FeatureDefinition(ALT_BALANCE_STABILITY, "Average daily balance stability", Bucket.ALTERNATIVE, 0.16, true),
            new FeatureDefinition(ALT_SETTLEMENT_VELOCITY, "Payment settlement velocity", Bucket.ALTERNATIVE, 0.16, true),
            new FeatureDefinition(ALT_COUNTERPARTY_DIVERSITY, "Counterparty network diversity", Bucket.ALTERNATIVE, 0.12, true),
            new FeatureDefinition(ALT_TELCO_TOPUP_CONSISTENCY, "Telecom top-up consistency", Bucket.ALTERNATIVE, 0.12, true),
            new FeatureDefinition(ALT_UTILITY_PUNCTUALITY, "Utility payment punctuality", Bucket.ALTERNATIVE, 0.18, true),
            new FeatureDefinition(ALT_BEHAVIORAL_STABILITY, "App and device behavioral stability", Bucket.ALTERNATIVE, 0.08, true),

            new FeatureDefinition(GNN_NETWORK_STABILITY, "Transaction network stability", Bucket.GNN, 0.50, true),
            new FeatureDefinition(GNN_FRAUD_RING_DISTANCE, "Distance from known fraud clusters", Bucket.GNN, 0.50, false)
    );

    private static final Map<String, FeatureDefinition> BY_KEY = DEFINITIONS.stream()
            .collect(Collectors.toMap(FeatureDefinition::key, Function.identity()));

    private FeatureCatalog() {
    }

    public static List<FeatureDefinition> all() {
        return DEFINITIONS;
    }

    public static FeatureDefinition get(String key) {
        FeatureDefinition definition = BY_KEY.get(key);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown feature key: " + key);
        }
        return definition;
    }

    public static List<FeatureDefinition> inBucket(Bucket bucket) {
        return DEFINITIONS.stream().filter(d -> d.bucket() == bucket).toList();
    }

    public static List<String> keys() {
        return DEFINITIONS.stream().map(FeatureDefinition::key).toList();
    }
}
