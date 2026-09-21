package com.synchrony.inclusion.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.inclusion.service.AnonymizationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XaiOrchestratorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BedrockProperties properties = new BedrockProperties();
    private final BedrockClient bedrockClient = new BedrockClient(properties, objectMapper);
    private final XaiOrchestrator orchestrator = new XaiOrchestrator(
            bedrockClient, new BedrockResponseParser(objectMapper), new AnonymizationService());

    @Test
    void fallbackApprovalRationaleReferencesScoreAndDrivers() {
        XaiRequest request = new XaiRequest("APPROVE", 720, "TIER_1_PRIME", 5.0,
                "Private Label Card", "General", "UNBANKED",
                List.of("Utility payment punctuality", "Transaction network stability", "Mobile money inflow ratio"),
                List.of(), List.of());

        XaiResult result = orchestrator.explain(request);

        assertThat(result.source()).isEqualTo("FALLBACK");
        assertThat(result.rationale()).contains("720");
        assertThat(result.principalReasons()).containsExactly(
                "Utility payment punctuality", "Transaction network stability", "Mobile money inflow ratio");
        assertThat(result.recourseSummary()).isEmpty();
    }

    @Test
    void fallbackDeclineRationaleIncludesRecourse() {
        XaiRequest request = new XaiRequest("DECLINE", 540, "TIER_5_DEEP_SUBPRIME", 40.0,
                "Private Label Card", "Gig Economy", "UNBANKED",
                List.of(),
                List.of("Utility payment punctuality", "Average daily balance stability"),
                List.of("Raise utility payment punctuality from 40% to 70%."));

        XaiResult result = orchestrator.explain(request);

        assertThat(result.source()).isEqualTo("FALLBACK");
        assertThat(result.rationale()).contains("540");
        assertThat(result.principalReasons()).containsExactly(
                "Utility payment punctuality", "Average daily balance stability");
        assertThat(result.recourseSummary()).contains("Raise utility payment punctuality");
    }

    @Test
    void listCopyDoesNotUseAnOxfordComma() {
        XaiRequest request = new XaiRequest("APPROVE", 700, "TIER_2_PRIME", 8.0,
                "Co-Branded Card", "General", "THIN_FILE",
                List.of("Alpha", "Beta", "Gamma"), List.of(), List.of());

        XaiResult result = orchestrator.explain(request);

        assertThat(result.rationale()).contains("Alpha, Beta and Gamma");
        assertThat(result.rationale()).doesNotContain(", and ");
    }
}
