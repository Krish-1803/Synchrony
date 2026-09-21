package com.synchrony.inclusion.scoring;

/**
 * One additive feature attribution, matching the SHAP local accuracy property.
 * The sum of every contribution plus the model intercept equals the logit z.
 *
 * @param key          feature key
 * @param label        human-readable label
 * @param value        normalized feature value used in scoring
 * @param contribution signed contribution to the logit; positive raises the score
 */
public record Attribution(String key, String label, double value, double contribution) {

    public boolean isPositive() {
        return contribution >= 0;
    }
}
