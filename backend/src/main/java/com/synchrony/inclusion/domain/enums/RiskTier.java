package com.synchrony.inclusion.domain.enums;

/**
 * Synchrony risk bands used for unified pricing across credit products.
 * Higher score maps to a lower numbered, lower risk tier.
 */
public enum RiskTier {
    TIER_1_PRIME(740, 850),
    TIER_2_PRIME(680, 739),
    TIER_3_NEAR_PRIME(620, 679),
    TIER_4_SUBPRIME(560, 619),
    TIER_5_DEEP_SUBPRIME(300, 559);

    private final int minScore;
    private final int maxScore;

    RiskTier(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Maps a hybrid credit score onto the matching Synchrony risk band.
     */
    public static RiskTier fromScore(int score) {
        for (RiskTier tier : values()) {
            if (score >= tier.minScore && score <= tier.maxScore) {
                return tier;
            }
        }
        return TIER_5_DEEP_SUBPRIME;
    }
}
