package com.synchrony.inclusion.controller;

import com.synchrony.inclusion.dto.ApplicationResponse;
import com.synchrony.inclusion.dto.AssessmentResponse;
import com.synchrony.inclusion.dto.CreateApplicationRequest;
import com.synchrony.inclusion.dto.IngestDataRequest;
import com.synchrony.inclusion.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Applicant portal endpoints. An applicant creates an application, links
 * alternative data streams then runs the dynamic underwriting evaluation.
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(Principal principal,
                                                      @Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.ok(applicationService.createApplication(principal.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> list(Principal principal) {
        return ResponseEntity.ok(applicationService.listForApplicant(principal.getName()));
    }

    @PostMapping("/{id}/data")
    public ResponseEntity<ApplicationResponse> linkData(Principal principal,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody IngestDataRequest request) {
        return ResponseEntity.ok(applicationService.ingestData(principal.getName(), id, request));
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<AssessmentResponse> evaluate(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.evaluate(principal.getName(), id));
    }

    @GetMapping("/{id}/assessment")
    public ResponseEntity<AssessmentResponse> assessment(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.latestAssessment(principal.getName(), id));
    }
}
