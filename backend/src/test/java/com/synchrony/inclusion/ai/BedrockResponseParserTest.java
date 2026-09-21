package com.synchrony.inclusion.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BedrockResponseParserTest {

    private final BedrockResponseParser parser = new BedrockResponseParser();

    @Test
    void extractsConcatenatedTextFromBedrockEnvelope() {
        String body = "{\"content\":[{\"type\":\"text\",\"text\":\"Hello \"},"
                + "{\"type\":\"text\",\"text\":\"world\"}],\"stop_reason\":\"end_turn\"}";

        assertThat(parser.extractText(body)).isEqualTo("Hello world");
    }

    @Test
    void extractTextReturnsEmptyOnMalformedEnvelope() {
        assertThat(parser.extractText("not json")).isEmpty();
        assertThat(parser.extractText(null)).isEmpty();
    }

    @Test
    void parsesCleanJsonPayload() {
        String modelText = "{\"rationale\":\"Approved.\","
                + "\"principalReasons\":[\"Strong utility punctuality\",\"Stable network\"],"
                + "\"recourseSummary\":\"\"}";

        XaiResult result = parser.parseXaiPayload(modelText, "BEDROCK");

        assertThat(result.rationale()).isEqualTo("Approved.");
        assertThat(result.principalReasons()).containsExactly("Strong utility punctuality", "Stable network");
        assertThat(result.source()).isEqualTo("BEDROCK");
    }

    @Test
    void isolatesJsonWhenModelWrapsItInProse() {
        String modelText = "Here is the result: "
                + "{\"rationale\":\"Declined.\",\"reasons\":[\"Low balance\"],\"recourse\":\"Increase balance.\"}"
                + " Thanks";

        XaiResult result = parser.parseXaiPayload(modelText, "BEDROCK");

        assertThat(result.rationale()).isEqualTo("Declined.");
        assertThat(result.principalReasons()).containsExactly("Low balance");
        assertThat(result.recourseSummary()).isEqualTo("Increase balance.");
    }

    @Test
    void fallsBackToRawTextWhenNoJsonPresent() {
        XaiResult result = parser.parseXaiPayload("The application was approved.", "FALLBACK");

        assertThat(result.rationale()).isEqualTo("The application was approved.");
        assertThat(result.principalReasons()).isEmpty();
        assertThat(result.source()).isEqualTo("FALLBACK");
    }
}
