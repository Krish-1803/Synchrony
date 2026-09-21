package com.synchrony.inclusion.scoring;

/**
 * One decision outcome used for fair lending monitoring. The protected class is
 * used only for measuring disparate impact after the fact. It never enters the
 * scoring engine.
 *
 * @param protectedClass monitored demographic group label
 * @param approved       whether the application was approved
 * @param repaid         ground-truth repayment outcome, or null when unknown
 */
public record FairnessObservation(String protectedClass, boolean approved, Boolean repaid) {
}
