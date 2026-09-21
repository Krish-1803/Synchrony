package com.synchrony.inclusion.service;

import com.synchrony.inclusion.scoring.FairnessObservation;
import com.synchrony.inclusion.scoring.FairnessReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes fair lending metrics across monitored demographic groups. This is a
 * post-hoc audit layer. It never influences a single applicant's decision.
 *
 * <ul>
 *   <li>Disparate Impact Ratio and Demographic Parity Ratio use the lowest group
 *       approval rate over the highest group approval rate. The four fifths rule
 *       flags a value below 0.80.</li>
 *   <li>Equal Opportunity Difference is the spread in true positive rates across
 *       groups, where the target is zero.</li>
 * </ul>
 */
@Service
public class FairnessService {

    private static final double FOUR_FIFTHS_RULE = 0.80;

    public FairnessReport evaluate(List<FairnessObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            return new FairnessReport(List.of(), 1.0, 1.0, 0.0, true, 0);
        }

        Map<String, int[]> counts = new LinkedHashMap<>();
        // index 0 total, 1 approved, 2 repaidTrue, 3 repaidTrueApproved
        for (FairnessObservation o : observations) {
            String group = o.protectedClass() == null ? "UNSPECIFIED" : o.protectedClass();
            int[] c = counts.computeIfAbsent(group, k -> new int[4]);
            c[0]++;
            if (o.approved()) {
                c[1]++;
            }
            if (Boolean.TRUE.equals(o.repaid())) {
                c[2]++;
                if (o.approved()) {
                    c[3]++;
                }
            }
        }

        List<FairnessReport.GroupFairness> groups = new ArrayList<>();
        double minApproval = Double.MAX_VALUE;
        double maxApproval = 0.0;
        double minTpr = Double.MAX_VALUE;
        double maxTpr = -Double.MAX_VALUE;

        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            int[] c = entry.getValue();
            double approvalRate = c[0] == 0 ? 0.0 : (double) c[1] / c[0];
            double tpr = c[2] == 0 ? 0.0 : (double) c[3] / c[2];
            groups.add(new FairnessReport.GroupFairness(entry.getKey(), c[0], round(approvalRate), round(tpr)));

            minApproval = Math.min(minApproval, approvalRate);
            maxApproval = Math.max(maxApproval, approvalRate);
            if (c[2] > 0) {
                minTpr = Math.min(minTpr, tpr);
                maxTpr = Math.max(maxTpr, tpr);
            }
        }

        double dir = maxApproval == 0.0 ? 1.0 : minApproval / maxApproval;
        double eod = (minTpr == Double.MAX_VALUE) ? 0.0 : (maxTpr - minTpr);
        boolean compliant = dir >= FOUR_FIFTHS_RULE;

        return new FairnessReport(groups, round(dir), round(dir), round(eod), compliant, observations.size());
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
