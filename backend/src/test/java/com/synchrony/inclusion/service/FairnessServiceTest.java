package com.synchrony.inclusion.service;

import com.synchrony.inclusion.scoring.FairnessObservation;
import com.synchrony.inclusion.scoring.FairnessReport;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FairnessServiceTest {

    private final FairnessService service = new FairnessService();

    private List<FairnessObservation> cohort(String group, int total, int approved) {
        List<FairnessObservation> list = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            list.add(new FairnessObservation(group, i < approved, null));
        }
        return list;
    }

    @Test
    void balancedApprovalRatesPassTheFourFifthsRule() {
        List<FairnessObservation> observations = new ArrayList<>();
        observations.addAll(cohort("Group A", 10, 8));
        observations.addAll(cohort("Group B", 10, 7));

        FairnessReport report = service.evaluate(observations);

        assertThat(report.disparateImpactRatio()).isCloseTo(0.875, Offset.offset(1e-4));
        assertThat(report.compliant()).isTrue();
        assertThat(report.sampleSize()).isEqualTo(20);
    }

    @Test
    void skewedApprovalRatesFailTheFourFifthsRule() {
        List<FairnessObservation> observations = new ArrayList<>();
        observations.addAll(cohort("Group A", 10, 9));
        observations.addAll(cohort("Group B", 10, 4));

        FairnessReport report = service.evaluate(observations);

        assertThat(report.disparateImpactRatio()).isLessThan(0.80);
        assertThat(report.compliant()).isFalse();
    }

    @Test
    void equalOpportunityDifferenceUsesTruePositiveRates() {
        List<FairnessObservation> observations = new ArrayList<>();
        // Group A: 5 creditworthy applicants, all approved -> TPR 1.0
        for (int i = 0; i < 5; i++) {
            observations.add(new FairnessObservation("Group A", true, true));
        }
        // Group B: 5 creditworthy applicants, 3 approved -> TPR 0.6
        for (int i = 0; i < 5; i++) {
            observations.add(new FairnessObservation("Group B", i < 3, true));
        }

        FairnessReport report = service.evaluate(observations);

        assertThat(report.equalOpportunityDiff()).isCloseTo(0.4, Offset.offset(1e-4));
    }

    @Test
    void emptyCohortIsTreatedAsCompliant() {
        FairnessReport report = service.evaluate(List.of());
        assertThat(report.compliant()).isTrue();
        assertThat(report.sampleSize()).isZero();
    }
}
