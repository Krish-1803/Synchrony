package com.synchrony.inclusion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.inclusion.dto.AttributionDto;
import com.synchrony.inclusion.dto.RecourseStepDto;
import com.synchrony.inclusion.scoring.Attribution;
import com.synchrony.inclusion.scoring.Bucket;
import com.synchrony.inclusion.scoring.CounterfactualPlan;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureVector;
import com.synchrony.inclusion.scoring.RecourseStep;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared conversions between scoring domain objects, transport DTOs and the JSON
 * columns persisted on a risk assessment.
 */
@Component
public class AssessmentSupport {

    private final ObjectMapper objectMapper;

    public AssessmentSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public double[] embedding(FeatureVector vector) {
        List<String> keys = FeatureCatalog.keys();
        double[] values = new double[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            values[i] = vector.value(keys.get(i));
        }
        return values;
    }

    public List<AttributionDto> toAttributionDtos(List<Attribution> attributions) {
        return attributions.stream()
                .map(a -> new AttributionDto(a.key(), a.label(), round(a.value()), round(a.contribution()), a.isPositive()))
                .toList();
    }

    public List<RecourseStepDto> toRecourseDtos(List<RecourseStep> steps) {
        return steps.stream()
                .map(s -> new RecourseStepDto(s.key(), s.label(), s.currentValue(), s.targetValue(),
                        s.percentileShift(), s.description()))
                .toList();
    }

    public Map<String, Double> bucketWeightsAsMap(Map<Bucket, Double> weights) {
        Map<String, Double> map = new LinkedHashMap<>();
        weights.forEach((bucket, weight) -> map.put(bucket.name(), round(weight)));
        return map;
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "null";
        }
    }

    public List<AttributionDto> readAttributions(String json) {
        return readList(json, new TypeReference<>() {
        });
    }

    public List<RecourseStepDto> readRecourse(String json) {
        return readList(json, new TypeReference<>() {
        });
    }

    public Map<String, Double> readFeatureMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    public double[] readEmbedding(String json) {
        if (json == null || json.isBlank()) {
            return new double[0];
        }
        try {
            return objectMapper.readValue(json, double[].class);
        } catch (Exception ex) {
            return new double[0];
        }
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> type) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
