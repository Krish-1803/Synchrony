package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.Applicant;
import com.synchrony.inclusion.domain.CreditApplication;
import com.synchrony.inclusion.domain.RiskAssessment;
import com.synchrony.inclusion.domain.enums.ApplicationStatus;
import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.dto.ApplicationResponse;
import com.synchrony.inclusion.dto.AttributionDto;
import com.synchrony.inclusion.dto.OverrideRequest;
import com.synchrony.inclusion.dto.RecourseStepDto;
import com.synchrony.inclusion.dto.UnderwritingDetailResponse;
import com.synchrony.inclusion.dto.VectorNeighborDto;
import com.synchrony.inclusion.exception.ResourceNotFoundException;
import com.synchrony.inclusion.repository.ApplicantRepository;
import com.synchrony.inclusion.repository.CreditApplicationRepository;
import com.synchrony.inclusion.repository.RiskAssessmentRepository;
import com.synchrony.inclusion.scoring.FairnessObservation;
import com.synchrony.inclusion.scoring.FairnessReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The underwriting dashboard backend for Synchrony credit officers. It surfaces
 * risk profiles, transparent feature attribution, the recourse plan, contextual
 * similar cases, fair lending metrics and manual override controls.
 */
@Service
public class UnderwritingService {

    private final CreditApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final VectorService vectorService;
    private final FairnessService fairnessService;
    private final AuditService auditService;
    private final AssessmentSupport support;
    private final ApplicationService applicationService;

    public UnderwritingService(CreditApplicationRepository applicationRepository,
                               ApplicantRepository applicantRepository,
                               RiskAssessmentRepository assessmentRepository,
                               VectorService vectorService,
                               FairnessService fairnessService,
                               AuditService auditService,
                               AssessmentSupport support,
                               ApplicationService applicationService) {
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.assessmentRepository = assessmentRepository;
        this.vectorService = vectorService;
        this.fairnessService = fairnessService;
        this.auditService = auditService;
        this.support = support;
        this.applicationService = applicationService;
    }

    public List<ApplicationResponse> queue() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(app -> {
                    Applicant applicant = applicantRepository.findById(app.getApplicantId()).orElse(null);
                    if (applicant == null) {
                        return null;
                    }
                    return applicationService.toApplicationResponse(app, applicant);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public UnderwritingDetailResponse detail(Long applicationId) {
        CreditApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
        Applicant applicant = applicantRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found."));
        RiskAssessment assessment = assessmentRepository.findTopByApplicationIdOrderByCreatedAtDesc(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("This application has not been evaluated yet."));

        return buildDetail(application, applicant, assessment);
    }

    @Transactional
    public UnderwritingDetailResponse override(String officer, Long applicationId, OverrideRequest request) {
        CreditApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
        Applicant applicant = applicantRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found."));
        RiskAssessment previous = assessmentRepository.findTopByApplicationIdOrderByCreatedAtDesc(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot override before evaluation."));

        RiskAssessment override = cloneForOverride(previous, request);
        override = assessmentRepository.save(override);

        application.setStatus(ApplicationStatus.OVERRIDDEN);
        application.setDecidedAt(Instant.now());
        applicationRepository.save(application);

        auditService.record("RiskAssessment", override.getId(), "MANUAL_OVERRIDE", officer,
                java.util.Map.of(
                        "applicationId", applicationId,
                        "previousDecision", previous.getDecision().name(),
                        "newDecision", request.decision().name(),
                        "reason", request.reason()));

        return buildDetail(application, applicant, override);
    }

    public FairnessReport fairness() {
        List<FairnessObservation> observations = new ArrayList<>();
        for (CreditApplication application : applicationRepository.findAllByOrderByCreatedAtDesc()) {
            RiskAssessment assessment = assessmentRepository
                    .findTopByApplicationIdOrderByCreatedAtDesc(application.getId())
                    .orElse(null);
            if (assessment == null) {
                continue;
            }
            Applicant applicant = applicantRepository.findById(application.getApplicantId()).orElse(null);
            String group = applicant == null || applicant.getProtectedClass() == null
                    ? "UNSPECIFIED" : applicant.getProtectedClass();
            boolean approved = assessment.getDecision() == Decision.APPROVE;
            observations.add(new FairnessObservation(group, approved, assessment.getOutcomeRepaid()));
        }
        return fairnessService.evaluate(observations);
    }

    // ----- helpers -----

    private RiskAssessment cloneForOverride(RiskAssessment previous, OverrideRequest request) {
        RiskAssessment override = new RiskAssessment();
        override.setApplicationId(previous.getApplicationId());
        override.setHybridScore(previous.getHybridScore());
        override.setRiskTier(previous.getRiskTier());
        override.setPdEstimate(previous.getPdEstimate());
        override.setDecision(request.decision());
        override.setLogitZ(previous.getLogitZ());
        override.setWeightTraditional(previous.getWeightTraditional());
        override.setWeightGnn(previous.getWeightGnn());
        override.setWeightAlternative(previous.getWeightAlternative());
        override.setFeatureJson(previous.getFeatureJson());
        override.setAttributionJson(previous.getAttributionJson());
        override.setCounterfactualJson(previous.getCounterfactualJson());
        override.setEmbeddingJson(previous.getEmbeddingJson());
        override.setRationale("Manual override by credit officer. " + request.reason());
        override.setManualOverride(true);
        override.setOverrideReason(request.reason());
        override.setModelVersion(previous.getModelVersion());
        override.setOutcomeRepaid(previous.getOutcomeRepaid());
        return override;
    }

    private UnderwritingDetailResponse buildDetail(CreditApplication application, Applicant applicant,
                                                   RiskAssessment assessment) {
        List<AttributionDto> attributions = support.readAttributions(assessment.getAttributionJson());
        List<AttributionDto> topPositive = attributions.stream()
                .filter(AttributionDto::positive)
                .sorted(Comparator.comparingDouble(AttributionDto::contribution).reversed())
                .limit(5)
                .toList();
        List<AttributionDto> topNegative = attributions.stream()
                .filter(a -> !a.positive())
                .sorted(Comparator.comparingDouble(AttributionDto::contribution))
                .limit(5)
                .toList();
        List<RecourseStepDto> recourse = support.readRecourse(assessment.getCounterfactualJson());

        boolean approved = assessment.getDecision() == Decision.APPROVE;
        List<String> principalReasons = (approved ? topPositive : topNegative).stream()
                .map(AttributionDto::label)
                .toList();
        String recourseSummary = recourse.isEmpty() ? ""
                : "Path to approval: " + String.join(" ", recourse.stream().map(RecourseStepDto::description).toList());

        List<VectorNeighborDto> similar = vectorService
                .findSimilar(support.readEmbedding(assessment.getEmbeddingJson()), 5, application.getId())
                .stream()
                .map(n -> new VectorNeighborDto(n.applicationId(), n.applicantRef(), n.decision(),
                        n.score(), n.distance(), round(1.0 - n.distance())))
                .toList();

        ApplicationResponse applicationResponse = applicationService.toApplicationResponse(application, applicant);

        return new UnderwritingDetailResponse(
                applicationResponse,
                assessment.getDecision().name(),
                assessment.getHybridScore(),
                assessment.getRiskTier().name(),
                round1(assessment.getPdEstimate() * 100.0),
                round(assessment.getLogitZ()),
                assessment.getModelVersion(),
                bucketWeights(assessment),
                support.readFeatureMap(assessment.getFeatureJson()),
                attributions,
                topPositive,
                topNegative,
                assessment.getRationale(),
                principalReasons,
                recourse,
                recourseSummary,
                recourse.isEmpty() || approved,
                assessment.isManualOverride() ? "MANUAL_OVERRIDE" : "PERSISTED",
                assessment.isManualOverride(),
                assessment.getOverrideReason(),
                similar,
                assessment.getCreatedAt());
    }

    private java.util.Map<String, Double> bucketWeights(RiskAssessment assessment) {
        java.util.Map<String, Double> weights = new java.util.LinkedHashMap<>();
        weights.put("TRADITIONAL", round(assessment.getWeightTraditional()));
        weights.put("ALTERNATIVE", round(assessment.getWeightAlternative()));
        weights.put("GNN", round(assessment.getWeightGnn()));
        return weights;
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
