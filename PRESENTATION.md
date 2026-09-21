# Synchrony Presentation Content

AI-Powered Financial Inclusion and Dynamic Risk Assessment. Slide-by-slide
content for a PPT or PDF deck. Brand palette: Synchrony Gold #FFC220, Charcoal
#333333, Off-White #F4F4F4.

---

## Slide 1 - Title and Vision

**AI-Powered Financial Inclusion**
**Dynamic Risk Assessment for Underserved Segments**

- Fair and accurate credit for thin-file and unbanked applicants.
- Alternative data plus explainable AI on the Synchrony 300 to 850 band.
- Built for account origination at the point of sale.

Speaker note: 45 million adults in the United States are credit invisible or
carry thin files. Synchrony sits at the point of sale, so safely approving this
segment grows merchant volume and customer lifetime value.

---

## Slide 2 - The Challenge

**Conventional scoring leaves creditworthy people behind**

- Legacy FICO and VantageScore models depend on bureau trade lines and
  repayment history.
- Applicants with no file are treated as high risk by default.
- Scorecards are backward looking and update on monthly batches.
- Recurring rent, utility and mobile money payments are ignored.

Speaker note: The result is a self-reinforcing cycle. No file means no approval,
and no approval means no way to build a file. We break that cycle with data that
already exists.

---

## Slide 3 - Data Strategy

**Responsibly leveraging alternative data**

- Mobile money: inflow ratio, balance stability, settlement velocity and
  counterparty diversity.
- Utility and telecom: payment punctuality and top-up consistency.
- Behavioral SDK: typing stability, app diversity and session regularity.
- Transaction graph: network stability and distance from fraud clusters.

Governance:

- Protected attributes are never used to score. They are held only for fairness
  monitoring.
- PII is anonymized before any AI processing.

Speaker note: Alternative data models reach AUC between 0.63 and 0.77 for
applicants where traditional models fail entirely.

---

## Slide 4 - Synchrony Solution

**A scalable, explainable model for reliable risk insight**

- Hybrid score blends traditional, alternative and network signals.
- Weights reallocate automatically when a data group is missing.
- Every decision returns top positive and negative drivers.
- Declines include a concrete path to approval.

Speaker note: Empirical implementations show approval rate uplift of 15 to 35
percent with no rise in baseline default rates.

---

## Slide 5 - System Architecture

**End-to-end technology stack**

- Frontend: React applicant portal and underwriting dashboard.
- Backend: Spring Boot REST API with JWT auth and Role-Based Access Control.
- Data: PostgreSQL with pgvector for contextual risk pattern search.
- AI: AWS Bedrock orchestrator with a deterministic fallback.

Flow: link data, extract features, score, assess fairness, compute recourse,
generate rationale, store the embedding and write the audit record.

Speaker note: The Spring Boot gateway validates and routes requests. pgvector
retrieves similar historical cases with an HNSW cosine index.

---

## Slide 6 - Explainable AI in Underwriting

**Feature extraction, vector similarity and transparent decisions**

- The additive scorecard makes each contribution an exact SHAP value.
- The dashboard shows diverging attribution bars per feature.
- Counterfactual recourse finds the minimal feasible changes to reach approval.
- Bedrock turns the numbers into a plain-language adverse action rationale.

Speaker note: Because contributions are additive, they sum back to the score
logit. That satisfies local accuracy and consistency without post-hoc guesswork.

---

## Slide 7 - Regulatory Compliance and Fairness

**Minimizing bias and protecting privacy**

- Adverse action notices meet ECOA, Regulation B and CFPB Circular 2022-03.
- Fairness monitor tracks Disparate Impact, Demographic Parity and Equal
  Opportunity, with a four fifths rule flag.
- Protected attributes are screened out of scoring.
- Append-only audit log with SHA-256 payload hashing for full lineage.

Speaker note: Bias mitigation spans pre-processing, in-processing and
post-processing. The monitor flags any group that falls below 0.80 so strategy
can recalibrate thresholds.

---

## Slide 8 - Prototype Walkthrough

**Working software, not slides alone**

- Applicant portal: link streams, run underwriting, read the decision and
  recourse.
- Underwriting dashboard: risk profile, model weighting, attribution, similar
  cases and manual override.
- Backend: clean separation of controllers, services, AI engines and
  repositories with unit tests.
- Design: Synchrony Gold, Charcoal and Off-White throughout.

Speaker note: The prototype runs end to end with seeded demo applicants. Every
decision is reproducible from the stored features, attribution and audit record.
