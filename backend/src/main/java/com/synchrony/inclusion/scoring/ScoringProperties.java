package com.synchrony.inclusion.scoring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunable constants for the hybrid scoring engine. Values bind from the
 * {@code synchrony.scoring} block in application configuration so risk strategy
 * can be adjusted without a code change.
 */
@ConfigurationProperties(prefix = "synchrony.scoring")
public class ScoringProperties {

    /** Logit spread constant. Larger values sharpen the score distribution. */
    private double gamma = 8.0;

    /** Logit intercept. */
    private double intercept = 0.0;

    /** Scores at or above this cutoff are approved. */
    private int approveThreshold = 640;

    /** Scores below this cutoff are declined. Scores between the two are referred. */
    private int declineThreshold = 580;

    /** Model version stamped onto every assessment for audit lineage. */
    private String modelVersion = "hybrid-gnn-alt-1.0.0";

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

    public int getApproveThreshold() {
        return approveThreshold;
    }

    public void setApproveThreshold(int approveThreshold) {
        this.approveThreshold = approveThreshold;
    }

    public int getDeclineThreshold() {
        return declineThreshold;
    }

    public void setDeclineThreshold(int declineThreshold) {
        this.declineThreshold = declineThreshold;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }
}
