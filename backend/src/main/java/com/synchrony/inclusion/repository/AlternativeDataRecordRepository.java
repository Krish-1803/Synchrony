package com.synchrony.inclusion.repository;

import com.synchrony.inclusion.domain.AlternativeDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlternativeDataRecordRepository extends JpaRepository<AlternativeDataRecord, Long> {

    List<AlternativeDataRecord> findByApplicantId(Long applicantId);
}
