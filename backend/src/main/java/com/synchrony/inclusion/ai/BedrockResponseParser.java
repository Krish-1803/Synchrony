package com.synchrony.inclusion.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses AWS Bedrock responses for the Explainable AI orchestrator.
 *
 * <p>Two layers are parsed. First the raw Bedrock invoke response envelope, whose
 * {@code content} array holds the model text. Second the model text itself, which
 * the prompt asks the model to return as a compact JSON object. The model text
 * parser is tolerant: it locates the JSON object even when the model wraps it in
 * commentary and it never throws on missing fields.</p>
 */
@Component
public class BedrockResponseParser {

    private final ObjectMapper objectMapper;

    public BedrockResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BedrockResponseParser() {
        this(new ObjectMapper());
    }

    /**
     * Extracts the concatenated model text from a Bedrock Anthropic response body.
     */
    public String extractText(String bedrockResponseJson) {
        if (bedrockResponseJson == null || bedrockResponseJson.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(bedrockResponseJson);
            JsonNode content = root.path("content");
            if (content.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode block : content) {
                    JsonNode text = block.path("text");
                    if (text.isTextual()) {
                        sb.append(text.asText());
                    }
                }
                return sb.toString().trim();
            }
            // Fall back to older completion style responses.
            JsonNode completion = root.path("completion");
            if (completion.isTextual()) {
                return completion.asText().trim();
            }
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Parses the model text into a structured result. Recognizes both
     * {@code principalReasons} and {@code reasons}, and both {@code recourseSummary}
     * and {@code recourse}.
     */
    public XaiResult parseXaiPayload(String modelText, String source) {
        String json = isolateJsonObject(modelText);
        if (json == null) {
            return new XaiResult(safe(modelText), List.of(), "", source);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            String rationale = firstText(node, "rationale", "explanation", "summary");
            List<String> reasons = firstArray(node, "principalReasons", "reasons", "drivers");
            String recourse = firstText(node, "recourseSummary", "recourse", "nextSteps");
            return new XaiResult(safe(rationale), reasons, safe(recourse), source);
        } catch (Exception ex) {
            return new XaiResult(safe(modelText), List.of(), "", source);
        }
    }

    private String isolateJsonObject(String text) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isTextual() && !value.asText().isBlank()) {
                return value.asText().trim();
            }
        }
        return "";
    }

    private List<String> firstArray(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isArray()) {
                List<String> items = new ArrayList<>();
                value.forEach(item -> {
                    if (item.isTextual()) {
                        items.add(item.asText().trim());
                    } else if (item.isObject() && item.has("reason")) {
                        items.add(item.get("reason").asText().trim());
                    }
                });
                return items;
            }
        }
        return List.of();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
