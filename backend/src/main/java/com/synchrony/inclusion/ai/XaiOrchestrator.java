package com.synchrony.inclusion.ai;

import com.synchrony.inclusion.service.AnonymizationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Explainable AI orchestrator. It converts numerical risk factors and
 * counterfactual recourse into a plain-language rationale that satisfies the
 * adverse action disclosure requirement of ECOA and Regulation B.
 *
 * <p>The orchestrator prefers AWS Bedrock. If Bedrock is disabled or unreachable
 * it produces the same structured output from a deterministic template, so the
 * decision path never depends on model availability. All prompt text is scrubbed
 * of PII before it leaves the service.</p>
 */
@Service
public class XaiOrchestrator {

    private static final String SYSTEM_PROMPT = """
            You are a compliance assistant for a consumer credit issuer.
            Write clear, factual adverse action and approval rationales.
            Never mention race, gender, age, national origin or any protected class.
            Base every statement only on the provided risk drivers and recourse steps.
            Return a compact JSON object with keys rationale, principalReasons and recourseSummary.
            principalReasons must be an array of short strings.
            Do not use em-dashes. Do not place a comma before and in a list.
            """;

    private final BedrockClient bedrockClient;
    private final BedrockResponseParser parser;
    private final AnonymizationService anonymizationService;

    public XaiOrchestrator(BedrockClient bedrockClient, BedrockResponseParser parser,
                           AnonymizationService anonymizationService) {
        this.bedrockClient = bedrockClient;
        this.parser = parser;
        this.anonymizationService = anonymizationService;
    }

    public XaiResult explain(XaiRequest request) {
        if (bedrockClient.isEnabled()) {
            String userPrompt = anonymizationService.redactText(buildUserPrompt(request));
            String raw = bedrockClient.invoke(SYSTEM_PROMPT, userPrompt);
            if (raw != null && !raw.isBlank()) {
                String modelText = parser.extractText(raw);
                XaiResult result = parser.parseXaiPayload(modelText, "BEDROCK");
                if (result.rationale() != null && !result.rationale().isBlank()) {
                    return result;
                }
            }
        }
        return fallback(request);
    }

    String buildUserPrompt(XaiRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Decision: ").append(r.decision()).append('\n');
        sb.append("Hybrid score: ").append(r.score()).append('\n');
        sb.append("Risk tier: ").append(r.riskTier()).append('\n');
        sb.append("Probability of default: ").append(String.format("%.1f", r.pdPercent())).append("%\n");
        sb.append("Product: ").append(r.productType()).append('\n');
        sb.append("File type: ").append(r.bankedStatus()).append('\n');
        sb.append("Top positive drivers: ").append(String.join("; ", r.positiveDrivers())).append('\n');
        sb.append("Top negative drivers: ").append(String.join("; ", r.negativeDrivers())).append('\n');
        if (!r.recourseSteps().isEmpty()) {
            sb.append("Recourse steps: ").append(String.join("; ", r.recourseSteps())).append('\n');
        }
        return sb.toString();
    }

    XaiResult fallback(XaiRequest r) {
        boolean approved = "APPROVE".equalsIgnoreCase(r.decision());
        String rationale;
        List<String> reasons;
        String recourse = "";

        if (approved) {
            reasons = new ArrayList<>(r.positiveDrivers());
            rationale = "This application meets the Synchrony approval threshold with a hybrid score of "
                    + r.score() + " in " + friendlyTier(r.riskTier()) + ". The strongest supporting signals are "
                    + joinList(r.positiveDrivers()) + ".";
        } else {
            reasons = new ArrayList<>(r.negativeDrivers());
            rationale = "This application scored " + r.score() + ", below the approval threshold. "
                    + "The principal factors were " + joinList(r.negativeDrivers()) + ".";
            if (!r.recourseSteps().isEmpty()) {
                recourse = "To move toward approval: " + joinSteps(r.recourseSteps());
            }
        }
        return new XaiResult(rationale, reasons, recourse, "FALLBACK");
    }

    private String friendlyTier(String tier) {
        if (tier == null) {
            return "the assigned risk band";
        }
        return tier.replace('_', ' ').toLowerCase();
    }

    /** Joins labels as "a, b and c" with no comma before and. */
    static String joinList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "no dominant single factor";
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        String head = String.join(", ", items.subList(0, items.size() - 1));
        return head + " and " + items.get(items.size() - 1);
    }

    private String joinSteps(List<String> steps) {
        StringBuilder sb = new StringBuilder();
        for (String step : steps) {
            sb.append(' ').append(step);
        }
        return sb.toString().trim();
    }
}
