package com.synchrony.inclusion.repository;

import com.synchrony.inclusion.domain.CreditApplication;
import com.synchrony.inclusion.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {

    List<CreditApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<CreditApplication> findByStatusInOrderByCreatedAtDesc(List<ApplicationStatus> statuses);

    List<CreditApplication> findAllByOrderByCreatedAtDesc();
}
