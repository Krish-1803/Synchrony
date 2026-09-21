package com.synchrony.inclusion.scoring;

import java.util.List;

/**
 * A counterfactual recourse plan. It lists the minimal actionable feature
 * changes that move a declined applicant toward approval, ranked by cost.
 *
 * @param feasible            true when the listed steps reach the approval logit
 * @param requiredLogitGain   logit distance to the approval threshold
 * @param achievedLogitGain   logit gain the listed steps deliver
 * @param totalPercentileShift the Max Percentile Shift cost of the full plan
 * @param steps               ordered recourse steps, lowest cost first
 */
public record CounterfactualPlan(
        boolean feasible,
        double requiredLogitGain,
        double achievedLogitGain,
        double totalPercentileShift,
        List<RecourseStep> steps
) {
    public static CounterfactualPlan none() {
        return new CounterfactualPlan(true, 0.0, 0.0, 0.0, List.of());
    }
}
