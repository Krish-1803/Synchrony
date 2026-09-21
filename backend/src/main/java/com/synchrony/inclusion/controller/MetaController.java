package com.synchrony.inclusion.controller;

import com.synchrony.inclusion.ai.BedrockClient;
import com.synchrony.inclusion.dto.SystemStatusResponse;
import com.synchrony.inclusion.scoring.FeatureCatalog;
import com.synchrony.inclusion.scoring.FeatureDefinition;
import com.synchrony.inclusion.scoring.ScoringProperties;
import com.synchrony.inclusion.service.VectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Metadata endpoints for the frontend: the feature catalog and runtime status of
 * the Bedrock and pgvector layers.
 */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final BedrockClient bedrockClient;
    private final VectorService vectorService;
    private final ScoringProperties scoringProperties;

    public MetaController(BedrockClient bedrockClient, VectorService vectorService,
                          ScoringProperties scoringProperties) {
        this.bedrockClient = bedrockClient;
        this.vectorService = vectorService;
        this.scoringProperties = scoringProperties;
    }

    @GetMapping("/features")
    public ResponseEntity<List<FeatureDefinition>> features() {
        return ResponseEntity.ok(FeatureCatalog.all());
    }

    @GetMapping("/status")
    public ResponseEntity<SystemStatusResponse> status() {
        return ResponseEntity.ok(new SystemStatusResponse(
                bedrockClient.isEnabled(),
                vectorService.isPgvectorEnabled(),
                scoringProperties.getModelVersion(),
                scoringProperties.getApproveThreshold(),
                scoringProperties.getDeclineThreshold()));
    }
}
