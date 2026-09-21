package com.synchrony.inclusion.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the AWS Bedrock Explainable AI orchestrator. When disabled,
 * or when no credentials are present, the orchestrator falls back to a
 * deterministic template so the prototype runs end to end without cloud access.
 */
@ConfigurationProperties(prefix = "synchrony.bedrock")
public class BedrockProperties {

    private boolean enabled = false;
    private String region = "us-east-1";
    private String modelId = "anthropic.claude-3-5-sonnet-20240620-v1:0";
    private int maxTokens = 900;
    private double temperature = 0.2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
