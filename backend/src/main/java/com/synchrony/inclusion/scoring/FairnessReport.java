package com.synchrony.inclusion.scoring;

import java.util.List;

/**
 * Aggregate fair lending report across monitored groups.
 *
 * @param groups                per-group approval and true positive rates
 * @param disparateImpactRatio  lowest group approval rate over highest, the 80 percent rule metric
 * @param demographicParityRatio same ratio, reported under its parity name
 * @param equalOpportunityDiff  spread in true positive rates across groups
 * @param compliant             true when the disparate impact ratio is at least 0.80
 * @param sampleSize            number of observations evaluated
 */
public record FairnessReport(
        List<GroupFairness> groups,
        double disparateImpactRatio,
        double demographicParityRatio,
        double equalOpportunityDiff,
        boolean compliant,
        int sampleSize
) {
    /**
     * Per-group fairness statistics.
     *
     * @param group           monitored group label
     * @param count           observations in the group
     * @param approvalRate    share of the group that was approved
     * @param truePositiveRate share of creditworthy applicants in the group that was approved
     */
    public record GroupFairness(String group, int count, double approvalRate, double truePositiveRate) {
    }
}
