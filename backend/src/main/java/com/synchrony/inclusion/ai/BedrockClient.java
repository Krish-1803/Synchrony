package com.synchrony.inclusion.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * Thin wrapper around the AWS Bedrock runtime. The client is created lazily and
 * only when the orchestrator is enabled. Every call is guarded so a missing
 * credential or a network error degrades to the deterministic fallback rather
 * than failing a credit decision.
 */
@Component
public class BedrockClient {

    private static final Logger log = LoggerFactory.getLogger(BedrockClient.class);

    private final BedrockProperties properties;
    private final ObjectMapper objectMapper;
    private volatile BedrockRuntimeClient runtimeClient;

    public BedrockClient(BedrockProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Invokes the configured model with a system and user prompt. Returns the raw
     * response body JSON, or null when the call could not be made.
     */
    public String invoke(String systemPrompt, String userPrompt) {
        if (!properties.isEnabled()) {
            return null;
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("anthropic_version", "bedrock-2023-05-31");
            body.put("max_tokens", properties.getMaxTokens());
            body.put("temperature", properties.getTemperature());
            body.put("system", systemPrompt);

            ArrayNode messages = body.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            ArrayNode content = userMessage.putArray("content");
            ObjectNode textBlock = content.addObject();
            textBlock.put("type", "text");
            textBlock.put("text", userPrompt);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(properties.getModelId())
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(body)))
                    .build();

            InvokeModelResponse response = client().invokeModel(request);
            return response.body().asUtf8String();
        } catch (Exception ex) {
            log.warn("Bedrock invoke failed, using deterministic fallback: {}", ex.getMessage());
            return null;
        }
    }

    private BedrockRuntimeClient client() {
        BedrockRuntimeClient local = runtimeClient;
        if (local == null) {
            synchronized (this) {
                local = runtimeClient;
                if (local == null) {
                    local = BedrockRuntimeClient.builder()
                            .region(Region.of(properties.getRegion()))
                            .build();
                    runtimeClient = local;
                }
            }
        }
        return local;
    }
}
