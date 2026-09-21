package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.enums.DataSourceType;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureVector;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Converts raw linked data payloads into the normalized feature vector the
 * scoring engine consumes. Every derived feature is scaled to the range 0.0 to
 * 1.0 where 1.0 is the most favorable outcome. Only features backed by real
 * linked data are recorded, which lets the scoring engine drop empty buckets
 * and reallocate weight for unbanked applicants.
 */
@Service
public class FeatureExtractionService {

    /**
     * Builds a feature vector from a set of source payloads keyed by data type.
     *
     * @param payloads raw metrics per linked source; each value map holds
     *                 numeric fields captured from that stream
     */
    public FeatureVector extract(Map<DataSourceType, Map<String, Object>> payloads) {
        FeatureVector vector = new FeatureVector();
        if (payloads == null || payloads.isEmpty()) {
            return vector;
        }

        extractMobileMoney(vector, payloads.get(DataSourceType.MOBILE_MONEY));
        extractTelco(vector, payloads.get(DataSourceType.TELCO_CDR));
        extractUtility(vector, payloads.get(DataSourceType.UTILITY_PAYMENTS));
        extractBehavioral(vector, payloads.get(DataSourceType.BEHAVIORAL_SDK));
        extractGraph(vector, payloads.get(DataSourceType.TRANSACTION_GRAPH));
        extractBureau(vector, payloads.get(DataSourceType.BUREAU_TRADELINE));

        return vector;
    }

    private void extractMobileMoney(FeatureVector vector, Map<String, Object> p) {
        if (p == null) {
            return;
        }
        Double inflow = readDouble(p, "inflow");
        Double outflow = readDouble(p, "outflow");
        if (inflow != null && outflow != null && (inflow + outflow) > 0) {
            vector.put(FeatureCatalog.ALT_MOBILE_MONEY_INFLOW, inflow / (inflow + outflow));
        }
        Double avgBalance = readDouble(p, "avgDailyBalance");
        Double peakBalance = readDouble(p, "peakBalance");
        if (avgBalance != null && peakBalance != null && peakBalance > 0) {
            vector.put(FeatureCatalog.ALT_BALANCE_STABILITY, avgBalance / peakBalance);
        }
        Double settlementDays = readDouble(p, "settlementDays");
        if (settlementDays != null) {
            vector.put(FeatureCatalog.ALT_SETTLEMENT_VELOCITY, 1.0 - clamp(settlementDays / 30.0));
        }
        Double counterparties = readDouble(p, "counterpartyCount");
        if (counterparties != null) {
            vector.put(FeatureCatalog.ALT_COUNTERPARTY_DIVERSITY, clamp(counterparties / 50.0));
        }
    }

    private void extractTelco(FeatureVector vector, Map<String, Object> p) {
        if (p == null) {
            return;
        }
        Double consistency = readDouble(p, "topupConsistency");
        if (consistency != null) {
            vector.put(FeatureCatalog.ALT_TELCO_TOPUP_CONSISTENCY, consistency);
            return;
        }
        Double intervalCv = readDouble(p, "topupIntervalCv");
        if (intervalCv != null) {
            vector.put(FeatureCatalog.ALT_TELCO_TOPUP_CONSISTENCY, 1.0 - clamp(intervalCv));
        }
    }

    private void extractUtility(FeatureVector vector, Map<String, Object> p) {
        if (p == null) {
            return;
        }
        Double onTime = readDouble(p, "onTimePayments");
        Double total = readDouble(p, "totalPayments");
        if (onTime != null && total != null && total > 0) {
            vector.put(FeatureCatalog.ALT_UTILITY_PUNCTUALITY, onTime / total);
            return;
        }
        Double avgDaysLate = readDouble(p, "avgDaysLate");
        if (avgDaysLate != null) {
            vector.put(FeatureCatalog.ALT_UTILITY_PUNCTUALITY, 1.0 - clamp(avgDaysLate / 60.0));
        }
    }

    private void extractBehavioral(FeatureVector vector, Map<String, Object> p) {
        if (p == null) {
            return;
        }
        double sum = 0.0;
        int count = 0;
        Double typing = readDouble(p, "typingStability");
        if (typing != null) {
            sum += clamp(typing);
            count++;
        }
        Double appDiversity = readDouble(p, "appDiversityCount");
        if (appDiversity != null) {
            sum += clamp(appDiversity / 40.0);
            count++;
        }
        Double sessionRegularity = readDouble(p, "sessionRegularity");
        if (sessionRegularity != null) {
            sum += clamp(sessionRegularity);
            count++;
        }
        if (count > 0) {
            vector.put(FeatureCatalog.ALT_BEHAVIORAL_STABILITY, sum / count);
        }
    }

    private void extractGraph(FeatureVector vector, Map<String, Object> p) {
        if (p == null) {
            return;
        }
        Double networkStability = readDouble(p, "networkStability");
        if (networkStability != null) {
            vector.put(FeatureCatalog.GNN_NETWORK_STABILITY, networkStability);
        }
        Double fraudProximity = readDouble(p, "fraudProximity");
        if (fraudProximity != null) {
            vector.put(FeatureCatalog.GNN_FRAUD_RING_DISTANCE, 1.0 - clamp(fraudProximity));
        }
    }

    private void extractBureau(FeatureVector vector, Map<String, Object> p) {
        if (p == null) {
            return;
        }
        Double tradeLines = readDouble(p, "tradeLineCount");
        if (tradeLines != null) {
            vector.put(FeatureCatalog.TRAD_TRADELINE_DEPTH, clamp(tradeLines / 8.0));
        }
        Double utilization = readDouble(p, "utilization");
        if (utilization != null) {
            vector.put(FeatureCatalog.TRAD_UTILIZATION_HEALTH, 1.0 - clamp(utilization));
        }
    }

    private static Double readDouble(Map<String, Object> map, String key) {
        Object raw = map.get(key);
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(raw.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
