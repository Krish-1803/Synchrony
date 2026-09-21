package com.synchrony.inclusion.config;

import com.synchrony.inclusion.domain.Applicant;
import com.synchrony.inclusion.domain.CreditApplication;
import com.synchrony.inclusion.domain.RiskAssessment;
import com.synchrony.inclusion.domain.UserAccount;
import com.synchrony.inclusion.domain.enums.ApplicationStatus;
import com.synchrony.inclusion.domain.enums.BankedStatus;
import com.synchrony.inclusion.domain.enums.DataSourceType;
import com.synchrony.inclusion.domain.enums.Role;
import com.synchrony.inclusion.dto.CreateApplicationRequest;
import com.synchrony.inclusion.dto.IngestDataRequest;
import com.synchrony.inclusion.repository.ApplicantRepository;
import com.synchrony.inclusion.repository.CreditApplicationRepository;
import com.synchrony.inclusion.repository.RiskAssessmentRepository;
import com.synchrony.inclusion.repository.UserAccountRepository;
import com.synchrony.inclusion.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Seeds a credit officer and a set of demo applicants with linked data and
 * evaluated decisions so the dashboards have content on first run. It runs only
 * when the database has no users, so it is safe to leave enabled.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserAccountRepository userRepository;
    private final ApplicantRepository applicantRepository;
    private final CreditApplicationRepository applicationRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final ApplicationService applicationService;
    private final PasswordEncoder passwordEncoder;

    @Value("${synchrony.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${synchrony.seed.officer-username:officer}")
    private String officerUsername;

    @Value("${synchrony.seed.officer-password:Officer#2024}")
    private String officerPassword;

    @Value("${synchrony.seed.applicant-password:Applicant#2024}")
    private String applicantPassword;

    public DataSeeder(UserAccountRepository userRepository, ApplicantRepository applicantRepository,
                      CreditApplicationRepository applicationRepository,
                      RiskAssessmentRepository assessmentRepository,
                      ApplicationService applicationService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.assessmentRepository = assessmentRepository;
        this.applicationService = applicationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Seed skipped. Users already exist.");
            return;
        }

        createOfficer();
        warnOnDefaultCredentials();

        List<SeedProfile> profiles = List.of(
                new SeedProfile("maria", "Maria Alvarez", "Gig Economy", BankedStatus.UNBANKED, "Group A",
                        "strong", false, "Private Label Card", 1500, Boolean.TRUE),
                new SeedProfile("aisha", "Aisha Khan", "New Immigrant", BankedStatus.UNBANKED, "Group B",
                        "strong", false, "Co-Branded Card", 2000, Boolean.TRUE),
                new SeedProfile("diego", "Diego Santos", "Established Retail", BankedStatus.BANKED, "Group A",
                        "strong", true, "Point-of-Sale Financing", 3000, Boolean.TRUE),
                new SeedProfile("james", "James Carter", "Recent Graduate", BankedStatus.THIN_FILE, "Group B",
                        "medium", false, "Private Label Card", 1200, Boolean.TRUE),
                new SeedProfile("priya", "Priya Nair", "Small Business", BankedStatus.THIN_FILE, "Group A",
                        "medium", true, "Co-Branded Card", 2500, Boolean.FALSE),
                new SeedProfile("chen", "Chen Wei", "Freelancer", BankedStatus.THIN_FILE, "Group B",
                        "weak", false, "Private Label Card", 900, Boolean.FALSE),
                new SeedProfile("sofia", "Sofia Rossi", "Rural Household", BankedStatus.UNBANKED, "Group A",
                        "weak", false, "Point-of-Sale Financing", 800, Boolean.FALSE),
                new SeedProfile("omar", "Omar Haddad", "Seasonal Worker", BankedStatus.UNBANKED, "Group B",
                        "medium", false, "Private Label Card", 1100, Boolean.TRUE)
        );

        for (SeedProfile profile : profiles) {
            try {
                seedProfile(profile);
            } catch (Exception ex) {
                log.warn("Failed to seed applicant {}: {}", profile.username(), ex.getMessage());
            }
        }
        log.info("Seed complete. Officer login: {} | Applicant logins share the seeded applicant password.",
                officerUsername);
    }

    private void createOfficer() {
        UserAccount officer = new UserAccount();
        officer.setUsername(officerUsername);
        officer.setPasswordHash(passwordEncoder.encode(officerPassword));
        officer.setFullName("Synchrony Credit Officer");
        officer.setRole(Role.CREDIT_OFFICER);
        userRepository.save(officer);
    }

    private void seedProfile(SeedProfile profile) {
        UserAccount account = new UserAccount();
        account.setUsername(profile.username());
        account.setPasswordHash(passwordEncoder.encode(applicantPassword));
        account.setFullName(profile.fullName());
        account.setRole(Role.APPLICANT);
        account = userRepository.save(account);

        Applicant applicant = new Applicant();
        applicant.setUserId(account.getId());
        applicant.setDisplayName(profile.fullName());
        applicant.setAnonymizedRef("SYF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        applicant.setSegment(profile.segment());
        applicant.setBankedStatus(profile.bankedStatus());
        applicant.setProtectedClass(profile.protectedClass());
        applicantRepository.save(applicant);

        var application = applicationService.createApplication(profile.username(),
                new CreateApplicationRequest(profile.productType(), BigDecimal.valueOf(profile.amount())));

        for (Map.Entry<DataSourceType, Map<String, Object>> entry : payloadsFor(profile).entrySet()) {
            applicationService.ingestData(profile.username(), application.id(),
                    new IngestDataRequest(entry.getKey(), entry.getValue()));
        }

        applicationService.evaluate(profile.username(), application.id());

        // Record a realized repayment outcome so the fairness monitor has labels.
        assessmentRepository.findTopByApplicationIdOrderByCreatedAtDesc(application.id())
                .ifPresent(assessment -> {
                    assessment.setOutcomeRepaid(profile.repaid());
                    assessmentRepository.save(assessment);
                });
    }

    private Map<DataSourceType, Map<String, Object>> payloadsFor(SeedProfile profile) {
        Map<DataSourceType, Map<String, Object>> payloads = new LinkedHashMap<>();
        double factor = switch (profile.strength()) {
            case "strong" -> 1.0;
            case "medium" -> 0.6;
            default -> 0.3;
        };

        payloads.put(DataSourceType.MOBILE_MONEY, Map.of(
                "inflow", 3200 * factor,
                "outflow", 2600.0,
                "avgDailyBalance", 620 * factor,
                "peakBalance", 900.0,
                "settlementDays", 30 - (int) (26 * factor),
                "counterpartyCount", (int) (45 * factor)
        ));
        payloads.put(DataSourceType.UTILITY_PAYMENTS, Map.of(
                "onTimePayments", (int) (24 * factor),
                "totalPayments", 24
        ));
        payloads.put(DataSourceType.TELCO_CDR, Map.of(
                "topupConsistency", 0.35 + 0.6 * factor
        ));
        payloads.put(DataSourceType.BEHAVIORAL_SDK, Map.of(
                "typingStability", 0.4 + 0.5 * factor,
                "appDiversityCount", (int) (30 * factor),
                "sessionRegularity", 0.4 + 0.5 * factor
        ));
        payloads.put(DataSourceType.TRANSACTION_GRAPH, Map.of(
                "networkStability", 0.35 + 0.6 * factor,
                "fraudProximity", 0.5 - 0.45 * factor
        ));
        if (profile.includeBureau()) {
            payloads.put(DataSourceType.BUREAU_TRADELINE, Map.of(
                    "tradeLineCount", (int) (6 * factor),
                    "utilization", 0.7 - 0.4 * factor
            ));
        }
        return payloads;
    }

    private void warnOnDefaultCredentials() {
        if ("Officer#2024".equals(officerPassword) || "Applicant#2024".equals(applicantPassword)) {
            log.warn("Seed is using default demo passwords. Set SEED_OFFICER_PASSWORD and "
                    + "SEED_APPLICANT_PASSWORD for any shared environment.");
        }
    }

    private record SeedProfile(
            String username,
            String fullName,
            String segment,
            BankedStatus bankedStatus,
            String protectedClass,
            String strength,
            boolean includeBureau,
            String productType,
            int amount,
            Boolean repaid
    ) {
    }
}
