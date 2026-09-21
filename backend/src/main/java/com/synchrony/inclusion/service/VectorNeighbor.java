package com.synchrony.inclusion.service;

/**
 * A nearest neighbor returned from the vector similarity search over historical
 * feature embeddings.
 *
 * @param applicationId neighbor application id
 * @param applicantRef  anonymized applicant reference
 * @param decision      the decision recorded for the neighbor
 * @param score         the neighbor's hybrid score
 * @param distance      cosine distance from the query embedding, lower is closer
 */
public record VectorNeighbor(Long applicationId, String applicantRef, String decision, int score, double distance) {
}
