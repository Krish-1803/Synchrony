package com.synchrony.inclusion.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Strips personally identifiable information before any data leaves the service
 * boundary toward the LLM layer. Two safeguards run together: sensitive keys are
 * dropped by name and free-text values are scrubbed for common PII patterns.
 * This enforces the privacy norm that raw consumer identity never reaches the
 * generative model.
 */
@Service
public class AnonymizationService {

    private static final String REDACTED = "[REDACTED]";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "name", "fullname", "firstname", "lastname", "middlename",
            "email", "phone", "phonenumber", "mobile",
            "ssn", "socialsecurity", "taxid",
            "address", "street", "city", "zip", "zipcode", "postalcode",
            "dob", "dateofbirth", "birthdate",
            "accountnumber", "cardnumber", "pan", "iban", "routingnumber"
    );

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern SSN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern LONG_NUMBER = Pattern.compile("\\b\\d{9,19}\\b");
    private static final Pattern PHONE = Pattern.compile("\\b(?:\\+?1[ .-]?)?\\(?\\d{3}\\)?[ .-]?\\d{3}[ .-]?\\d{4}\\b");

    /**
     * Returns a copy of the map with sensitive keys redacted and string values
     * scrubbed of PII patterns. Nested maps are anonymized recursively.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> anonymize(Map<String, Object> input) {
        Map<String, Object> output = new LinkedHashMap<>();
        if (input == null) {
            return output;
        }
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (isSensitiveKey(key)) {
                output.put(key, REDACTED);
            } else if (value instanceof Map<?, ?> nested) {
                output.put(key, anonymize((Map<String, Object>) nested));
            } else if (value instanceof String text) {
                output.put(key, redactText(text));
            } else {
                output.put(key, value);
            }
        }
        return output;
    }

    /**
     * Scrubs a free-text string of emails, phone numbers, government identifiers
     * and long account numbers.
     */
    public String redactText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String scrubbed = EMAIL.matcher(text).replaceAll(REDACTED);
        scrubbed = SSN.matcher(scrubbed).replaceAll(REDACTED);
        scrubbed = PHONE.matcher(scrubbed).replaceAll(REDACTED);
        scrubbed = LONG_NUMBER.matcher(scrubbed).replaceAll(REDACTED);
        return scrubbed;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase().replaceAll("[^a-z]", "");
        return SENSITIVE_KEYS.contains(normalized);
    }
}
