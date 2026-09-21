package com.synchrony.inclusion.repository;

import com.synchrony.inclusion.domain.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    List<RiskAssessment> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
