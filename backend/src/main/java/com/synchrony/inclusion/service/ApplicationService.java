package com.synchrony.inclusion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.inclusion.ai.XaiOrchestrator;
import com.synchrony.inclusion.ai.XaiRequest;
import com.synchrony.inclusion.ai.XaiResult;
import com.synchrony.inclusion.domain.AlternativeDataRecord;
import com.synchrony.inclusion.domain.Applicant;
import com.synchrony.inclusion.domain.CreditApplication;
import com.synchrony.inclusion.domain.RiskAssessment;
import com.synchrony.inclusion.domain.UserAccount;
import com.synchrony.inclusion.domain.enums.ApplicationStatus;
import com.synchrony.inclusion.domain.enums.Decision;
import com.synchrony.inclusion.dto.ApplicationResponse;
import com.synchrony.inclusion.dto.AssessmentResponse;
import com.synchrony.inclusion.dto.CreateApplicationRequest;
import com.synchrony.inclusion.dto.IngestDataRequest;
import com.synchrony.inclusion.exception.ForbiddenException;
import com.synchrony.inclusion.exception.ResourceNotFoundException;
import com.synchrony.inclusion.repository.AlternativeDataRecordRepository;
import com.synchrony.inclusion.repository.ApplicantRepository;
import com.synchrony.inclusion.repository.CreditApplicationRepository;
import com.synchrony.inclusion.repository.RiskAssessmentRepository;
import com.synchrony.inclusion.repository.UserAccountRepository;
import com.synchrony.inclusion.scoring.Attribution;
import com.synchrony.inclusion.scoring.CounterfactualPlan;
import com.synchrony.inclusion.scoring.FeatureVector;
import com.synchrony.inclusion.scoring.RecourseStep;
import com.synchrony.inclusion.scoring.ScoreResult;
import com.synchrony.inclusion.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Drives the applicant side of account origination: create an application, link
 * alternative data streams then run the dynamic underwriting evaluation.
 */
@Service
public class ApplicationService {

    private final CreditApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final UserAccountRepository userRepository;
    private final AlternativeDataRecordRepository dataRecordRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final FeatureExtractionService featureExtractionService;
    private final ScoringService scoringService;
    private final ExplainabilityService explainabilityService;
    private final XaiOrchestrator xaiOrchestrator;
    private final VectorService vectorService;
    private final AuditService auditService;
    private final AssessmentSupport support;
    private final ObjectMapper objectMapper;

    public ApplicationService(CreditApplicationRepository applicationRepository,
                              ApplicantRepository applicantRepository,
                              UserAccountRepository userRepository,
                              AlternativeDataRecordRepository dataRecordRepository,
                              RiskAssessmentRepository assessmentRepository,
                              FeatureExtractionService featureExtractionService,
                              ScoringService scoringService,
                              ExplainabilityService explainabilityService,
                              XaiOrchestrator xaiOrchestrator,
                              VectorService vectorService,
                              AuditService auditService,
                              AssessmentSupport support,
                              ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.userRepository = userRepository;
        this.dataRecordRepository = dataRecordRepository;
        this.assessmentRepository = assessmentRepository;
        this.featureExtractionService = featureExtractionService;
        this.scoringService = scoringService;
        this.explainabilityService = explainabilityService;
        this.xaiOrchestrator = xaiOrchestrator;
        this.vectorService = vectorService;
        this.auditService = auditService;
        this.support = support;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApplicationResponse createApplication(String username, CreateApplicationRequest request) {
        Applicant applicant = resolveApplicant(username);
        CreditApplication application = new CreditApplication();
        application.setApplicantId(applicant.getId());
        application.setProductType(request.productType());
        application.setRequestedAmount(request.requestedAmount());
        application.setStatus(ApplicationStatus.DRAFT);
        application = applicationRepository.save(application);

        auditService.record("CreditApplication", application.getId(), "CREATE", username,
                Map.of("productType", request.productType(), "requestedAmount", request.requestedAmount()));
        return toApplicationResponse(application, applicant);
    }

    @Transactional
    public ApplicationResponse ingestData(String username, Long applicationId, IngestDataRequest request) {
        Applicant applicant = resolveApplicant(username);
        CreditApplication application = requireOwnedApplication(applicationId, applicant.getId());

        String payloadJson = support.writeJson(request.payload());
        AlternativeDataRecord record = new AlternativeDataRecord();
        record.setApplicantId(applicant.getId());
        record.setSourceType(request.sourceType());
        record.setPayloadJson(payloadJson);
        record.setPayloadHash(HashUtil.sha256Hex(payloadJson));
        dataRecordRepository.save(record);

        if (application.getStatus() == ApplicationStatus.DRAFT) {
            application.setStatus(ApplicationStatus.DATA_LINKED);
            application = applicationRepository.save(application);
        }

        auditService.record("AlternativeDataRecord", record.getId(), "INGEST", username,
                Map.of("sourceType", request.sourceType().name(), "payloadHash", record.getPayloadHash()));
        return toApplicationResponse(application, applicant);
    }

    @Transactional
    public AssessmentResponse evaluate(String username, Long applicationId) {
        Applicant applicant = resolveApplicant(username);
        CreditApplication application = requireOwnedApplication(applicationId, applicant.getId());

        FeatureVector featureVector = featureExtractionService.extract(loadPayloads(applicant.getId()));
        ScoreResult scoreResult = scoringService.score(featureVector);
        CounterfactualPlan plan = explainabilityService.buildRecourse(scoreResult);

        List<String> positiveLabels = scoreResult.topPositiveDrivers(3).stream().map(Attribution::label).toList();
        List<String> negativeLabels = scoreResult.topNegativeDrivers(3).stream().map(Attribution::label).toList();
        List<String> recourseDescriptions = plan.steps().stream().map(RecourseStep::description).toList();

        XaiRequest xaiRequest = new XaiRequest(
                scoreResult.getDecision().name(),
                scoreResult.getHybridScore(),
                scoreResult.getRiskTier().name(),
                scoreResult.getProbabilityOfDefault() * 100.0,
                application.getProductType(),
                applicant.getSegment() == null ? "General" : applicant.getSegment(),
                applicant.getBankedStatus().name(),
                positiveLabels,
                negativeLabels,
                recourseDescriptions);
        XaiResult xai = xaiOrchestrator.explain(xaiRequest);

        RiskAssessment assessment = persistAssessment(application, featureVector, scoreResult, plan, xai);
        double[] embedding = support.embedding(featureVector);
        vectorService.upsert(application.getId(), applicant.getAnonymizedRef(),
                scoreResult.getDecision().name(), scoreResult.getHybridScore(), embedding);

        applyDecisionToApplication(application, scoreResult.getDecision());

        auditService.record("RiskAssessment", assessment.getId(), "EVALUATE", username, buildAuditDetail(scoreResult, assessment));

        return buildAssessmentResponse(application.getId(), scoreResult, plan, xai,
                positiveLabels, assessment.isManualOverride(), assessment.getCreatedAt());
    }

    public AssessmentResponse latestAssessment(String username, Long applicationId) {
        Applicant applicant = resolveApplicant(username);
        CreditApplication application = requireOwnedApplication(applicationId, applicant.getId());
        RiskAssessment assessment = assessmentRepository.findTopByApplicationIdOrderByCreatedAtDesc(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("This application has not been evaluated yet."));
        return toAssessmentResponse(application.getId(), assessment);
    }

    public List<ApplicationResponse> listForApplicant(String username) {
        Applicant applicant = resolveApplicant(username);
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicant.getId()).stream()
                .map(app -> toApplicationResponse(app, applicant))
                .toList();
    }

    // ----- helpers -----

    private RiskAssessment persistAssessment(CreditApplication application, FeatureVector featureVector,
                                             ScoreResult scoreResult, CounterfactualPlan plan, XaiResult xai) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setApplicationId(application.getId());
        assessment.setHybridScore(scoreResult.getHybridScore());
        assessment.setRiskTier(scoreResult.getRiskTier());
        assessment.setPdEstimate(scoreResult.getProbabilityOfDefault());
        assessment.setDecision(scoreResult.getDecision());
        assessment.setLogitZ(scoreResult.getLogitZ());
        Map<String, Double> weights = support.bucketWeightsAsMap(scoreResult.getBucketWeights());
        assessment.setWeightTraditional(weights.getOrDefault("TRADITIONAL", 0.0));
        assessment.setWeightGnn(weights.getOrDefault("GNN", 0.0));
        assessment.setWeightAlternative(weights.getOrDefault("ALTERNATIVE", 0.0));
        assessment.setFeatureJson(support.writeJson(featureVector.asOrderedMap()));
        assessment.setAttributionJson(support.writeJson(support.toAttributionDtos(scoreResult.getAttributions())));
        assessment.setCounterfactualJson(support.writeJson(support.toRecourseDtos(plan.steps())));
        assessment.setEmbeddingJson(support.writeJson(support.embedding(featureVector)));
        assessment.setRationale(xai.rationale());
        assessment.setModelVersion(scoreResult.getModelVersion());
        return assessmentRepository.save(assessment);
    }

    private void applyDecisionToApplication(CreditApplication application, Decision decision) {
        switch (decision) {
            case APPROVE -> application.setStatus(ApplicationStatus.APPROVED);
            case DECLINE -> application.setStatus(ApplicationStatus.DECLINED);
            case REFER -> application.setStatus(ApplicationStatus.EVALUATED);
        }
        application.setDecidedAt(Instant.now());
        applicationRepository.save(application);
    }

    private Map<String, Object> buildAuditDetail(ScoreResult scoreResult, RiskAssessment assessment) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("decision", scoreResult.getDecision().name());
        detail.put("score", scoreResult.getHybridScore());
        detail.put("riskTier", scoreResult.getRiskTier().name());
        detail.put("modelVersion", scoreResult.getModelVersion());
        detail.put("featureHash", HashUtil.sha256Hex(assessment.getFeatureJson()));
        return detail;
    }

    private Map<com.synchrony.inclusion.domain.enums.DataSourceType, Map<String, Object>> loadPayloads(Long applicantId) {
        Map<com.synchrony.inclusion.domain.enums.DataSourceType, Map<String, Object>> latest =
                new EnumMap<>(com.synchrony.inclusion.domain.enums.DataSourceType.class);
        List<AlternativeDataRecord> records = dataRecordRepository.findByApplicantId(applicantId);
        records.sort((a, b) -> a.getIngestedAt().compareTo(b.getIngestedAt()));
        for (AlternativeDataRecord record : records) {
            latest.put(record.getSourceType(), parsePayload(record.getPayloadJson()));
        }
        return latest;
    }

    private Map<String, Object> parsePayload(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private AssessmentResponse buildAssessmentResponse(Long applicationId, ScoreResult scoreResult,
                                                       CounterfactualPlan plan, XaiResult xai,
                                                       List<String> positiveLabels, boolean manualOverride,
                                                       Instant createdAt) {
        return new AssessmentResponse(
                applicationId,
                scoreResult.getDecision().name(),
                scoreResult.getHybridScore(),
                scoreResult.getRiskTier().name(),
                round1(scoreResult.getProbabilityOfDefault() * 100.0),
                xai.rationale(),
                xai.principalReasons(),
                positiveLabels,
                support.toRecourseDtos(plan.steps()),
                xai.recourseSummary(),
                plan.feasible(),
                xai.source(),
                manualOverride,
                createdAt);
    }

    private AssessmentResponse toAssessmentResponse(Long applicationId, RiskAssessment assessment) {
        List<com.synchrony.inclusion.dto.AttributionDto> attributions = support.readAttributions(assessment.getAttributionJson());
        List<String> positive = attributions.stream()
                .filter(com.synchrony.inclusion.dto.AttributionDto::positive)
                .sorted((a, b) -> Double.compare(b.contribution(), a.contribution()))
                .limit(3)
                .map(com.synchrony.inclusion.dto.AttributionDto::label)
                .toList();
        List<com.synchrony.inclusion.dto.RecourseStepDto> recourse = support.readRecourse(assessment.getCounterfactualJson());
        return new AssessmentResponse(
                applicationId,
                assessment.getDecision().name(),
                assessment.getHybridScore(),
                assessment.getRiskTier().name(),
                round1(assessment.getPdEstimate() * 100.0),
                assessment.getRationale(),
                positive.isEmpty() ? attributions.stream().limit(3).map(com.synchrony.inclusion.dto.AttributionDto::label).toList() : positive,
                positive,
                recourse,
                "",
                recourse.isEmpty() || assessment.getDecision() == Decision.APPROVE,
                "PERSISTED",
                assessment.isManualOverride(),
                assessment.getCreatedAt());
    }

    ApplicationResponse toApplicationResponse(CreditApplication application, Applicant applicant) {
        RiskAssessment latest = assessmentRepository
                .findTopByApplicationIdOrderByCreatedAtDesc(application.getId())
                .orElse(null);
        int linkedSources = dataRecordRepository.findByApplicantId(applicant.getId()).size();
        return new ApplicationResponse(
                application.getId(),
                applicant.getId(),
                applicant.getAnonymizedRef(),
                applicant.getSegment(),
                applicant.getBankedStatus().name(),
                application.getProductType(),
                application.getRequestedAmount(),
                application.getStatus().name(),
                application.getCreatedAt(),
                latest == null ? null : latest.getHybridScore(),
                latest == null ? null : latest.getRiskTier().name(),
                latest == null ? null : latest.getDecision().name(),
                linkedSources);
    }

    private Applicant resolveApplicant(String username) {
        UserAccount account = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return applicantRepository.findByUserId(account.getId())
                .orElseThrow(() -> new ForbiddenException("This account has no applicant profile."));
    }

    private CreditApplication requireOwnedApplication(Long applicationId, Long applicantId) {
        CreditApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
        if (!application.getApplicantId().equals(applicantId)) {
            throw new ForbiddenException("This application belongs to another applicant.");
        }
        return application;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
