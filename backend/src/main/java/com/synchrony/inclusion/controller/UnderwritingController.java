package com.synchrony.inclusion.controller;

import com.synchrony.inclusion.dto.ApplicationResponse;
import com.synchrony.inclusion.dto.OverrideRequest;
import com.synchrony.inclusion.dto.UnderwritingDetailResponse;
import com.synchrony.inclusion.scoring.FairnessReport;
import com.synchrony.inclusion.service.UnderwritingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Underwriting dashboard endpoints. Restricted to credit officers by both the
 * URL rule in the security config and the method rule here.
 */
@RestController
@RequestMapping("/api/underwriting")
@PreAuthorize("hasRole('CREDIT_OFFICER')")
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    public UnderwritingController(UnderwritingService underwritingService) {
        this.underwritingService = underwritingService;
    }

    @GetMapping("/queue")
    public ResponseEntity<List<ApplicationResponse>> queue() {
        return ResponseEntity.ok(underwritingService.queue());
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<UnderwritingDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(underwritingService.detail(id));
    }

    @PostMapping("/applications/{id}/override")
    public ResponseEntity<UnderwritingDetailResponse> override(Principal principal,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody OverrideRequest request) {
        return ResponseEntity.ok(underwritingService.override(principal.getName(), id, request));
    }

    @GetMapping("/fairness")
    public ResponseEntity<FairnessReport> fairness() {
        return ResponseEntity.ok(underwritingService.fairness());
    }
}
