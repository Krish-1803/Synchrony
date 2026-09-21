package com.synchrony.inclusion.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnonymizationServiceTest {

    private final AnonymizationService service = new AnonymizationService();

    @Test
    void redactsSensitiveKeysAndKeepsNumericSignals() {
        Map<String, Object> input = new HashMap<>();
        input.put("fullName", "Jane Doe");
        input.put("email", "jane@example.com");
        input.put("accountNumber", "1234567890123");
        input.put("inflow", 3000);

        Map<String, Object> output = service.anonymize(input);

        assertThat(output.get("fullName")).isEqualTo("[REDACTED]");
        assertThat(output.get("email")).isEqualTo("[REDACTED]");
        assertThat(output.get("accountNumber")).isEqualTo("[REDACTED]");
        assertThat(output.get("inflow")).isEqualTo(3000);
    }

    @Test
    void scrubsPiiPatternsInsideFreeTextValues() {
        Map<String, Object> input = new HashMap<>();
        input.put("note", "Please call 555-123-4567 to confirm");

        Map<String, Object> output = service.anonymize(input);

        assertThat((String) output.get("note")).contains("[REDACTED]");
        assertThat((String) output.get("note")).doesNotContain("555-123-4567");
    }

    @Test
    void anonymizesNestedMaps() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("ssn", "111-22-3333");
        nested.put("score", 700);
        Map<String, Object> input = new HashMap<>();
        input.put("profile", nested);

        Map<String, Object> output = service.anonymize(input);

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) output.get("profile");
        assertThat(profile.get("ssn")).isEqualTo("[REDACTED]");
        assertThat(profile.get("score")).isEqualTo(700);
    }

    @Test
    void redactTextMasksEmailsIdentifiersAndCardNumbers() {
        String result = service.redactText("Email a@b.co, SSN 111-22-3333, card 4111111111111111");

        assertThat(result).doesNotContain("a@b.co");
        assertThat(result).doesNotContain("111-22-3333");
        assertThat(result).doesNotContain("4111111111111111");
        assertThat(result).contains("[REDACTED]");
    }
}
