package com.synchrony.inclusion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

/**
 * Stores and queries dense feature embeddings for contextual risk pattern
 * search. When the database is PostgreSQL with the pgvector extension it uses an
 * HNSW indexed cosine search. Otherwise it falls back to an in-application cosine
 * scan over embeddings stored as JSON, so the prototype still returns neighbors
 * during local development.
 */
@Service
public class VectorService {

    private static final Logger log = LoggerFactory.getLogger(VectorService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private volatile boolean pgvectorEnabled = false;

    public VectorService(DataSource dataSource, ObjectMapper objectMapper) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void initialize() {
        String product;
        try {
            product = jdbcTemplate.execute((java.sql.Connection c) ->
                    c.getMetaData().getDatabaseProductName());
        } catch (Exception ex) {
            log.warn("Could not read database metadata: {}", ex.getMessage());
            return;
        }
        if (product == null || !product.toLowerCase().contains("postgresql")) {
            log.info("Vector search running in fallback mode. Database product: {}", product);
            return;
        }
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            int dim = com.synchrony.inclusion.scoring.FeatureCatalog.keys().size();
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS feature_embedding (
                        application_id BIGINT PRIMARY KEY,
                        applicant_ref VARCHAR(64),
                        decision VARCHAR(20),
                        score INT,
                        embedding vector(%d),
                        embedding_json TEXT,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    )""".formatted(dim));
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_feature_embedding_hnsw
                    ON feature_embedding USING hnsw (embedding vector_cosine_ops)""");
            pgvectorEnabled = true;
            log.info("pgvector similarity search is enabled with dimension {}", dim);
        } catch (Exception ex) {
            log.warn("pgvector setup failed, using fallback similarity: {}", ex.getMessage());
            pgvectorEnabled = false;
            ensureFallbackTable();
        }
    }

    private void ensureFallbackTable() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS feature_embedding (
                        application_id BIGINT PRIMARY KEY,
                        applicant_ref VARCHAR(64),
                        decision VARCHAR(20),
                        score INT,
                        embedding_json TEXT,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    )""");
        } catch (Exception ex) {
            log.warn("Could not create fallback embedding table: {}", ex.getMessage());
        }
    }

    public boolean isPgvectorEnabled() {
        return pgvectorEnabled;
    }

    /**
     * Inserts or updates the embedding for an application.
     */
    public void upsert(Long applicationId, String applicantRef, String decision, int score, double[] embedding) {
        String json = toJson(embedding);
        try {
            if (pgvectorEnabled) {
                String literal = toVectorLiteral(embedding);
                jdbcTemplate.update("""
                        INSERT INTO feature_embedding (application_id, applicant_ref, decision, score, embedding, embedding_json)
                        VALUES (?, ?, ?, ?, ?::vector, ?)
                        ON CONFLICT (application_id) DO UPDATE SET
                            applicant_ref = EXCLUDED.applicant_ref,
                            decision = EXCLUDED.decision,
                            score = EXCLUDED.score,
                            embedding = EXCLUDED.embedding,
                            embedding_json = EXCLUDED.embedding_json
                        """, applicationId, applicantRef, decision, score, literal, json);
            } else {
                jdbcTemplate.update("""
                        INSERT INTO feature_embedding (application_id, applicant_ref, decision, score, embedding_json)
                        VALUES (?, ?, ?, ?, ?)
                        ON CONFLICT (application_id) DO UPDATE SET
                            applicant_ref = EXCLUDED.applicant_ref,
                            decision = EXCLUDED.decision,
                            score = EXCLUDED.score,
                            embedding_json = EXCLUDED.embedding_json
                        """, applicationId, applicantRef, decision, score, json);
            }
        } catch (Exception ex) {
            log.warn("Embedding upsert failed for application {}: {}", applicationId, ex.getMessage());
        }
    }

    /**
     * Returns the closest historical applications by cosine distance.
     */
    public List<VectorNeighbor> findSimilar(double[] embedding, int k, Long excludeApplicationId) {
        try {
            if (pgvectorEnabled) {
                String literal = toVectorLiteral(embedding);
                return jdbcTemplate.query("""
                        SELECT application_id, applicant_ref, decision, score, embedding <=> ?::vector AS distance
                        FROM feature_embedding
                        WHERE application_id <> ?
                        ORDER BY embedding <=> ?::vector
                        LIMIT ?
                        """,
                        (rs, rn) -> new VectorNeighbor(
                                rs.getLong("application_id"),
                                rs.getString("applicant_ref"),
                                rs.getString("decision"),
                                rs.getInt("score"),
                                rs.getDouble("distance")),
                        literal, excludeApplicationId == null ? -1L : excludeApplicationId, literal, k);
            }
            return fallbackSearch(embedding, k, excludeApplicationId);
        } catch (Exception ex) {
            log.warn("Similarity search failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<VectorNeighbor> fallbackSearch(double[] query, int k, Long excludeApplicationId) {
        List<VectorNeighbor> all = new ArrayList<>();
        jdbcTemplate.query("SELECT application_id, applicant_ref, decision, score, embedding_json FROM feature_embedding",
                rs -> {
                    long appId = rs.getLong("application_id");
                    if (excludeApplicationId != null && appId == excludeApplicationId) {
                        return;
                    }
                    double[] candidate = fromJson(rs.getString("embedding_json"));
                    double distance = cosineDistance(query, candidate);
                    all.add(new VectorNeighbor(appId, rs.getString("applicant_ref"),
                            rs.getString("decision"), rs.getInt("score"), round(distance)));
                });
        all.sort(Comparator.comparingDouble(VectorNeighbor::distance));
        return all.size() > k ? all.subList(0, k) : all;
    }

    static double cosineDistance(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) {
            return 1.0;
        }
        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0.0 || nb == 0.0) {
            return 1.0;
        }
        double cosine = dot / (Math.sqrt(na) * Math.sqrt(nb));
        return 1.0 - cosine;
    }

    private String toVectorLiteral(double[] embedding) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (double v : embedding) {
            joiner.add(Double.toString(v));
        }
        return joiner.toString();
    }

    private String toJson(double[] embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private double[] fromJson(String json) {
        try {
            return objectMapper.readValue(json, double[].class);
        } catch (Exception ex) {
            return new double[0];
        }
    }

    private static double round(double v) {
        return Math.round(v * 100000.0) / 100000.0;
    }
}
