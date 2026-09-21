-- =============================================================================
-- Synchrony Dynamic Risk Assessment - canonical schema
-- PostgreSQL 15+ with the pgvector extension
--
-- This is the reference migration. The application also self-bootstraps the
-- relational tables through JPA and the pgvector table through the vector
-- service, so running this script is optional for local development but
-- recommended for any managed environment.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS vector;

-- -----------------------------------------------------------------------------
-- Identity and access
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_account (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(160),
    role          VARCHAR(40)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS applicant (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT,
    display_name    VARCHAR(160),
    anonymized_ref  VARCHAR(64) NOT NULL UNIQUE,
    segment         VARCHAR(120),
    banked_status   VARCHAR(30) NOT NULL,
    protected_class VARCHAR(60),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_applicant_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);
CREATE INDEX IF NOT EXISTS idx_applicant_user ON applicant (user_id);

-- -----------------------------------------------------------------------------
-- Applications and linked alternative data
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS credit_application (
    id               BIGSERIAL PRIMARY KEY,
    applicant_id     BIGINT        NOT NULL,
    product_type     VARCHAR(80)   NOT NULL,
    requested_amount NUMERIC(12,2) NOT NULL,
    status           VARCHAR(30)   NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    decided_at       TIMESTAMPTZ,
    CONSTRAINT fk_application_applicant FOREIGN KEY (applicant_id) REFERENCES applicant (id)
);
CREATE INDEX IF NOT EXISTS idx_application_applicant ON credit_application (applicant_id);

CREATE TABLE IF NOT EXISTS alternative_data_record (
    id           BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT      NOT NULL,
    source_type  VARCHAR(40) NOT NULL,
    payload_json TEXT        NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    ingested_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_data_applicant FOREIGN KEY (applicant_id) REFERENCES applicant (id)
);
CREATE INDEX IF NOT EXISTS idx_data_applicant ON alternative_data_record (applicant_id);

-- -----------------------------------------------------------------------------
-- Risk assessments (one row per scoring run, history preserved)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS risk_assessment (
    id                 BIGSERIAL PRIMARY KEY,
    application_id     BIGINT           NOT NULL,
    hybrid_score       INT              NOT NULL,
    risk_tier          VARCHAR(40)      NOT NULL,
    pd_estimate        DOUBLE PRECISION NOT NULL,
    decision           VARCHAR(20)      NOT NULL,
    logit_z            DOUBLE PRECISION NOT NULL,
    weight_traditional DOUBLE PRECISION NOT NULL,
    weight_gnn         DOUBLE PRECISION NOT NULL,
    weight_alternative DOUBLE PRECISION NOT NULL,
    feature_json       TEXT             NOT NULL,
    attribution_json   TEXT             NOT NULL,
    counterfactual_json TEXT,
    embedding_json     TEXT,
    rationale          TEXT,
    manual_override    BOOLEAN          NOT NULL DEFAULT FALSE,
    override_reason    VARCHAR(500),
    outcome_repaid     BOOLEAN,
    model_version      VARCHAR(40)      NOT NULL,
    created_at         TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT fk_assessment_application FOREIGN KEY (application_id) REFERENCES credit_application (id)
);
CREATE INDEX IF NOT EXISTS idx_assessment_application ON risk_assessment (application_id);

-- -----------------------------------------------------------------------------
-- Append-only audit trail
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(60) NOT NULL,
    entity_id    BIGINT,
    action       VARCHAR(80) NOT NULL,
    actor        VARCHAR(120),
    detail_json  TEXT,
    payload_hash VARCHAR(64),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log (created_at DESC);

-- -----------------------------------------------------------------------------
-- pgvector store for contextual risk pattern search
-- The dimension matches the 11 model features in the feature catalog.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS feature_embedding (
    application_id BIGINT PRIMARY KEY,
    applicant_ref  VARCHAR(64),
    decision       VARCHAR(20),
    score          INT,
    embedding      vector(11),
    embedding_json TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Hierarchical Navigable Small World index for fast cosine nearest neighbor search.
CREATE INDEX IF NOT EXISTS idx_feature_embedding_hnsw
    ON feature_embedding USING hnsw (embedding vector_cosine_ops);
