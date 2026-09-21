package com.synchrony.inclusion.repository;

import com.synchrony.inclusion.domain.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    Optional<Applicant> findByUserId(Long userId);

    Optional<Applicant> findByAnonymizedRef(String anonymizedRef);
}
