package com.synchrony.inclusion.scoring;

/**
 * One actionable step in a counterfactual recourse plan. Each step names a
 * mutable feature an applicant can improve, the target value that helps flip a
 * decline to an approval and the percentile shift that improvement represents.
 *
 * @param key             feature key
 * @param label           human-readable label
 * @param currentValue    the applicant's current normalized value
 * @param targetValue     the suggested normalized value
 * @param percentileShift the absolute shift used in the Max Percentile Shift cost
 * @param description     plain-language instruction for the applicant
 */
public record RecourseStep(
        String key,
        String label,
        double currentValue,
        double targetValue,
        double percentileShift,
        String description
) {
}
